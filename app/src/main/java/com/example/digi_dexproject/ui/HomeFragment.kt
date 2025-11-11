package com.example.digi_dexproject.ui

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
import com.example.digi_dexproject.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var cardAdapter: CardAdapter
    private lateinit var searchBar: EditText
    private var allCards: List<Card> = emptyList()

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

        loadCardsFromDatabase()
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
        cardAdapter = CardAdapter(filteredList) { card ->
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CardDetailFragment.newInstance(card))
                .addToBackStack(null)
                .commit()
        }
        recyclerView.adapter = cardAdapter
    }

    private fun loadCardsFromDatabase() {
        lifecycleScope.launch(Dispatchers.IO) {
            val cardDao = AppDatabase.getDatabase(requireContext()).cardDao()
            val cardEntities = cardDao.getAll()
            allCards = cardEntities.map { toCardData(it) }

            withContext(Dispatchers.Main) {
                cardAdapter = CardAdapter(allCards) { card ->
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, CardDetailFragment.newInstance(card))
                        .addToBackStack(null)
                        .commit()
                }
                recyclerView.adapter = cardAdapter
            }
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