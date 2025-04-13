package edu.utap.bookbuddy

data class Review(
    val userId: String = "",
    val userName: String = "", // optional display name
    val rating: Int = 0,       // 1–5 stars
    val comment: String = "",
    val timestamp: Long = 0L
)