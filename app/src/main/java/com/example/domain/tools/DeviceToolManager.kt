package com.example.domain.tools

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.provider.AlarmClock
import android.widget.Toast
import com.example.domain.model.DeviceAction

class DeviceToolManager(private val context: Context) {

    private val knownAppPackages = mapOf(
        "youtube" to "com.google.android.youtube",
        "chrome" to "com.android.chrome",
        "browser" to "com.android.chrome",
        "maps" to "com.google.android.apps.maps",
        "google maps" to "com.google.android.apps.maps",
        "camera" to "com.android.camera",
        "clock" to "com.google.android.deskclock",
        "alarm" to "com.google.android.deskclock",
        "calculator" to "com.google.android.calculator",
        "calendar" to "com.google.android.calendar",
        "settings" to "com.android.settings",
        "gmail" to "com.google.android.gm",
        "spotify" to "com.spotify.music",
        "play store" to "com.android.vending",
        "photos" to "com.google.android.apps.photos"
    )

    private var isTorchOn = false

    fun executeAction(action: DeviceAction): String {
        return when (action) {
            is DeviceAction.OpenApp -> openApp(action.appName, action.packageName)
            is DeviceAction.SearchWeb -> searchWeb(action.query)
            is DeviceAction.SetAlarm -> setAlarm(action.hour, action.minute, action.label)
            is DeviceAction.SetTimer -> setTimer(action.durationSeconds, action.label)
            is DeviceAction.LaunchMaps -> launchMaps(action.location)
            is DeviceAction.ToggleFlashlight -> toggleFlashlight(action.enable)
            is DeviceAction.DialPhone -> dialPhone(action.numberOrName)
            is DeviceAction.ComposeMessage -> composeMessage(action.recipient, action.text)
            is DeviceAction.StoreMemory -> "Memory saved successfully."
            is DeviceAction.ClearHistory -> "History cleared."
            is DeviceAction.GeneralResponse -> action.text
        }
    }

    private fun openApp(appName: String, explicitPackage: String?): String {
        val pm = context.packageManager
        val cleanName = appName.lowercase().trim()

        // Try explicit package or known mapping
        val pkg = explicitPackage ?: knownAppPackages[cleanName]
        if (pkg != null) {
            val launchIntent = pm.getLaunchIntentForPackage(pkg)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                return "Opening $appName, sir."
            }
        }

        // Search through all installed launchable applications
        try {
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val apps = pm.queryIntentActivities(mainIntent, 0)
            val match = apps.firstOrNull {
                val label = it.loadLabel(pm).toString().lowercase()
                label.contains(cleanName) || cleanName.contains(label)
            }
            if (match != null) {
                val targetIntent = pm.getLaunchIntentForPackage(match.activityInfo.packageName)
                if (targetIntent != null) {
                    targetIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(targetIntent)
                    return "Opening ${match.loadLabel(pm)}, sir."
                }
            }
        } catch (e: Exception) {
            // fallback
        }

        // Special system handlers
        if (cleanName.contains("camera")) {
            val cameraIntent = Intent("android.media.action.IMAGE_CAPTURE").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (cameraIntent.resolveActivity(pm) != null) {
                context.startActivity(cameraIntent)
                return "Accessing optical feed, sir."
            }
        }

        if (cleanName.contains("setting")) {
            val settingsIntent = Intent(android.provider.Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(settingsIntent)
            return "Opening system settings, sir."
        }

        return "I could not locate an application named '$appName' on this device, sir."
    }

    private fun searchWeb(query: String): String {
        try {
            val searchIntent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                putExtra(SearchManager.QUERY, query)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (searchIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(searchIntent)
                return "Searching the global network for '$query', sir."
            }

            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.google.com/search?q=" + Uri.encode(query))
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(browserIntent)
            return "Launching browser query for '$query', sir."
        } catch (e: Exception) {
            return "Network query failed: ${e.localizedMessage}"
        }
    }

    private fun setAlarm(hour: Int, minute: Int, label: String): String {
        try {
            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, hour)
                putExtra(AlarmClock.EXTRA_MINUTES, minute)
                putExtra(AlarmClock.EXTRA_MESSAGE, label.ifBlank { "JARVIS Reminder" })
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            val timeFormatted = String.format("%02d:%02d", hour, minute)
            return "Alarm initialized for $timeFormatted, sir."
        } catch (e: Exception) {
            return "Unable to set alarm automatically: ${e.localizedMessage}"
        }
    }

    private fun setTimer(durationSeconds: Int, label: String): String {
        try {
            val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                putExtra(AlarmClock.EXTRA_LENGTH, durationSeconds)
                putExtra(AlarmClock.EXTRA_MESSAGE, label.ifBlank { "JARVIS Timer" })
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            val mins = durationSeconds / 60
            val secs = durationSeconds % 60
            val dur = if (mins > 0) "$mins minute${if (mins > 1) "s" else ""}" else "$secs seconds"
            return "Countdown timer for $dur has been activated, sir."
        } catch (e: Exception) {
            return "Unable to initialize timer: ${e.localizedMessage}"
        }
    }

    private fun launchMaps(location: String): String {
        try {
            val gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(location))
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                setPackage("com.google.android.apps.maps")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (mapIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(mapIntent)
                return "Plotting navigation coordinates to $location, sir."
            }

            val fallbackIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(location))
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallbackIntent)
            return "Accessing satellite mapping for $location, sir."
        } catch (e: Exception) {
            return "Navigation uplink failed: ${e.localizedMessage}"
        }
    }

    private fun toggleFlashlight(enable: Boolean): String {
        try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
                ?: return "Hardware sensor unavailable."
            val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                val characteristics = cameraManager.getCameraCharacteristics(id)
                characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            } ?: return "Flashlight hardware not found on device."

            cameraManager.setTorchMode(cameraId, enable)
            isTorchOn = enable
            return if (enable) "Illumination engaged, sir." else "Illumination powered down, sir."
        } catch (e: CameraAccessException) {
            return "Hardware bus error: ${e.localizedMessage}"
        } catch (e: Exception) {
            return "Could not adjust torch: ${e.localizedMessage}"
        }
    }

    private fun dialPhone(numberOrName: String): String {
        try {
            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:" + Uri.encode(numberOrName))
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(dialIntent)
            return "Opening communications channel for $numberOrName, sir."
        } catch (e: Exception) {
            return "Failed to open phone dialer: ${e.localizedMessage}"
        }
    }

    private fun composeMessage(recipient: String, text: String): String {
        try {
            val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:" + Uri.encode(recipient))
                putExtra("sms_body", text)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(smsIntent)
            return "Drafting dispatch to $recipient, sir."
        } catch (e: Exception) {
            return "Failed to open messaging console: ${e.localizedMessage}"
        }
    }
}
