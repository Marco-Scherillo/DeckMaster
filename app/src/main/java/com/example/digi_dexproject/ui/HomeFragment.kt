package com.example.digi_dexproject.ui

import CardAdapter
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.digi_dexproject.* // Import all necessary classes like Card, CardImage, CardPrice
import com.example.digi_dexproject.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var cardAdapter: CardAdapter
    private lateinit var searchBar: EditText
    private var allCards: List<Card> = emptyList() // The list that holds the full dataset
    private var scannedCardNames: Set<String> = emptySet()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.card_recycler_view)
        searchBar = view.findViewById(R.id.search_bar)

        // 1. Set up the RecyclerView and Adapter ONCE with empty data
        setupRecyclerView()

        // 2. Load the data from the database
        loadScannedCardsAndThenAllCards()

        // 3. Set up the search functionality
        setupSearch()
    }

    private fun setupRecyclerView() {
        // Initialize the adapter with an empty list. It will be updated later.
        cardAdapter = CardAdapter(emptyList(), emptySet()) { card ->
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CardDetailFragment.newInstance(card))
                .addToBackStack(null)
                .commit()
        }
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = cardAdapter
    }

    private fun setupSearch() {
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterCards(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterCards(query: String) {
        val filteredList = if (query.isEmpty()) {
            allCards // If search is empty, show the full list
        } else {
            allCards.filter {
                it.name.contains(query, ignoreCase = true)
            }
        }
        // Simply update the data in the existing adapter
        cardAdapter.updateData(filteredList, scannedCardNames)
    }

    private fun loadScannedCardsAndThenAllCards() {
        lifecycleScope.launch(Dispatchers.IO) {
            val username = requireActivity().getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
                .getString(MainActivity.KEY_USERNAME, null)
            if (username != null) {
                val userDb = UserDatabase.getDatabase(requireContext())
                val user = userDb.userDao().getUserByUsername(username)
                if (user != null) {
                    scannedCardNames = user.scannedCards.toSet()
                }
            }
            // After getting scanned cards, load the main card list
            loadAllCards()
        }
    }

    private suspend fun loadAllCards() {
        val cardDao = AppDatabase.getDatabase(requireContext()).cardDao()
        val cardEntities = cardDao.getAll()
        // Convert the database entities to UI data objects and store them
        allCards = cardEntities.map { toCardData(it) }

        // Switch to the main thread to update the UI
        withContext(Dispatchers.Main) {
            // Update the adapter with the full list of cards
            cardAdapter.updateData(allCards, scannedCardNames)
        }
    }

    // --- THIS IS THE IMPLEMENTED FIX ---
    // This function now correctly converts a CardEntity into a complete Card object.
    private fun toCardData(cardEntity: CardEntity): Card {
        // Recreate the CardImage list from the database data
        val cardImages = cardEntity.imageUrl?.let {
            listOf(CardImage(
                id = cardEntity.id,
                imageUrl = it,
                imageUrlSmall = cardEntity.imageUrlSmall ?: "",
                imageUrlCropped = null
            ))
        } ?: emptyList()

        // Get the CardPrice list from the database data
        val cardPrices = cardEntity.cardPrices ?: emptyList()

        // Return a complete Card object, including the prices
        return Card(
            name = cardEntity.name ?: "",
            level = cardEntity.level ?: 0,
            type = cardEntity.type ?: "",
            attribute = cardEntity.attribute ?: "",
            race = cardEntity.race ?: "",
            atk = cardEntity.atk ?: 0,
            def = cardEntity.def ?: 0,
            desc = cardEntity.desc ?: "",
            card_images = cardImages,
            card_prices = cardPrices
        )
    }
}
