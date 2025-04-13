//Currently not in use!!!!
package edu.utap.bookbuddy

import android.util.Log
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ReviewsRepository {
    private val db = Firebase.firestore

    fun fetchReviewsForBook(
        bookId: String,
        onResult: (List<Review>) -> Unit
    ) {
        db.collection("books")
            .document(bookId)
            .collection("reviews")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val reviews = result.mapNotNull { it.toObject(Review::class.java) }
                onResult(reviews)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    fun addReview(
        bookId: String,
        review: Review,
        onComplete: (Boolean) -> Unit
    ) {
        db.collection("books")
            .document(bookId)
            .collection("reviews")
            .add(review)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }
}