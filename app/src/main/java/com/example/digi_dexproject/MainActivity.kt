package com.example.digi_dexproject

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.digi_dexproject.ui.AuthFragment
import com.example.digi_dexproject.ui.HomeFragment
import com.example.digi_dexproject.ui.MapFragment
import com.example.digi_dexproject.ui.ScanFragment
import com.example.digi_dexproject.ui.SettingsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.work.*
import java.util.concurrent.TimeUnit
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import com.example.digi_dexproject.ui.ProfileFragment


class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView
    private var isDatabaseInitialized = false // <-- ADD THIS FLAG
    private lateinit var prefs: SharedPreferences

    private val reminderWorkTag = "scanReminderWork"

    companion object {
        const val PREFS_NAME = "DigiDexPrefs"
        const val KEY_IS_LOGGED_IN = "isLoggedIn"
        const val KEY_USERNAME = "username"
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted. Now we can schedule the work.
            scheduleReminder()
        } else {
            // Permission denied. You might want to inform the user or disable the switch.
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        bottomNavigation = findViewById(R.id.bottom_navigation)

        if (isLoggedIn()) {
            setupMainAppUI()
        } else {
            setupAuthUI()
        }
    }

    private fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    private fun setupAuthUI() {
        bottomNavigation.visibility = View.GONE
        loadFragment(AuthFragment())
    }

    private fun setupMainAppUI() {
        bottomNavigation.visibility = View.VISIBLE
        bottomNavigation.setOnNavigationItemSelectedListener {
            var selectedFragment: Fragment = HomeFragment()
            when (it.itemId) {
                R.id.navigation_home -> selectedFragment = HomeFragment()
                R.id.navigation_scan -> selectedFragment = ScanFragment()
                R.id.navigation_map -> selectedFragment = MapFragment()
                R.id.navigation_profile -> selectedFragment = ProfileFragment()
                R.id.navigation_settings -> selectedFragment = SettingsFragment()
            }
            loadFragment(selectedFragment)
            true
        }

        // Load the default fragment
        loadFragment(HomeFragment())
    }

    fun onLoginSuccess(username: String) {
        // Save login state
        with(prefs.edit()) {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putString(KEY_USERNAME, username)
            apply()
        }
        // Transition to the main app
        setupMainAppUI()
    }

    fun logout() {
        // Clear login state
        with(prefs.edit()) {
            clear()
            apply()
        }
        // Transition to the auth screen
        setupAuthUI()
    }

    // This function is now public to be callable by our auth fragments
    fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()

        // Only initialize the card database if we are loading the HomeFragment
        if (fragment is HomeFragment && !isDatabaseInitialized) {
            initializeDatabase()
        }
    }

    private fun initializeDatabase() {
        isDatabaseInitialized = true // Set the flag immediately to break any loops
        lifecycleScope.launch(Dispatchers.IO) {
            val cardDao = AppDatabase.getDatabase(applicationContext).cardDao()
            val cardCount = cardDao.countCards()
            Log.d("MainActivity", "Cards in DB: $cardCount")
            if (cardCount == 0) {
                Log.d("MainActivity", "Database is empty. Fetching cards...")
                fetchAndSaveCards()
            } else {
                importMissingDeckCards()
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
                                    val mainImage = cardData.cardImages?.firstOrNull()

                                    imageUrl = cardData.cardImages?.firstOrNull()?.imageUrl
                                    imageUrlSmall = cardData.cardImages?.firstOrNull()?.imageUrlSmall
                                }
                            }
                            cardDao.insertAll(cardEntities)
                            Log.d("MainActivity", "Data saved to database successfully.")
//                            runOnUiThread {
//                                loadFragment(HomeFragment())
//                            }
                            importMissingDeckCards()
                            scheduleReminder()

                            // Schedule the reminder

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

    private fun importMissingDeckCards() {
        val apiService = RetrofitClient.getClient().create(ApiService::class.java)

        lifecycleScope.launch(Dispatchers.IO) {
            val cardDao = AppDatabase.getDatabase(applicationContext).cardDao()
            val existingNames = cardDao.getExistingNames(myCardNames)
            val missingNames = myCardNames.filter { it !in existingNames }

            if (missingNames.isEmpty()) {
                Log.d("IMPORT", "No deck cards missing. Finalizing.")
                // Finalize the process here as well
                scheduleReminder()
                runOnUiThread { loadFragment(HomeFragment()) }
                return@launch
            }

            Log.d("IMPORT", "Missing ${missingNames.size} cards → Fetching all in one batch.")

            try {
                // Join the names into a single string for the API
                val namesQuery = missingNames.joinToString("|")

                // Use .execute() for a direct, sequential call inside the coroutine
                val response = apiService.getCardsByNames(namesQuery).execute()

                if (response.isSuccessful) {
                    val cardDataList = response.body()?.data
                    if (cardDataList != null && cardDataList.isNotEmpty()) {
                        Log.d("IMPORT", "Successfully fetched ${cardDataList.size} missing cards.")

                        val cardEntities = cardDataList.map { cardData ->
                            CardEntity().apply {
                                id = cardData.id
                                this.name = cardData.name
                                type = cardData.type
                                typeline = cardData.typeline
                                desc = cardData.desc
                                race = cardData.race
                                atk = if (cardData.atk != 0) cardData.atk else null
                                def = if (cardData.def != 0) cardData.def else null
                                level = if (cardData.level != 0) cardData.level else null
                                attribute = cardData.attribute
                                imageUrl = cardData.cardImages?.firstOrNull()?.imageUrl
                                imageUrlSmall = cardData.cardImages?.firstOrNull()?.imageUrlSmall
                            }
                        }
                        cardDao.insertAll(cardEntities)
                        Log.d("IMPORT", "All missing cards saved to database.")
                    }
                } else {
                    Log.e("IMPORT", "Batch fetch failed with code: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("IMPORT", "Batch fetch failed entirely: ${e.message}", e)
            }

            // --- FINALIZATION ---
            // This is the single, reliable end-point for the entire process.
            Log.d("IMPORT", "Process complete. Scheduling reminder and refreshing UI.")
            scheduleReminder()
            runOnUiThread {
                loadFragment(HomeFragment())
            }
        }
    }






    fun scheduleReminder() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return
            }
        }

        val reminderWorkRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(15, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(this).enqueueUniqueWork(
            reminderWorkTag,
            ExistingWorkPolicy.REPLACE,
            reminderWorkRequest
        )
    }

    fun cancelReminder() {
        WorkManager.getInstance(this).cancelUniqueWork(reminderWorkTag)
    }

    fun onCardScanned() {
        prefs.edit().putLong(ReminderWorker.KEY_LAST_SCAN_TIME, System.currentTimeMillis()).apply()
        bottomNavigation.selectedItemId = R.id.navigation_profile
    }

    private val myCardNames = listOf(
        "Summoned Skull",
        "Giant Soldier of Stone",
        "Infinite Cards",
        "Mad Dog of Darkness",
        "Royal Decree",
        "Luster Dragon",
        "Alpha the Magnet Warrior",
        "Gamma the Magnet Warrior",
        "Beta the Magnet Warrior",
        "Valkyrion the Magna Warrior",
        "Millennium Shield",
        "Royal Magical Library",
        "Spellbinding Circle",
        "Trance the Magic Swordsman",
        "Swords of Revealing Light",
        "Slifer the Sky Dragon",
        "Sabersaurus",
        "Spirit of the Harp",
        "Soul Taker",
        "Sakuretsu Armor",
        "Mystical Elf",
        "Catapult Turtle",
        "Pot of Duality",
        "Heart of the Underdog",
        "Bottomless Trap Hole",
        "Supply Squad",
        "Final Destiny",
        "Gold Sarcophagus",
        "Vorse Raider",
        "Chamberlain of the Six Samurai",
        "Aqua Madoor",
        "Fissure",
        "Left Arm of the Forbidden One",
        "Right Arm of the Forbidden One",
        "Left Leg of the Forbidden One",
        "Right Leg of the Forbidden One",
        "Exodia the Forbidden One",
        "Dark Magician",
        "Dark Magician Girl",
        "Breaker the Magical Warrior",
        "Skilled Dark Magician",
        "Mystical Space Typhoon",
        "Sorcerer of Dark Magic",
        "Dark Magic Attack",
        "Emblem of Dragon Destroyer",
        "Sage’s Stone",
        "Defender, the Magical Knight",
        "Raigeki Break",
        "Magician’s Circle",
        "Gagaga Magician",
        "Magician of Black Chaos",
        "Black Illusion",
        "Dark Magic Curtain",
        "Call of the Haunted",
        "Polymerization",
        "Endymion, the Master Magician",
        "Magic Cylinder",
        "Summoner Monk",
        "Thousand Knives",
        "Mirror Force",
        "Magical Dimension",
        "Torrential Tribute",
        "Old Vindictive Magician",
        "Black Magic Ritual",
        "Silent Magician LV4",
        "Silent Magician LV8",
        "Buster Blader",
        "Level Lifter"
    )




}


