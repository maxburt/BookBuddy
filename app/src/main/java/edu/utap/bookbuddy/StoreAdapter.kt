package edu.utap.bookbuddy

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide


class StoreAdapter(
    private val onBookClick: (Book) -> Unit
) : ListAdapter<Book, StoreAdapter.BookViewHolder>(BookDiffCallback()) {

    class BookViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val titleTV: TextView = view.findViewById(R.id.titleTextView)
        private val authorTV: TextView = view.findViewById(R.id.authorTextView)
        private val coverIV: ImageView = view.findViewById(R.id.bookCoverImageView)

        fun bind(book: Book, onClick: (Book) -> Unit) {
            titleTV.text = book.title
            authorTV.text = "by ${book.author}"

            Glide.with(itemView.context)
                .load(book.coverUrl)
                .placeholder(R.drawable.placeholder_cover)
                .error(R.drawable.error_cover)
                .into(coverIV)

            itemView.setOnClickListener {
                onClick(book)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_book, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(getItem(position), onBookClick)
    }
}

class BookDiffCallback : DiffUtil.ItemCallback<Book>() {
    override fun areItemsTheSame(old: Book, new: Book) = old.id == new.id
    override fun areContentsTheSame(old: Book, new: Book) = old == new
}