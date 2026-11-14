package com.kartik.pocketagent

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

object AgentActions {
    fun run(context: Context, raw: String): String {
        val command = raw.trim()
        val lower = command.lowercase()
        return try {
            when {
                lower == "open settings" || lower.contains("phone settings") -> {
                    context.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    "Opening Android Settings."
                }
                lower.contains("wifi") || lower.contains("wi-fi") -> {
                    context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    "Opening Wi-Fi settings. Android requires you to confirm the switch there."
                }
                lower.contains("bluetooth") -> {
                    context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    "Opening Bluetooth settings."
                }
                lower.contains("youtube") -> launchPackage(context, "com.google.android.youtube", "YouTube")
                lower.contains("whatsapp") -> launchPackage(context, "com.whatsapp", "WhatsApp")
                lower.startsWith("search for ") || lower.startsWith("search ") -> {
                    val q = command.substringAfter("search", "").trim()
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + Uri.encode(q)))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    "Searching the web for: $q"
                }
                lower.startsWith("open ") -> {
                    val app = command.substringAfter("open ").trim()
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=" + Uri.encode("open $app")))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    "I couldn't identify an installed app called $app, so I opened a web search."
                }
                else -> "I can currently open YouTube, WhatsApp, Android Settings, Wi-Fi and Bluetooth settings, or search the web. AI chat and more actions are next."
            }
        } catch (e: Exception) {
            "I couldn't complete that action: ${e.message ?: "Android returned an error"}"
        }
    }

    private fun launchPackage(context: Context, pkg: String, label: String): String {
        val intent = context.packageManager.getLaunchIntentForPackage(pkg)
            ?: return "$label does not appear to be installed."
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        return "Opening $label."
    }
}
