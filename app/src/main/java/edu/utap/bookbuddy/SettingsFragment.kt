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
    private val fontTypes = listOf("sans-serif", "serif", "monospace")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        val logoutButton = view.findViewById<Button>(R.id.logoutButton)
        val fontSizeSpinner = view.findViewById<Spinner>(R.id.fontSizeSpinner)
        val fontTypeSpinner = view.findViewById<Spinner>(R.id.fontTypeSpinner)
        val themeSwitch = view.findViewById<Switch>(R.id.themeSwitch)

        fontSizeSpinner.visibility = View.INVISIBLE
        fontTypeSpinner.visibility = View.INVISIBLE
        themeSwitch.visibility = View.INVISIBLE

        logoutButton.setOnClickListener {
            (activity as? MainActivity)?.authUser?.logout()
        }

        val currentUser = (activity as? MainActivity)?.authUser?.observeUser()?.value
        if (currentUser != null && !currentUser.isInvalid()) {
            val uid = currentUser.uid
            val userDoc = FirebaseFirestore.getInstance().collection("users").document(uid)

            var skipSizeSelect = true
            var skipTypeSelect = true

            userDoc.get().addOnSuccessListener { document ->
                val savedSize = document.getString("fontSize") ?: "100%"
                val savedType = document.getString("fontType") ?: "sans-serif"
                val savedTheme = document.getString("theme") ?: "light"

                // Font Size Spinner Setup
                val sizeIndex = fontSizes.indexOf(savedSize).takeIf { it >= 0 } ?: 1
                val sizeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, fontSizes)
                sizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                fontSizeSpinner.adapter = sizeAdapter
                fontSizeSpinner.setSelection(sizeIndex)
                fontSizeSpinner.visibility = View.VISIBLE

                fontSizeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        if (skipSizeSelect) {
                            skipSizeSelect = false
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

                // Font Type Spinner Setup
                val typeIndex = fontTypes.indexOf(savedType).takeIf { it >= 0 } ?: 0
                val typeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, fontTypes)
                typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                fontTypeSpinner.adapter = typeAdapter
                fontTypeSpinner.setSelection(typeIndex)
                fontTypeSpinner.visibility = View.VISIBLE

                fontTypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        if (skipTypeSelect) {
                            skipTypeSelect = false
                            return
                        }

                        val selectedType = fontTypes[position]
                        userDoc.update("fontType", selectedType)
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "Font type updated", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(requireContext(), "Failed to update font type", Toast.LENGTH_SHORT).show()
                            }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }

                // Theme Switch Setup
                themeSwitch.isChecked = savedTheme == "dark"
                themeSwitch.visibility = View.VISIBLE

                themeSwitch.setOnCheckedChangeListener { _, isChecked ->
                    val newTheme = if (isChecked) "dark" else "light"
                    userDoc.update("theme", newTheme)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Theme updated to $newTheme", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Failed to update theme", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }

        return view
    }
}