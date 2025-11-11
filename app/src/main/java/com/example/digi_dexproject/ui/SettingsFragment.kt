package com.example.digi_dexproject.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.digi_dexproject.MainActivity
import com.example.digi_dexproject.R

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        val changePasswordButton = view.findViewById<Button>(R.id.button_change_password)
        val logoutButton = view.findViewById<Button>(R.id.button_logout)

        changePasswordButton.setOnClickListener {
            (activity as? MainActivity)?.loadFragment(ChangePasswordFragment())
        }

        logoutButton.setOnClickListener {
            (activity as? MainActivity)?.logout()
        }

        return view
    }
}