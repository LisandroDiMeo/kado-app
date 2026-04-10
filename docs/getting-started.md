# Getting Started with Kado

Kado is a spaced repetition flashcard app for Android and iOS. This guide walks you through installation, creating your first deck, and reviewing cards.

## Installation

### Android

<a href="https://play.google.com/store/apps/details?id=com.eldiem.kado.app">
<img src="../google_playstore_badge.svg" width="135" height="40"/>
</a>

You can also download the latest APK from the [Releases](https://github.com/LisandroDiMeo/kado-app/releases) page.

### iOS

iOS support is coming soon. In the meantime, you can build from source by opening the `iosApp/` directory in Xcode. See the [README](../README.md#building-from-source) for build instructions.

## Creating Your First Deck

1. Open Kado and tap the **+** button on the home screen.
2. Enter a **deck name** (e.g., "Spanish Vocabulary").
3. Set the **daily new card limit** — this controls how many unseen cards are introduced each day.
4. Choose a **scheduling algorithm**:
   - **SM2** — A well-established algorithm. Good default for most users.
   - **FSRS** — A more modern, evidence-based algorithm with fine-tunable parameters. See the [Features](features.md#spaced-repetition-algorithms) page for details.
5. Tap **Save**.

You can change the algorithm and other deck settings at any time from the deck detail screen.

## Adding Cards

1. Open a deck and tap the **+** button to add a new card.
2. Enter the **front** (question/prompt) and **back** (answer) content.
3. Tap **Save**.

### Card Content Types

- **Plain text** — Just type normally. Best for simple vocabulary, definitions, etc.
- **Rich text / HTML** — Cards support HTML formatting including bold, italic, underline, headings, colored text, and lists. This is particularly useful for imported Anki decks that use HTML styling.
- **Images** — You can attach images to cards. Images appear inline with the card text.

## Reviewing Cards

1. From the deck detail screen, tap **Review** to start a study session.
2. The card shows the **front side** first.
3. **Tap** the card or **swipe vertically** to flip it and reveal the answer.
4. Rate how well you recalled the answer using the four buttons:

| Rating | Meaning | Effect on Scheduling |
|--------|---------|---------------------|
| **Again** | Forgot the answer | Card resets and appears again soon |
| **Hard** | Recalled with significant difficulty | Interval increases slightly |
| **Good** | Recalled correctly with moderate effort | Standard interval increase |
| **Easy** | Recalled instantly | Interval increases more aggressively |

Each button shows the **next review interval** so you can see when the card will appear again.

The session ends when all due cards have been reviewed. A summary screen shows the total cards reviewed and a breakdown of your ratings.

## Importing Anki Decks

Kado can import Anki `.apkg` deck files, including their card templates and media (images).

1. On the home screen, tap **Import**.
2. Select an `.apkg` file from your device.
3. Kado processes the file in several phases:
   - **Extracting** — Unpacking the `.apkg` archive
   - **Parsing** — Reading the Anki database and rendering card templates
   - **Inserting** — Saving cards to the local database
   - **Extracting media** — Pulling out referenced images
4. Once complete, the imported deck appears on the home screen.

Anki's template syntax (field substitution, conditionals, etc.) is fully supported. See [Anki Import](features.md#anki-import) in the feature reference for details.

## Customizing Settings

Access settings from the menu on the home screen.

| Setting | Options | Description |
|---------|---------|-------------|
| **Theme** | System, Light, Dark | Controls the app's color scheme |
| **Card Font Scale** | Slider | Adjusts the size of text displayed on flashcards |
| **App Font Scale** | Slider | Adjusts the size of text throughout the app UI |
| **Language** | English, Spanish | Changes the app's interface language |
