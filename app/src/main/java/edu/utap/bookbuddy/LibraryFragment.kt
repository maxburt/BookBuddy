package edu.utap.bookbuddy

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL


class LibraryFragment : Fragment() {

    private lateinit var viewModel: LibraryViewModel
    private lateinit var adapter: LibraryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_library, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this)[LibraryViewModel::class.java]
        val recyclerView = view.findViewById<RecyclerView>(R.id.libraryRecyclerView)

        adapter = LibraryAdapter { book ->
            downloadBookIfNeeded(book) { localPath ->
                if (localPath != null) {
                    val action = LibraryFragmentDirections
                        .actionLibraryFragmentToReaderFragment(book.id)
                    findNavController().navigate(action)
                } else {
                    Log.e("LibraryFragment", "Failed to download EPUB")
                }
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        viewModel.libraryBooks.observe(viewLifecycleOwner) { books ->
            adapter.submitList(books)
            view.findViewById<TextView>(R.id.libraryPlaceholder).visibility =
                if (books.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.fetchUserLibrary()
    }

    private fun downloadBookIfNeeded(book: Book, onComplete: (String?) -> Unit) {
        val file = File(requireContext().filesDir, "${book.id}.epub")
        Log.d("Download", "Checking if file exists: ${file.absolutePath}")

        if (file.exists()) {
            Log.d("Download", "File already exists locally. Skipping download.")
            onComplete(file.absolutePath)
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("Download", "Starting download for book: ${book.id}")
                Log.d("Download", "Raw fileUrl: '${book.downloadUrl}'")

                val fullUrl = if (book.downloadUrl.startsWith("http")) {
                    book.downloadUrl
                } else {
                    // Optional fallback if your DB is missing the full URL
                    "https://firebasestorage.googleapis.com/v0/b/bookbuddy-2bb4b.appspot.com/o/${book.downloadUrl}?alt=media"
                }

                Log.d("Download", "Resolved full download URL: $fullUrl")

                val url = URL(fullUrl)
                val connection = url.openConnection()
                connection.connect()

                val input = connection.getInputStream()
                val output = FileOutputStream(file)
                input.copyTo(output)
                input.close()
                output.close()

                Log.d("Download", "Download complete for book: ${book.id}")
                withContext(Dispatchers.Main) {
                    onComplete(file.absolutePath)
                }
            } catch (e: Exception) {
                Log.e("Download", "Failed to download EPUB for book: ${book.id}", e)
                withContext(Dispatchers.Main) {
                    onComplete(null)
                }
            }
        }
    }

}