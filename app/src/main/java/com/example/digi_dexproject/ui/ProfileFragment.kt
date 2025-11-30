package com.example.digi_dexproject.ui

import CardAdapter
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.digi_dexproject.AppDatabase
import com.example.digi_dexproject.Card
import com.example.digi_dexproject.CardImage
import com.example.digi_dexproject.MainActivity
import com.example.digi_dexproject.R
import com.example.digi_dexproject.UserDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileFragment : Fragment() {

    private lateinit var usernameTextView: TextView
    private lateinit var cardsRecyclerView: RecyclerView
    private lateinit var cardAdapter: CardAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        usernameTextView = view.findViewById(R.id.text_view_username)
        cardsRecyclerView = view.findViewById(R.id.recycler_view_profile_cards)

        setupRecyclerView()
        loadUserData()

        return view
    }

    private fun loadUserData() {
        val prefs = requireActivity().getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
        val username = prefs.getString(MainActivity.KEY_USERNAME, "Guest")
        usernameTextView.text = username

        lifecycleScope.launch(Dispatchers.IO) {
            val context = requireContext()
            val userDb = UserDatabase.getDatabase(context)
            val appDb = AppDatabase.getDatabase(context)
            val user = userDb.userDao().getUserByUsername(username!!)

            if (user != null) {
                val scannedCardNames = user.scannedCards
                val scannedCardEntities = appDb.cardDao().getCardsByNames(scannedCardNames)
                val scannedCards = scannedCardEntities.map { entity ->
                    Card(
                        name = entity.name,
                        level = entity.level ?: 0,
                        type = entity.type,
                        attribute = entity.attribute ?: "",
                        race = entity.race ?: "",
                        atk = entity.atk ?: 0,
                        def = entity.def ?: 0,
                        desc = entity.desc,
                        card_prices = entity.cardPrices ?: emptyList(),
                        card_images = listOfNotNull(
                             entity.imageUrlSmall?.let {
                                CardImage(
                                    id = entity.id,
                                    imageUrl = entity.imageUrl ?: "",
                                    imageUrlSmall = it,
                                    imageUrlCropped = ""
                                )
                            }
                        )
                    )
                }
                withContext(Dispatchers.Main) {
                    cardAdapter.updateData(scannedCards, scannedCardNames.toSet())
                }
            }
        }
    }

    private fun setupRecyclerView() {
        cardsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        cardAdapter = CardAdapter(emptyList(), emptySet()) { card ->
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CardDetailFragment.newInstance(card))
                .addToBackStack(null)
                .commit()
        }
        cardsRecyclerView.adapter = cardAdapter
    }

    private fun displayScannedCards(cards: List<Card>) {
        cardAdapter.updateData(cards)
    }
}