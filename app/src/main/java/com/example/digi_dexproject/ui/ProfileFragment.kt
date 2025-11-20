package com.example.digi_dexproject.ui

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.digi_dexproject.CardAdapter
import com.example.digi_dexproject.MainActivity
import com.example.digi_dexproject.R

/**
 * A fragment to display the user's profile information, including their username
 * and a list of their collected cards.
 */
class ProfileFragment : Fragment() {

    private lateinit var usernameTextView: TextView
    private lateinit var cardsRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        // Find the UI elements from the layout
        usernameTextView = view.findViewById(R.id.text_view_username)
        cardsRecyclerView = view.findViewById(R.id.recycler_view_profile_cards)

        // Set up the RecyclerView with a placeholder adapter
        setupRecyclerView()

        // Load the user's data
        loadUserData()

        return view
    }

    private fun loadUserData() {
        // Get the SharedPreferences instance to read saved data
        val prefs = requireActivity().getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)

        // Read the saved username. If it doesn't exist, default to "Guest".
        val username = prefs.getString(MainActivity.KEY_USERNAME, "Guest")

        // Set the username in the TextView
        usernameTextView.text = username
    }

    private fun setupRecyclerView() {
        // Set the layout manager that the RecyclerView will use
        cardsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Create an adapter with an empty list for now (our placeholder)
        // We will need to fix the CardAdapter for this to work
        val cardAdapter = CardAdapter(emptyList()) { card ->
            // This is where you would handle a click on a card in the future
        }
        cardsRecyclerView.adapter = cardAdapter
    }
}
