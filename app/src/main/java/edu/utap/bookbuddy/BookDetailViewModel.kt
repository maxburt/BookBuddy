package edu.utap.bookbuddy

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore

class BookDetailViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val reviewsRepo = ReviewsRepository()

    private val _reviews = MutableLiveData<List<Review>>()
    val reviews: LiveData<List<Review>> = _reviews

    private val _avgRating = MutableLiveData<Double>()
    val avgRating: LiveData<Double> = _avgRating
    private val _numRatings = MutableLiveData<Int>()
    val numRatings: LiveData<Int> = _numRatings

    fun fetchAvgRating(bookId: String) {
        db.collection("books")
            .document(bookId)
            .get()
            .addOnSuccessListener { document ->
                val avg = document.getDouble("avgRating")
                _avgRating.value = avg ?: 0.0

                val num = document.getLong("numRatings")?.toInt()
                _numRatings.value = num ?: 0
            }
            .addOnFailureListener { exception ->
                Log.e("BookDetailVM", "Error fetching rating data", exception)
                _avgRating.value = 0.0
                _numRatings.value = 0
            }
    }

    fun fetchBook(bookId: String, onResult: (Book?) -> Unit) {
        db.collection("books")
            .document(bookId)
            .get()
            .addOnSuccessListener { doc ->
                val book = doc.toObject(Book::class.java)?.copy(id = doc.id)
                onResult(book)
            }
            .addOnFailureListener {
                Log.e("BookDetailVM", "Failed to load book: ${it.message}")
                onResult(null)
            }
    }

    fun fetchReviews(bookId: String) {
        reviewsRepo.fetchReviewsForBook(bookId) { reviewList ->
            _reviews.postValue(reviewList)
        }
    }

    fun submitReview(bookId: String, review: Review, onComplete: (Boolean) -> Unit) {
        reviewsRepo.addReview(bookId, review) { success ->
            if (success) {
                fetchReviews(bookId) // Refresh UI

                // Recalculate average rating and update Firestore
                db.collection("books")
                    .document(bookId)
                    .collection("reviews")
                    .get()
                    .addOnSuccessListener { snapshot ->
                        val allRatings = snapshot.mapNotNull { it.getDouble("rating") }
                        val numRatings = allRatings.size
                        val avgRating = if (numRatings > 0) allRatings.sum() / numRatings else 0.0

                        db.collection("books")
                            .document(bookId)
                            .update(
                                mapOf(
                                    "avgRating" to avgRating,
                                    "numRatings" to numRatings
                                )
                            )
                            .addOnSuccessListener {
                                Log.d("BookDetailVM", "Updated avgRating=$avgRating and numRatings=$numRatings")
                                _avgRating.value = avgRating
                                _numRatings.value = numRatings
                            }
                            .addOnFailureListener {
                                Log.e("BookDetailVM", "Failed to update ratings", it)
                            }
                    }
                    .addOnFailureListener {
                        Log.e("BookDetailVM", "Failed to recalculate ratings", it)
                    }
            }
            onComplete(success)
        }
    }

    fun checkIfUserReviewed(bookId: String, userId: String, onResult: (Boolean) -> Unit) {
        db.collection("books")
            .document(bookId)
            .collection("reviews")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                onResult(!querySnapshot.isEmpty)
            }
            .addOnFailureListener {
                Log.e("BookDetailVM", "Failed to check user review: ${it.message}")
                onResult(false)
            }
    }

    fun checkIfUserHasBook(bookId: String, userId: String, onResult: (Boolean) -> Unit) {
        db.collection("users")
            .document(userId)
            .collection("library")
            .document(bookId)
            .get()
            .addOnSuccessListener { document ->
                onResult(document.exists())
            }
            .addOnFailureListener { e ->
                Log.e("BookDetailVM", "Error checking if book exists: ${e.message}", e)
                onResult(false)
            }
    }

    fun addBookToUserLibrary(userId: String, book: Book, onComplete: (Boolean) -> Unit) {
        val data = hashMapOf(
            "bookId" to book.id,
            "timestamp" to System.currentTimeMillis(),
            "title" to book.title,
            "progress" to "",
            "highlights" to hashMapOf<String, Any>()
        )

        Log.d("AddToLibrary", "Preparing to add book to library")
        Log.d("AddToLibrary", "User ID: $userId")
        Log.d("AddToLibrary", "Book ID: ${book.id}")
        Log.d("AddToLibrary", "Book Title: ${book.title}")

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("library")
            .document(book.id)
            .set(data)
            .addOnSuccessListener {
                Log.d("AddToLibrary", "Successfully added book to user's library")
                onComplete(true)
            }
            .addOnFailureListener { e ->
                Log.e("AddToLibrary", "Failed to add book: ${e.message}", e)
                onComplete(false)
            }
    }
}