package com.example.digi_dexproject

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeDatabase()
    }

    private fun initializeDatabase() {
        lifecycleScope.launch(Dispatchers.IO) {
            val cardDao = AppDatabase.getDatabase(applicationContext).cardDao()
            val cardCount = cardDao.countCards()
            Log.d("MainActivity", "Cards in DB: $cardCount")
            if (cardCount == 0) {
                Log.d("MainActivity", "Database is empty. Fetching cards...")
                fetchAndSaveCards()
            } else {
                Log.d("MainActivity", "Database is already populated.")
            }
        }
    }

    private fun fetchAndSaveCards() {
        val apiService = RetrofitClient.getClient().create(ApiService::class.java)
        val call = apiService.getCardsBySet("Magician's Force", "tcg", "1999-01-01", "2011-07-07")

        call.enqueue(object : Callback<CardApiResponse> {
            override fun onResponse(call: Call<CardApiResponse>, response: Response<CardApiResponse>) {
                if (response.isSuccessful) {
                    val cardDataList = response.body()?.data
                    if (cardDataList != null && cardDataList.isNotEmpty()) {
                        Log.d("MainActivity", "Successfully fetched ${cardDataList.size} cards.")
                        lifecycleScope.launch(Dispatchers.IO) {
                            val cardDao = AppDatabase.getDatabase(applicationContext).cardDao()
                            val cardEntities = cardDataList.map { cardData ->
                                CardEntity().apply {
                                    id = cardData.id
                                    name = cardData.name
                                    type = cardData.type
                                    typeline = cardData.typeline
                                    desc = cardData.desc
                                    race = cardData.race
                                    atk = if (cardData.atk != 0) cardData.atk else null
                                    def = if (cardData.def != 0) cardData.def else null
                                    level = if (cardData.level != 0) cardData.level else null
                                    attribute = cardData.attribute
                                    ygoprodeckUrl = cardData.ygoprodeckUrl
                                    imageUrl = cardData.cardImages?.firstOrNull()?.imageUrl
                                    tcgplayerPrice = cardData.cardPrices?.firstOrNull()?.tcgplayerPrice
                                }
                            }
                            cardDao.insertAll(cardEntities)
                            Log.d("MainActivity", "Data saved to database successfully.")
                        }
                    } else {
                        Log.w("MainActivity", "API response was successful but the data list is null or empty.")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("MainActivity", "API call failed with code ${response.code()}: $errorBody")
                }
            }

            override fun onFailure(call: Call<CardApiResponse>, t: Throwable) {
                Log.e("MainActivity", "Failed to fetch data: ${t.message}", t)
            }
        })
    }
}
