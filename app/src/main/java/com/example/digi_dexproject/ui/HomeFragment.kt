package com.example.digi_dexproject.ui

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
import com.example.digi_dexproject.AppDatabase
import com.example.digi_dexproject.Card
import com.example.digi_dexproject.CardAdapter
import com.example.digi_dexproject.CardEntity
import com.example.digi_dexproject.CardImage
import com.example.digi_dexproject.MainActivity
import com.example.digi_dexproject.R
import com.example.digi_dexproject.UserDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var cardAdapter: CardAdapter
    private lateinit var searchBar: EditText
    private var allCards: List<Card> = emptyList()
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
        recyclerView.layoutManager = LinearLayoutManager(context)
        searchBar = view.findViewById(R.id.search_bar)

        loadScannedCardsAndThenAllCards()
        setupSearch()
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
        val filteredList = allCards.filter {
            it.name.contains(query, ignoreCase = true)
        }
        cardAdapter.updateData(filteredList)
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
            loadAllCards()
        }
    }

    private suspend fun loadAllCards() {
        val cardDao = AppDatabase.getDatabase(requireContext()).cardDao()
        val cardEntities = cardDao.getAll()
        allCards = cardEntities.map { toCardData(it) }

        withContext(Dispatchers.Main) {
            cardAdapter = CardAdapter(allCards, scannedCardNames) { card ->
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, CardDetailFragment.newInstance(card))
                    .addToBackStack(null)
                    .commit()
            }
            recyclerView.adapter = cardAdapter
        }
    }

    private fun toCardData(cardEntity: CardEntity): Card {
        val cardImages = listOf(CardImage(
            id = cardEntity.id,
            imageUrl = cardEntity.imageUrl ?: "",
            imageUrlSmall = cardEntity.imageUrlSmall ?: "",
            imageUrlCropped = null
        ))

        return Card(
            name = cardEntity.name,
            level = cardEntity.level ?: 0,
            type = cardEntity.type,
            attribute = cardEntity.attribute ?: "",
            race = cardEntity.race ?: "",
            atk = cardEntity.atk ?: 0,
            def = cardEntity.def ?: 0,
            desc = cardEntity.desc ?: "",
            card_images = cardImages
        )
    }
}