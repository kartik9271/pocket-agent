# Pocket Agent (starter v0.1)

A native Android assistant starter: text commands, speech input, speech output, app launching, Android settings shortcuts, and an optional floating launcher button.

## Current scope
- Opens YouTube and WhatsApp when installed.
- Opens Android Settings, Wi-Fi settings and Bluetooth settings.
- Performs web searches.
- Uses Android speech recognition and text-to-speech.
- Floating overlay button returns to the app.
- GitHub Actions workflow builds a debug APK.

## Important limitations
This is an early starter, not a full AI agent yet. Gemini chat/API integration, persistent memory, notification access, robust app discovery, and advanced automation are not implemented yet. The floating button is a launcher shortcut, not a listening always-on assistant. Android does not permit silently toggling many protected settings.

## Build APK
Open the repository's Actions tab and run **Build Pocket Agent APK** (also runs automatically on pushes to main). Download the `pocket-agent-debug-apk` artifact from a successful run, extract it and install `app-debug.apk`.

## Requirements
- Android 8.0 (API 26) or newer.
- No API key is required for the current local command prototype.

## Security
Never commit a Gemini API key or other secrets to this repository. The next version should accept the key locally or use a protected backend. A Gemini subscription does not automatically include Gemini API usage; API access and billing are separate.

## Repository status
Starter source and automated APK build workflow are configured in this repository.
