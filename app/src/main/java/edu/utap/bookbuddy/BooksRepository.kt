package edu.utap.bookbuddy

import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class BooksRepository {
    private val db = Firebase.firestore

    fun fetchAllBooks(onResult: (List<Book>) -> Unit) {
        Log.d("BooksRepository", "Starting fetchAllBooks()...")

        db.collection("books").get()
            .addOnSuccessListener { result ->
                Log.d("BooksRepository", "Successfully fetched ${result.size()} documents.")

                result.documents.forEachIndexed { index, doc ->
                    Log.d("BooksRepository", "Document $index ID: ${doc.id}, Data: ${doc.data}")
                }

                val books = result.mapNotNull {
                    try {
                        val book = it.toObject(Book::class.java)
                        Log.d("BooksRepository", "Parsed book: $book")
                        book?.copy(id = it.id)
                    } catch (e: Exception) {
                        Log.e("BooksRepository", "Error parsing document ${it.id}", e)
                        null
                    }
                }

                Log.d("BooksRepository", "Final book list size: ${books.size}")
                onResult(books)
            }
            .addOnFailureListener { e ->
                Log.e("BooksRepository", "Failed to fetch books from Firestore", e)
                onResult(emptyList())
            }
    }
}