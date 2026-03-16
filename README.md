<p align="center">
  <img src="kado_icon_1024.png" alt="Kado" width="200" />
</p>

<p align="center">
  <img src="screenshots/home_preview.png" alt="Home" width="180" />
  <img src="screenshots/deck_preview.png" alt="Deck" width="180" />
  <img src="screenshots/review_preview.png" alt="Review" width="180" />
  <img src="screenshots/settings_preview.png" alt="Settings" width="180" />
</p>

# Kado

Kado is an open-source spaced repetition flashcard app built with Kotlin Multiplatform for Android and iOS. It helps you learn and retain knowledge using an SM2-based spaced repetition algorithm, with support for importing Anki decks and syncing to a companion ESP32 hardware device.

## Features

- Create and manage flashcard decks
- Spaced repetition scheduling (SM2-like algorithm)
- Import Anki `.apkg` decks
- Deck partitioning and cloning — split large decks or duplicate them for focused study
- Learning stats — track your review progress and retention over time
- Text-to-speech for card pronunciation (Android only at the moment)
- Dark theme support
- Tablet-friendly layout
- Sync decks to an ESP32 device over WiFi
- Kotlin Multiplatform — shared logic across Android and iOS

## Download

### Stores

<img src="apple_store_badge.svg" width="135" height="40"/>
<img src="google_playstore_badge.svg" width="135" height="40"/>

You can download the latest Android APK from the [Releases](https://github.com/LisandroDiMeo/kado-app/releases) page.

iOS builds require building from source via Xcode (see below).

## Building from Source

### Android

```shell
./gradlew :androidApp:assembleDebug
```

### iOS

Open the `iosApp/` directory in Xcode and run from there.

## Project Structure

| Directory | Purpose |
|---|---|
| `composeApp/` | Shared KMP module (commonMain + platform actuals) |
| `androidApp/` | Android app entry point |
| `iosApp/` | iOS Xcode project |

## ESP32 Hardware Module

Kado supports syncing flashcard decks to an ESP32-based hardware device for offline review. The firmware for this module is kept in a private repository, but we provide an interface definition so anyone can build their own integration with ESP32-like microcontrollers.

### Device Communication Protocol

The device runs a WiFi access point with SSID prefix `KadoLite-`. Once connected, the app communicates via HTTP at `192.168.4.1`:

| Endpoint | Method | Description |
|---|---|---|
| `/api/decks` | GET | List decks on the device |
| `/upload` | POST | Upload a deck file (`.ald` format) |
| `/api/delete?idx=<index>` | DELETE | Delete a deck by index |

### `.ald` Deck Format

The `.ald` file is a simple TSV format where each line represents a card:

```
front_text\tback_text
```

This makes it straightforward to generate deck files from any source and upload them to a compatible device.

## Support the Project

If Kado is useful to you, consider supporting its development:

<p align="center">
  <a href="https://github.com/sponsors/lisandrodimeo">
    <img src="https://img.shields.io/badge/GitHub_Sponsors-E30074?style=for-the-badge&logo=githubsponsors&logoColor=white" alt="GitHub Sponsors" />
  </a>
  <a href="https://ko-fi.com/lisandrodimeo">
    <img src="https://img.shields.io/badge/Ko--fi-FF5E5B?style=for-the-badge&logo=kofi&logoColor=white" alt="Ko-fi" />
  </a>
  <a href="https://cafecito.app/lisandrodimeo">
    <img src="https://img.shields.io/badge/Cafecito-6F4E37?style=for-the-badge&logo=buymeacoffee&logoColor=white" alt="Cafecito" />
  </a>
</p>

## License

This project follows GLP v3. See the [LICENSE](LICENSE) file for details.
