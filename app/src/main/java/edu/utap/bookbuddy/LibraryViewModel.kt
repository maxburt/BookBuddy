package edu.utap.bookbuddy

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class LibraryViewModel : ViewModel() {
    private val db = Firebase.firestore
    private val booksRepo = BooksRepository()

    private val _libraryBooks = MutableLiveData<List<Book>>()
    val libraryBooks: LiveData<List<Book>> = _libraryBooks

    fun fetchUserLibrary() {
        val uid = Firebase.auth.currentUser?.uid ?: return
        db.collection("users")
            .document(uid)
            .collection("library")
            .get()
            .addOnSuccessListener { snapshot ->
                val bookRefs = snapshot.mapNotNull { it.getString("bookId") }
                booksRepo.fetchAllBooks { allBooks ->
                    val userBooks = allBooks.filter { it.id in bookRefs }
                    _libraryBooks.postValue(userBooks)
                }
            }
            .addOnFailureListener {
                _libraryBooks.postValue(emptyList())
            }
    }
}