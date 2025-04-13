package edu.utap.bookbuddy

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import edu.utap.bookbuddy.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    companion object {
        const val TAG = "LoginFragment"
    }

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var authUser: AuthUser


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize AuthUser
        authUser = AuthUser(requireActivity().activityResultRegistry)
        lifecycle.addObserver(authUser)

        // Set button click listeners
        binding.loginBut.setOnClickListener {
            authUser.login()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}