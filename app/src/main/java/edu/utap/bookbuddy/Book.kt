package edu.utap.bookbuddy

data class Book(
    val id: String = "",              // Firestore doc ID (set manually from snapshot.id)
    val title: String = "",
    val author: String = "",
    val description: String = "",
    val coverUrl: String = "",        // Link to image in Firebase Storage
    val downloadUrl: String = "",         // Link to EPUB file in Firebase Storage
    val uploadTime: Long = 0L,       // Optional: for sorting or freshness
    val avgRating: Double = 0.0,
    val numRatings: Int = 0
)