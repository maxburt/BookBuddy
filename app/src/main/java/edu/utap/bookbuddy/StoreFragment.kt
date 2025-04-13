package edu.utap.bookbuddy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.navigation.fragment.findNavController

class StoreFragment : Fragment() {

    private lateinit var viewModel: StoreViewModel
    private lateinit var storeAdapter: StoreAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_store, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = ViewModelProvider(this).get(StoreViewModel::class.java)
        val recyclerView = view.findViewById<RecyclerView>(R.id.storeRecyclerView)

        storeAdapter = StoreAdapter { book ->
            val action = StoreFragmentDirections
                .actionStoreFragmentToBookDetailFragment(book.id)
            findNavController().navigate(action)
        }


        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = storeAdapter

        viewModel.storeBooks.observe(viewLifecycleOwner) { books ->
            storeAdapter.submitList(books)
        }

        viewModel.fetchStoreBooks()
    }
}