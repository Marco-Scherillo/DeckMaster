package com.example.digi_dexproject.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.digi_dexproject.AuthEvent
import com.example.digi_dexproject.AuthViewModel
import com.example.digi_dexproject.MainActivity
import com.example.digi_dexproject.R
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private val authViewModel: AuthViewModel by viewModels()
    private lateinit var errorTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        val usernameEditText = view.findViewById<EditText>(R.id.edit_text_username_login)
        val passwordEditText = view.findViewById<EditText>(R.id.edit_text_password_login)
        val submitButton = view.findViewById<Button>(R.id.button_submit_login)
        val backButton = view.findViewById<Button>(R.id.button_back_login)
        errorTextView = view.findViewById(R.id.text_view_error_login)

        submitButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()
            authViewModel.login(username, password)
        }

        backButton.setOnClickListener {
            (activity as? MainActivity)?.loadFragment(AuthFragment())
        }

        // Observe auth results
        lifecycleScope.launch {
            authViewModel.authResult.collectLatest { event ->
                when (event) {
                    is AuthEvent.Success -> {
                        (activity as? MainActivity)?.onLoginSuccess(event.username)
                    }
                    is AuthEvent.Failure -> {
                        errorTextView.text = event.message
                        errorTextView.visibility = View.VISIBLE
                    }
                }
            }
        }

        return view
    }
}