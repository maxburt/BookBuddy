package edu.utap.bookbuddy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore

class SettingsFragment : Fragment() {

    private val fontSizes = listOf("80%", "100%", "120%", "150%")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        val logoutButton = view.findViewById<Button>(R.id.logoutButton)
        val fontSizeSpinner = view.findViewById<Spinner>(R.id.fontSizeSpinner)
        fontSizeSpinner.visibility = View.INVISIBLE  // Hide it at first

        logoutButton.setOnClickListener {
            (activity as? MainActivity)?.authUser?.logout()
        }

        val currentUser = (activity as? MainActivity)?.authUser?.observeUser()?.value
        if (currentUser != null && !currentUser.isInvalid()) {
            val uid = currentUser.uid
            val userDoc = FirebaseFirestore.getInstance().collection("users").document(uid)

            var skipNextSelection = true

            userDoc.get().addOnSuccessListener { document ->
                val savedSize = document.getString("fontSize") ?: "100%"
                val index = fontSizes.indexOf(savedSize).takeIf { it >= 0 } ?: 1

                // Only set adapter and visibility after getting saved value
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, fontSizes)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                fontSizeSpinner.adapter = adapter
                fontSizeSpinner.setSelection(index)
                fontSizeSpinner.visibility = View.VISIBLE

                fontSizeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        if (skipNextSelection) {
                            skipNextSelection = false
                            return
                        }

                        val selectedSize = fontSizes[position]
                        userDoc.update("fontSize", selectedSize)
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "Font size updated", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(requireContext(), "Failed to update font size", Toast.LENGTH_SHORT).show()
                            }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            }
        }

        return view
    }
}