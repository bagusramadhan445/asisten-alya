# Asisten Alya

An anime futuristic AI assistant Android app powered by Gemini AI.

## Features

- **Gemini AI Chat** — Real-time AI conversation using `gemini-2.0-flash`
- **Voice Assistant** — Speech recognition with Indonesian language support
- **Text-to-Speech** — AI responses spoken aloud
- **Overlay Orb** — Floating orb that works outside the app
- **App Launcher** — Scan and launch installed apps via voice or tap
- **Voice Commands** — Open WhatsApp, TikTok, Instagram, YouTube, Chrome by voice
- **Continuous Listening** — Long-press orb for always-on listening mode
- **Realtime AI State** — Orb animates based on state (READY, LISTENING, THINKING, SPEAKING, ERROR)
- **Power Control** — Lock screen and power dialog via accessibility
- **Premium Anime UI** — Dark navy gradient, sakura pink glow, purple pastel accents

## Tech Stack

- Kotlin + Jetpack Compose
- Material 3
- MVVM + StateFlow + Coroutines
- Navigation Compose
- Retrofit + OkHttp (Gemini API)
- Room Database
- EncryptedSharedPreferences
- Foreground Service + Accessibility Service

## Project Structure

```
com.asistenalya/
├── ai/                  # GeminiProvider
├── data/
│   ├── api/             # GeminiApiService
│   ├── local/           # Room (reserved)
│   └── repository/      # AIRepository
├── domain/
│   ├── model/           # Data models
│   └── usecase/         # Business logic (reserved)
├── manager/             # AssistantStateManager, SpeechManager, TTSManager, etc.
├── service/             # OverlayService, AccessibilityService, DeviceAdmin
├── ui/
│   ├── components/      # GlowingOrb, GlowCard, TypingIndicator
│   ├── screens/         # Onboarding, Home, Chat, Apps, Settings
│   └── theme/           # Colors, Typography, Theme
└── utils/               # SecurePrefs
```

## Setup

1. Open in Android Studio (Hedgehog or later)
2. Sync Gradle
3. Build and run on device/emulator (API 24+)
4. On first launch, enter your [Gemini API key](https://aistudio.google.com/apikey)
5. Tap "Test Connection" to verify
6. Use the orb to start voice commands or open the chat screen

## Permissions

The app requests these permissions at runtime as needed:

| Permission | Purpose |
|---|---|
| `RECORD_AUDIO` | Speech recognition |
| `INTERNET` | Gemini API calls |
| `SYSTEM_ALERT_WINDOW` | Floating overlay orb |
| `FOREGROUND_SERVICE` | Background service |
| `POST_NOTIFICATIONS` | Service notification (Android 13+) |
| `BIND_ACCESSIBILITY_SERVICE` | Power dialog control |

## Voice Commands

| Command | Action |
|---|---|
| "wa", "whatsapp", "buka wa" | Open WhatsApp |
| "tt", "tiktok" | Open TikTok |
| "ig", "instagram" | Open Instagram |
| "yt", "youtube" | Open YouTube |
| "chrome" | Open Chrome |
| "matikan layar", "kunci hp", "sleep" | Lock screen |
| "power", "menu daya" | Open power dialog |
| Any other text | Ask Gemini AI |

## API Key

The app does **not** hardcode any API key. Users enter their own Gemini API key during onboarding, which is stored securely using `EncryptedSharedPreferences`.

## Min SDK

API 24 (Android 7.0 Nougat)
