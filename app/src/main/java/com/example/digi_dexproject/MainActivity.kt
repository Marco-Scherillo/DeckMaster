package com.example.digi_dexproject

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.digi_dexproject.ui.HomeFragment
import com.example.digi_dexproject.ui.MapFragment
import com.example.digi_dexproject.ui.ScanFragment
import com.example.digi_dexproject.ui.SettingsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

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

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
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

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}