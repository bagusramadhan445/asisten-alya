package com.asistenalya.manager

sealed class ParsedIntent {
    data class OpenApp(val appName: String, val packageName: String) : ParsedIntent()
    data class AskAI(val query: String) : ParsedIntent()
    data class PowerAction(val action: PowerActionType) : ParsedIntent()
}

enum class PowerActionType {
    LOCK_SCREEN,
    POWER_DIALOG
}

object IntentParser {

    private val appCommands = mapOf(
        "wa" to Pair("WhatsApp", "com.whatsapp"),
        "whatsapp" to Pair("WhatsApp", "com.whatsapp"),
        "buka wa" to Pair("WhatsApp", "com.whatsapp"),
        "buka whatsapp" to Pair("WhatsApp", "com.whatsapp"),
        "tt" to Pair("TikTok", "com.zhiliaoapp.musically"),
        "tiktok" to Pair("TikTok", "com.zhiliaoapp.musically"),
        "buka tiktok" to Pair("TikTok", "com.zhiliaoapp.musically"),
        "ig" to Pair("Instagram", "com.instagram.android"),
        "instagram" to Pair("Instagram", "com.instagram.android"),
        "buka ig" to Pair("Instagram", "com.instagram.android"),
        "buka instagram" to Pair("Instagram", "com.instagram.android"),
        "yt" to Pair("YouTube", "com.google.android.youtube"),
        "youtube" to Pair("YouTube", "com.google.android.youtube"),
        "buka youtube" to Pair("YouTube", "com.google.android.youtube"),
        "buka yt" to Pair("YouTube", "com.google.android.youtube"),
        "chrome" to Pair("Chrome", "com.android.chrome"),
        "buka chrome" to Pair("Chrome", "com.android.chrome"),
    )

    private val lockCommands = listOf(
        "matikan layar", "kunci hp", "sleep", "lock", "kunci layar"
    )

    private val powerCommands = listOf(
        "power", "buka power menu", "menu daya", "power menu", "buka menu daya"
    )

    fun parse(input: String): ParsedIntent {
        val normalized = input.trim().lowercase()

        appCommands[normalized]?.let { (name, pkg) ->
            return ParsedIntent.OpenApp(name, pkg)
        }

        for ((key, value) in appCommands) {
            if (normalized.contains(key)) {
                return ParsedIntent.OpenApp(value.first, value.second)
            }
        }

        for (cmd in lockCommands) {
            if (normalized.contains(cmd)) {
                return ParsedIntent.PowerAction(PowerActionType.LOCK_SCREEN)
            }
        }

        for (cmd in powerCommands) {
            if (normalized.contains(cmd)) {
                return ParsedIntent.PowerAction(PowerActionType.POWER_DIALOG)
            }
        }

        return ParsedIntent.AskAI(input)
    }
}
