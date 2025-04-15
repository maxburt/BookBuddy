package edu.utap.bookbuddy

import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

// This is our abstract concept of a User, which is visible
// outside AuthUser.  That way, client code will not change
// if we use something other than Firebase for authentication
data class User(
    private val nullableName: String?,
    private val nullableEmail: String?,
    val uid: String
) {
    val name: String = nullableName ?: "User logged out"
    val email: String = nullableEmail ?: "User logged out"
}

const val invalidUserUid = "-1"

fun User.isInvalid(): Boolean {
    return uid == invalidUserUid
}

val invalidUser = User(null, null, invalidUserUid)

class AuthUser(private val registry: ActivityResultRegistry) :
    FirebaseAuth.AuthStateListener,
    DefaultLifecycleObserver {

    companion object {
        private const val TAG = "AuthUser"
    }

    private var pendingLogin = false
    private lateinit var signInLauncher: ActivityResultLauncher<Intent>
    private var liveUser = MutableLiveData<User>().apply {
        this.postValue(invalidUser)
    }

    init {
        Firebase.auth.addAuthStateListener(this)
    }

    fun observeUser(): LiveData<User> {
        return liveUser
    }

    private fun postUserUpdate(firebaseUser: FirebaseUser?) {
        if (firebaseUser == null) {
            liveUser.postValue(invalidUser)
        } else {
            liveUser.postValue(
                User(
                    firebaseUser.displayName,
                    firebaseUser.email,
                    firebaseUser.uid
                )
            )
        }
    }

    override fun onAuthStateChanged(p0: FirebaseAuth) {
        postUserUpdate(p0.currentUser)
    }

    override fun onCreate(owner: LifecycleOwner) {
        signInLauncher = registry.register("key", owner, FirebaseAuthUIActivityResultContract()) { result ->
            Log.d(TAG, "sign in result ${result.resultCode}")
            pendingLogin = false

            val firebaseUser = Firebase.auth.currentUser
            if (firebaseUser != null) {
                val db = Firebase.firestore
                val userDocRef = db.collection("users").document(firebaseUser.uid)

                userDocRef.get().addOnSuccessListener { document ->
                    if (!document.exists()) {
                        val nameToUse = firebaseUser.displayName ?: "Anonymous"
                        val userData = hashMapOf(
                            "uid" to firebaseUser.uid,
                            "name" to nameToUse,
                            "email" to firebaseUser.email,
                            "theme" to "light",
                            "favorites" to listOf<String>()
                        )

                        userDocRef.set(userData).addOnSuccessListener {
                            Log.d(TAG, "User doc created with correct name: $nameToUse")
                        }.addOnFailureListener { e ->
                            Log.w(TAG, "Error creating user doc", e)
                        }
                    } else {
                        Log.d(TAG, "User already exists in Firestore: ${firebaseUser.uid}")
                    }
                }.addOnFailureListener { e ->
                    Log.w(TAG, "Error checking if user exists", e)
                }
            }
        }
    }

    private fun user(): FirebaseUser? {
        return Firebase.auth.currentUser
    }

    fun login() {
        if (user() == null && !pendingLogin) {
            Log.d(TAG, "User null, log in")
            pendingLogin = true

            val providers = arrayListOf(AuthUI.IdpConfig.EmailBuilder().build())

            val signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setIsSmartLockEnabled(false)
                .setTheme(R.style.Theme_FirebaseAuth_NoActionBar)
                .build()
            signInLauncher.launch(signInIntent)
        }
    }

    fun logout() {
        if (user() == null) return
        Firebase.auth.signOut()
    }
}
