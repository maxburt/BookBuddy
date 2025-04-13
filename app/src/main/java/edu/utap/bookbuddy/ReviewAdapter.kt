package edu.utap.bookbuddy

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ReviewAdapter(private val reviews: List<Review>) :
    RecyclerView.Adapter<ReviewAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val nameTV: TextView = view.findViewById(R.id.reviewDisplayName)
        private val commentTV: TextView = view.findViewById(R.id.reviewText)
        private val timestampTV: TextView = view.findViewById(R.id.reviewTimestamp)
        private val ratingBar: RatingBar = view.findViewById(R.id.reviewRatingBar)

        fun bind(review: Review) {
            nameTV.text = review.userName
            commentTV.text = review.comment
            timestampTV.text = formatTimestamp(review.timestamp)
            ratingBar.rating = review.rating.toFloat()
        }

        private fun formatTimestamp(timestamp: Long): String {
            return if (timestamp == 0L) "" else {
                val date = java.util.Date(timestamp)
                val format = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                format.format(date)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_review, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(reviews[position])
    }

    override fun getItemCount() = reviews.size
}