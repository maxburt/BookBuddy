package edu.utap.bookbuddy

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class StoreViewModel : ViewModel() {
    private val booksRepo = BooksRepository
    private val _storeBooks = MutableLiveData<List<Book>>()
    val storeBooks: LiveData<List<Book>> = _storeBooks

    init {
        Log.d("StoreViewModel", "Calling fetchAllBooks()...")
        booksRepo.fetchAllBooks { bookList ->
            Log.d("StoreViewModel", "Books received: ${bookList.size}")
            _storeBooks.postValue(bookList)
        }
    }

    fun fetchStoreBooks() {
        booksRepo.fetchAllBooks { books ->
            _storeBooks.postValue(books)
        }
    }
}