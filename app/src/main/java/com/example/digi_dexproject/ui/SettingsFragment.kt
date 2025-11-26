package com.example.digi_dexproject.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import com.example.digi_dexproject.MainActivity
import com.example.digi_dexproject.R
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsFragment : Fragment() {

    companion object {
        const val KEY_NOTIFICATIONS_ENABLED = "notificationsEnabled"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        val prefs = requireActivity().getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)

        val notificationSwitch = view.findViewById<SwitchMaterial>(R.id.notifications_switch)

        val changePasswordButton = view.findViewById<Button>(R.id.button_change_password)
        val logoutButton = view.findViewById<Button>(R.id.button_logout)

        // This block is a button to test the timer. Dont worry about this, once Nick finished the code, i will delete it


        // Find the new test button from the layout
//        val simulateScanButton = view.findViewById<Button>(R.id.button_simulate_scan)
//
//        // Set a listener for the test button
//        simulateScanButton.setOnClickListener {
//            // Call the onCardScanned() function in MainActivity.
//            // This updates the lastScanTime, just as a real scan would.
//            (activity as? MainActivity)?.onCardScanned()
//
//            // Optional: Show a toast message to confirm the action
//            Toast.makeText(requireContext(), "Scan timer reset!", Toast.LENGTH_SHORT).show()
//        }


        //Set the switches initial state based on the saved preferences
        notificationSwitch.isChecked = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, false)

        // Listen for when the user flips the switch
        notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            // 1. Save the new setting immediately
            prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, isChecked).apply()

            // 2. Tell MainActivity to either schedule or cancel the background job
            if (isChecked) {
                (activity as? MainActivity)?.scheduleReminder()
            } else {
                (activity as? MainActivity)?.cancelReminder()
            }
        }

        changePasswordButton.setOnClickListener {
            (activity as? MainActivity)?.loadFragment(ChangePasswordFragment())
        }

        logoutButton.setOnClickListener {
            (activity as? MainActivity)?.logout()
        }

        return view
    }


}