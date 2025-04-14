package edu.utap.bookbuddy

import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

object BooksRepository {
    private val db by lazy { Firebase.firestore }
    private var cachedBooks: List<Book>? = null

    fun fetchAllBooks(forceRefresh: Boolean = false, onResult: (List<Book>) -> Unit) {
        if (!forceRefresh && cachedBooks != null) {
            Log.d("BooksRepository", "Using cached books")
            onResult(cachedBooks!!)
            return
        }

        Log.d("BooksRepository", "Fetching books from Firestore")
        db.collection("books").get()
            .addOnSuccessListener { result ->
                val books = result.mapNotNull {
                    try {
                        val book = it.toObject(Book::class.java)
                        book?.copy(id = it.id)
                    } catch (e: Exception) {
                        Log.e("BooksRepository", "Error parsing document ${it.id}", e)
                        null
                    }
                }
                cachedBooks = books
                onResult(books)
            }
            .addOnFailureListener { e ->
                Log.e("BooksRepository", "Failed to fetch books from Firestore", e)
                onResult(emptyList())
            }
    }
}