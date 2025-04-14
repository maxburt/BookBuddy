package edu.utap.bookbuddy

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import edu.utap.bookbuddy.databinding.FragmentBookDetailBinding
import android.util.Log
import android.widget.EditText
import android.widget.RatingBar

class BookDetailFragment : Fragment() {
    private var _binding: FragmentBookDetailBinding? = null
    private val binding get() = _binding!!

    private val args: BookDetailFragmentArgs by navArgs()
    private lateinit var authUser: AuthUser
    private val viewModel: BookDetailViewModel by viewModels()
    private lateinit var currentBook: Book

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        authUser = AuthUser(requireActivity().activityResultRegistry)
        lifecycle.addObserver(authUser)

        val bookId = args.bookId
        Log.d("BookDetail", "Loading book ID: $bookId")

        viewModel.fetchBook(bookId) { book ->
            if (book != null) {
                bindBookToUI(book)
            } else {
                Log.w("BookDetail", "Book object was null for ID: $bookId")
            }
        }

        viewModel.reviews.observe(viewLifecycleOwner) { reviews ->
            if (reviews.isEmpty()) {
                binding.reviewRecycler.visibility = View.GONE
            } else {
                val adapter = ReviewAdapter(reviews)
                binding.reviewRecycler.adapter = adapter
                binding.reviewRecycler.layoutManager = LinearLayoutManager(requireContext())
                binding.reviewRecycler.visibility = View.VISIBLE
            }
        }

        viewModel.fetchReviews(bookId)
    }

    private fun bindBookToUI(book: Book) {
        binding.bookTitle.text = book.title
        binding.bookAuthor.text = book.author
        binding.bookDescription.text = book.description
        currentBook = book

        Glide.with(requireContext())
            .load(book.coverUrl)
            .into(binding.bookCover)

        binding.downloadButton.setOnClickListener {
            val currentUser = authUser.observeUser().value ?: invalidUser
            if (currentUser.isInvalid()) {
                Toast.makeText(requireContext(), "You must be logged in to download", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.checkIfUserHasBook(currentBook.id, currentUser.uid) { alreadyHasBook ->
                if (alreadyHasBook) {
                    Toast.makeText(requireContext(), "This book is already in your library", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.addBookToUserLibrary(currentUser.uid, currentBook) { success ->
                        if (success) {
                            Toast.makeText(requireContext(), "Book added to your library", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "Failed to download book", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        binding.leaveReviewButton.setOnClickListener {
            val currentUser = authUser.observeUser().value ?: invalidUser
            if (currentUser.isInvalid()) {
                Toast.makeText(requireContext(), "You must be logged in to leave a review", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.checkIfUserReviewed(args.bookId, currentUser.uid) { hasReviewed ->
                if (hasReviewed) {
                    Toast.makeText(requireContext(), "You've already left a review for this book", Toast.LENGTH_SHORT).show()
                } else {
                    showLeaveReviewDialog(currentUser)
                }
            }
        }
    }

    private fun showLeaveReviewDialog(currentUser: User) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_leave_review, null)
        val ratingBar = dialogView.findViewById<RatingBar>(R.id.reviewRatingBar)
        val commentEditText = dialogView.findViewById<EditText>(R.id.reviewCommentEditText)

        AlertDialog.Builder(requireContext())
            .setTitle("Leave a Review")
            .setView(dialogView)
            .setPositiveButton("Submit") { _, _ ->
                val rating = ratingBar.rating.toInt()
                val comment = commentEditText.text.toString().trim()

                if (rating > 0 && comment.isNotEmpty()) {
                    val review = Review(
                        userId = currentUser.uid,
                        userName = currentUser.name,
                        rating = rating,
                        comment = comment,
                        timestamp = System.currentTimeMillis()
                    )
                    viewModel.submitReview(args.bookId, review) { success ->
                        if (success) {
                            Toast.makeText(requireContext(), "Review submitted", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "Failed to submit review", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Please give a rating and comment", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
