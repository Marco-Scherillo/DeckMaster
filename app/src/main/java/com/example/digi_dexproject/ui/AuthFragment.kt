package com.example.digi_dexproject.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.digi_dexproject.MainActivity
import com.example.digi_dexproject.R

class AuthFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_auth, container, false)

        val toLoginButton = view.findViewById<Button>(R.id.button_to_login)
        val toRegisterButton = view.findViewById<Button>(R.id.button_to_register)

        toLoginButton.setOnClickListener {
            (activity as? MainActivity)?.loadFragment(LoginFragment())
        }

        toRegisterButton.setOnClickListener {
            (activity as? MainActivity)?.loadFragment(RegisterFragment())
        }

        return view
    }
}