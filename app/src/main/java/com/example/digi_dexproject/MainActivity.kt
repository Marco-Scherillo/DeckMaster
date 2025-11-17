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


class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView
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
        if (fragment is HomeFragment) {
            initializeDatabase()
        }
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
                                    val mainImage = cardData.cardImages?.firstOrNull()

                                    imageUrl = cardData.cardImages?.firstOrNull()?.imageUrl
                                    imageUrlSmall = cardData.cardImages?.firstOrNull()?.imageUrlSmall
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

    //The way we will do it, reminds once a day, but for testing the other one will be every 15 seconds
//    fun scheduleReminder() {
//        // First, check for notification permission on Android 13 (API 33) or higher
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
//                // If permission is not granted, launch the permission request dialog
//                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
//                return // Exit the function; the rest of the code will run if the user grants permission
//            }
//        }
//
//        // Set up the recurring work request to run roughly once a day
//        val reminderWorkRequest = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS).build()
//
//        // Enqueue the work with a unique tag, keeping existing work if it's already scheduled
//        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
//            reminderWorkTag,
//            ExistingPeriodicWorkPolicy.KEEP,
//            reminderWorkRequest
//        )
//    }

    fun scheduleReminder() {
        // First, check for notification permission on Android 13 (API 33) or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // If permission is not granted, launch the permission request dialog
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                return // Exit the function; the rest of the code will run if the user grants permission
            }
        }

        // --- THIS IS THE MODIFIED PART FOR TESTING ---

        // Instead of a periodic request, create a One-Time request.
        // We set an initial delay of 15 seconds to give you time to close the app.
        val reminderWorkRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(15, TimeUnit.SECONDS)
            .build()

        // Enqueue the unique work.
        // IMPORTANT: Change the policy to REPLACE. This ensures that when we schedule the "next" test,
        // it replaces the old one, creating a repeating effect.
        WorkManager.getInstance(this).enqueueUniqueWork(
            reminderWorkTag,
            ExistingWorkPolicy.REPLACE, // Use REPLACE for testing
            reminderWorkRequest
        )
    }


    fun cancelReminder() {
        WorkManager.getInstance(this).cancelUniqueWork(reminderWorkTag)
    }

    // Call this function whenever a user successfully scans a card
    fun onCardScanned() {
        prefs.edit().putLong(ReminderWorker.KEY_LAST_SCAN_TIME, System.currentTimeMillis()).apply()
    }


}