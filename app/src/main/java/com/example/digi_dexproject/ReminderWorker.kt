package com.example.digi_dexproject

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
//import androidx.privacysandbox.tools.core.generator.build //Uncomment this when its not testing
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.digi_dexproject.ui.SettingsFragment
import java.util.concurrent.TimeUnit

class ReminderWorker(appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {

    companion object {
        // A key for SharedPreferences. Used to save and retrieve the timestamp of the last scan.
        const val KEY_LAST_SCAN_TIME = "lastScanTime"

        // A unique ID string for our notification channel. Required for Android 8.0+.
        const val NOTIFICATION_CHANNEL_ID = "scan_reminder_channel"
    }

    //     * All your background logic goes here. This is the way to do it but for testing it will go off every 15 seconds wiht the new function
//    override fun doWork(): Result {
//        val prefs = applicationContext.getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
//
//        // Only proceed if the user has enabled notifications in settings
//        val notificationsEnabled = prefs.getBoolean(SettingsFragment.KEY_NOTIFICATIONS_ENABLED, false)
//        if (!notificationsEnabled) {
//            return Result.success() // Stop work if the user has disabled it
//        }
//
//        val lastScanTime = prefs.getLong(KEY_LAST_SCAN_TIME, 0L)
//
//        // Remind if the user hasn't scanned in 3 days (you can change this)
//        val threeDaysInMillis = TimeUnit.DAYS.toMillis(1)
//        val shouldRemind = (lastScanTime == 0L) || (System.currentTimeMillis() - lastScanTime > threeDaysInMillis)
//
//        if (shouldRemind) {
//            sendNotification()
//        }
//
//        return Result.success()
//    }

    override fun doWork(): Result {

        // --- Step 1: Get our saved settings ---
        val prefs = applicationContext.getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)

        // --- Step 2: The "Guard Clause" - Should we even proceed? ---
        val notificationsEnabled = prefs.getBoolean(SettingsFragment.KEY_NOTIFICATIONS_ENABLED, false)
        if (!notificationsEnabled) {
            return Result.success()
        }

        // --- Step 3: The Core Logic - Is it time to remind the user? ---
        val lastScanTime = prefs.getLong(KEY_LAST_SCAN_TIME, 0L)

        // FOR TESTING: Change the threshold to something very short, like 10 seconds.
        val fiftySecondsInMillis = TimeUnit.SECONDS.toMillis(50) // <--- CHANGE THIS

        val shouldRemind = (lastScanTime == 0L) || (System.currentTimeMillis() - lastScanTime > fiftySecondsInMillis)


        // --- Step 4: Take Action ---
        if (shouldRemind) {
            sendNotification()
        }

        // --- Step 5: RESCHEDULE FOR TESTING ---
        // Create the next work request to run in another 15 seconds.
        val nextWorkRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(15, TimeUnit.SECONDS)
            .build()
        // Enqueue the next run.
        WorkManager.getInstance(applicationContext).enqueueUniqueWork(
            "scanReminderWork", // Must use the same tag
            ExistingWorkPolicy.REPLACE,
            nextWorkRequest
        )


        // --- Step 6: Report Completion ---
        return Result.success()
    }


    private fun sendNotification() {
        createNotificationChannel()

        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Use a real icon
            .setContentTitle("Remember to Scan!")
            .setContentText("It's been a while. Come back and build your collection!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            with(NotificationManagerCompat.from(applicationContext)) {
                val notificationId = System.currentTimeMillis().toInt() // Use a unique ID for each notification
                notify(notificationId, builder.build()) // Use a unique ID for the notification
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Scan Reminders"
            val descriptionText = "Notifications to remind you to scan cards"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
