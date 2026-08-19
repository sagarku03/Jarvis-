package com.example.core.automation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings

data class AppProfile(
    val id: String,
    val name: String,
    val packageName: String,
    val searchUriTemplate: String? = null,
    val deepLinkPrefix: String? = null,
    val aliases: List<String> = emptyList()
)

object AppCapabilityManager {

    private val supportedApps = listOf(
        AppProfile(
            id = "youtube",
            name = "YouTube",
            packageName = "com.google.android.youtube",
            searchUriTemplate = "https://www.youtube.com/results?search_query=%s",
            deepLinkPrefix = "vnd.youtube:",
            aliases = listOf("youtube", "yt", "videos", "video", "ytube")
        ),
        AppProfile(
            id = "maps",
            name = "Google Maps",
            packageName = "com.google.android.apps.maps",
            searchUriTemplate = "geo:0,0?q=%s",
            aliases = listOf("maps", "google maps", "directions", "navigation", "map")
        ),
        AppProfile(
            id = "spotify",
            name = "Spotify",
            packageName = "com.spotify.music",
            searchUriTemplate = "spotify:search:%s",
            aliases = listOf("spotify", "music", "songs")
        ),
        AppProfile(
            id = "chrome",
            name = "Google Chrome",
            packageName = "com.android.chrome",
            searchUriTemplate = "https://www.google.com/search?q=%s",
            aliases = listOf("chrome", "browser", "internet", "google")
        ),
        AppProfile(
            id = "whatsapp",
            name = "WhatsApp",
            packageName = "com.whatsapp",
            searchUriTemplate = null,
            aliases = listOf("whatsapp", "wa", "what's app")
        ),
        AppProfile(
            id = "instagram",
            name = "Instagram",
            packageName = "com.instagram.android",
            aliases = listOf("instagram", "insta", "ig")
        ),
        AppProfile(
            id = "camera",
            name = "Camera",
            packageName = "com.google.android.GoogleCamera",
            aliases = listOf("camera", "cam", "photo", "take picture")
        ),
        AppProfile(
            id = "settings",
            name = "Settings",
            packageName = "com.android.settings",
            aliases = listOf("settings", "phone settings", "configuration")
        ),
        AppProfile(
            id = "gmail",
            name = "Gmail",
            packageName = "com.google.android.gm",
            aliases = listOf("gmail", "email", "mail")
        ),
        AppProfile(
            id = "calculator",
            name = "Calculator",
            packageName = "com.google.android.calculator",
            aliases = listOf("calculator", "calc")
        )
    )

    fun findApp(query: String): AppProfile? {
        val clean = query.trim().lowercase()
        return supportedApps.find { profile ->
            profile.name.lowercase() == clean ||
            profile.id.lowercase() == clean ||
            profile.aliases.any { clean.contains(it) }
        }
    }

    fun launchApp(context: Context, packageName: String): Boolean {
        return try {
            val pm = context.packageManager
            val intent = pm.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    fun searchWithinApp(context: Context, appProfile: AppProfile, query: String): Boolean {
        return try {
            if (appProfile.id == "youtube") {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=${Uri.encode(query)}")).apply {
                    setPackage(appProfile.packageName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                return true
            }

            if (appProfile.id == "maps") {
                val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(query)}")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                    setPackage(appProfile.packageName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(mapIntent)
                return true
            }

            if (appProfile.id == "spotify") {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("spotify:search:${Uri.encode(query)}")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                return true
            }

            if (appProfile.id == "chrome" || appProfile.searchUriTemplate != null) {
                val uri = Uri.parse(appProfile.searchUriTemplate!!.format(Uri.encode(query)))
                val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                return true
            }

            launchApp(context, appProfile.packageName)
        } catch (e: Exception) {
            // Fallback: general web search
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=${Uri.encode(query)}")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        }
    }
}
