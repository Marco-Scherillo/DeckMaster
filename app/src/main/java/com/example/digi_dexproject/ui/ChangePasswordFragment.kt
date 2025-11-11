package com.example.digi_dexproject.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.digi_dexproject.AuthViewModel
import com.example.digi_dexproject.MainActivity
import com.example.digi_dexproject.PasswordChangeEvent
import com.example.digi_dexproject.R
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ChangePasswordFragment : Fragment() {

    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var messageTextView: TextView
    private lateinit var oldPasswordEditText: EditText
    private lateinit var newPasswordEditText: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_change_password, container, false)

        oldPasswordEditText = view.findViewById(R.id.edit_text_old_password)
        newPasswordEditText = view.findViewById(R.id.edit_text_new_password)
        val submitButton = view.findViewById<Button>(R.id.button_submit_change_password)
        val backButton = view.findViewById<Button>(R.id.button_back_change_password)
        messageTextView = view.findViewById(R.id.text_view_message)

        val prefs = requireActivity().getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
        val username = prefs.getString(MainActivity.KEY_USERNAME, null)

        submitButton.setOnClickListener {
            val oldPassword = oldPasswordEditText.text.toString()
            val newPassword = newPasswordEditText.text.toString()

            if (username != null) {
                authViewModel.changePassword(username, oldPassword, newPassword)
            } else {
                // This case should ideally not happen if user is logged in
                displayMessage("Error: User not found.", false)
            }
        }

        backButton.setOnClickListener {
            (activity as? MainActivity)?.loadFragment(SettingsFragment())
        }

        // Observe password change results
        lifecycleScope.launch {
            authViewModel.passwordChangeResult.collectLatest { event ->
                when (event) {
                    is PasswordChangeEvent.Success -> {
                        displayMessage(event.message, true)
                        // Clear fields on success
                        oldPasswordEditText.text.clear()
                        newPasswordEditText.text.clear()
                    }
                    is PasswordChangeEvent.Failure -> {
                        displayMessage(event.message, false)
                    }
                }
            }
        }

        return view
    }

    private fun displayMessage(message: String, isSuccess: Boolean) {
        messageTextView.text = message
        if (isSuccess) {
            messageTextView.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark))
        } else {
            messageTextView.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark))
        }
        messageTextView.visibility = View.VISIBLE
    }
}