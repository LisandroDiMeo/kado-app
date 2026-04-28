@file:Suppress("ktlint:standard:max-line-length")

package com.kado.app.presentation.localization

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import com.kado.app.domain.model.AppLanguage

val LocalAppLanguage = compositionLocalOf { AppLanguage.English }

@Composable
fun S(): AppStrings = when (LocalAppLanguage.current) {
    AppLanguage.English -> EnStrings
    AppLanguage.Spanish -> EsStrings
}

interface AppStrings {
    // Common
    val appName: String
    val cancel: String
    val confirm: String
    val delete: String
    val remove: String
    val clone: String
    val edit: String
    val done: String
    val retry: String
    val save: String
    val back: String
    val next: String

    // Home
    val noDecksTitle: String
    val noDecksSubtitle: String
    val deleteDeck: String
    val deleteDeckMessage: String
    val importApkg: String
    val createDeck: String

    // Deck Detail
    fun reviewCount(count: Int): String
    val stats: String
    val transferToDevice: String
    val partitionDeck: String
    val subDecks: String
    fun partLabel(index: Int): String
    fun cardStats(cards: Int, due: Int, new: Int): String
    val review: String
    val send: String
    fun cardsCount(count: Int): String
    val noCardsTitle: String
    val noCardsSubtitle: String
    val removeSubDeck: String
    val removeSubDeckMessage: String
    val cloneSubDeck: String
    val cloneSubDeckMessage: String
    val createReversedDeck: String
    val createReversedDeckMessage: String

    // Review
    val sessionComplete: String
    fun reviewedCount(count: Int): String
    fun againHardCount(again: Int, hard: Int): String
    fun goodEasyCount(good: Int, easy: Int): String
    val again: String
    val hard: String
    val good: String
    val easy: String

    // Deck Edit
    val newDeck: String
    val editDeck: String
    val deckName: String
    val deckNamePlaceholder: String
    val dailyNewCardLimit: String
    val saveChanges: String
    val deleteDeckEditMessage: String
    val deleteDeckButton: String

    // Card Edit
    val newCard: String
    val editCard: String
    val addCard: String
    val saveCard: String
    val deleteCard: String
    val deleteCardMessage: String
    val front: String
    val backSide: String
    val questionPlaceholder: String
    val answerPlaceholder: String

    // Stats
    val statistics: String
    val resetProgress: String
    val resetProgressMessage: String
    val resetCardProgressMessage: String
    val reset: String
    val statsGuide: String
    val statsGuideBodySm2: String
    val statsGuideBodyFsrs: String
    val gotIt: String
    val newLabel: String
    val learning: String
    val young: String
    val mature: String
    fun dueNow(count: Int): String
    fun totalCards(count: Int): String

    // Connection
    val deviceConnection: String
    val connectionDescription: String
    val deviceSsid: String
    val ssidPlaceholder: String
    val connect: String
    val connecting: String
    val connected: String
    val disconnect: String

    // Transfer
    val transfer: String
    val uploadToDevice: String
    val uploading: String
    val uploadSuccessful: String
    val deviceDecks: String
    fun freeMb(mb: Int): String

    // Partition
    val batch: String
    val manual: String
    val existingPartitionsWarning: String
    val cardsPerSubDeck: String
    fun subDeckSummary(count: Int, batchSize: Int, lastSize: Int): String
    val partition: String
    val partitioning: String
    val saving: String

    // Settings
    val settings: String
    val about: String
    val appSettings: String
    val donate: String
    fun versionLabel(version: String): String
    val theme: String
    val system: String
    val light: String
    val dark: String
    val cardFontSize: String
    val appFontSize: String
    val language: String

    // About
    val aboutDescription: String
    val kadoLite: String
    val kadoLiteDescription: String
    val contribute: String
    val contributeDescription: String
    val builtWith: String
    val builtWithDescription: String

    // Donate
    val supportKado: String
    val donateDescription: String
    val donateDisclaimer: String

    // Import
    val importComplete: String
    val importFailed: String
    val importing: String
    val extracting: String
    val readingAnkiDb: String
    fun importingCards(count: Int): String
    val processing: String
    val unknownError: String
    fun importedCards(count: Int, deckName: String): String
    val dismiss: String

    // Images
    val images: String
    val noImagesAttached: String
    val addImage: String

    // Help
    val help: String
    val howToUseApp: String
    val howToUseKadoLite: String
    val tutorialCreateDeckTitle: String
    val tutorialCreateDeckDesc: String
    val tutorialAddCardsTitle: String
    val tutorialAddCardsDesc: String
    val tutorialImportAnkiTitle: String
    val tutorialImportAnkiDesc: String
    val tutorialReviewCardsTitle: String
    val tutorialReviewCardsDesc: String
    val tutorialRateRecallTitle: String
    val tutorialRateRecallDesc: String
    val tutorialPowerOnTitle: String
    val tutorialPowerOnDesc: String
    val tutorialConnectTitle: String
    val tutorialConnectDesc: String
    val tutorialTransferDeckTitle: String
    val tutorialTransferDeckDesc: String
    val tutorialReviewDeviceTitle: String
    val tutorialReviewDeviceDesc: String
    val tutorialSyncBackTitle: String
    val tutorialSyncBackDesc: String

    // Tutorial Dialog
    fun stepOf(current: Int, total: Int): String

    // DeckCard
    fun dueCount(count: Int): String
    fun newCount(count: Int): String

    // Algorithm / Scheduler
    val scheduler: String
    val schedulerSm2: String
    val schedulerFsrs: String
    val sm2Algorithm: String
    val fsrsAlgorithm: String

    // FSRS Settings
    val fsrsSettings: String
    val desiredRetention: String
    val desiredRetentionHint: String
    val learningSteps: String
    val learningStepsHint: String
    val relearningSteps: String
    val relearningStepsHint: String
    val maxInterval: String
    val maxIntervalHint: String
    val enableFuzzing: String
    val enableFuzzingHint: String
    val resetToDefaults: String

    // SM-2 Parameters
    val ease: String
    val easeDescription: String
    val easeHint: String

    // Algorithm Explanations
    val sm2SimpleExplanation: String
    val fsrsSimpleExplanation: String
    val sm2TechnicalDetails: String
    val fsrsTechnicalDetails: String

    // Algorithm Parameter Explanations
    val desiredRetentionExplanation: String
    val learningStepsExplanation: String
    val relearningStepsExplanation: String
    val maxIntervalExplanation: String
    val enableFuzzingExplanation: String

    // Algorithm Detail UI
    val howItWorks: String
    val technicalDetails: String
    val parameters: String

    // Bulk select / delete
    val selectCards: String
    fun selectedCount(count: Int): String
    val deleteSelected: String
    fun deleteSelectedMessage(count: Int): String
    val updateFromApkg: String

    // Deck patch / update
    val updateDeckTitle: String
    fun patchNeedsBackfillMessage(nullCount: Int): String
    val patchReimportOriginal: String
    fun patchBackfillResult(matched: Int, unmatched: Int): String
    val patchContinue: String
    val patchInvalidApkg: String
    val patchReadDbFailed: String
    val patchNoCards: String
    val patchParseFailed: String
    val patchClose: String
    val patchSessionMissing: String
    val patchNoChangesTitle: String
    val patchNoChangesMessage: String
    val patchPreviewIntro: String
    fun patchAddedSection(count: Int): String
    fun patchModifiedSection(count: Int): String
    fun patchRemovedSection(count: Int): String
    val patchKeepHint: String
    fun patchMoreItems(count: Int): String
    fun patchLoadMoreLeft(count: Int): String
    fun patchKeptCount(count: Int): String
    val patchApply: String
    val patchApplying: String
    val patchDiscard: String
    val patchBefore: String
    val patchAfter: String
    val patchWillKeep: String
    val patchWillDelete: String
    val patchParsing: String
    val patchBackfilling: String

    // Bulk Edit
    val bulkEdit: String
    val cardFrontRule: String
    val cardBackRule: String
    val findPattern: String
    val replacePattern: String
    val isRegex: String
    val previewChanges: String
    val saveRule: String
    val savedRules: String
    val commitChanges: String
    val ruleName: String
    val noSavedRules: String
    val invalidRegex: String
    fun cardsAffected(count: Int): String
    val selectAll: String
    val deselectAll: String
    val changesSaved: String
    val enabled: String
    val noChangesFound: String
    val regexMatchesEmpty: String

    // Validation Errors
    val errorRetentionRange: String
    val errorInvalidNumber: String
    val errorPositiveInteger: String
    val errorInvalidStepsFormat: String
}

object EnStrings : AppStrings {
    // Common
    override val appName = "Kado"
    override val cancel = "Cancel"
    override val confirm = "Confirm"
    override val delete = "Delete"
    override val remove = "Remove"
    override val clone = "Clone"
    override val edit = "✏️"
    override val done = "Done"
    override val retry = "Retry"
    override val save = "Save"
    override val back = "Back"
    override val next = "Next"

    // Home
    override val noDecksTitle = "No decks yet"
    override val noDecksSubtitle = "Tap + to create your first deck"
    override val deleteDeck = "Delete Deck"
    override val deleteDeckMessage = "This will permanently delete this deck and all its cards."
    override val importApkg = "Import APKG"
    override val createDeck = "Create Deck"

    // Deck Detail
    override fun reviewCount(count: Int) = "Review ($count)"
    override val stats = "Stats"
    override val transferToDevice = "Transfer to Device"
    override val partitionDeck = "Partition Deck"
    override val subDecks = "Sub-decks"
    override fun partLabel(index: Int) = "Part ${index + 1}"
    override fun cardStats(cards: Int, due: Int, new: Int) = "$cards cards\n$due due\n$new new"
    override val review = "Review"
    override val send = "Send"
    override fun cardsCount(count: Int) = "$count cards"
    override val noCardsTitle = "No cards yet"
    override val noCardsSubtitle = "Tap + to add your first card"
    override val removeSubDeck = "Remove Sub-deck"
    override val removeSubDeckMessage = "Remove this sub-deck? Cards will become unassigned."
    override val cloneSubDeck = "Clone Sub-deck"
    override val cloneSubDeckMessage = "Create a new independent deck from this sub-deck?"
    override val createReversedDeck = "Create Reversed Deck"
    override val createReversedDeckMessage = "Create a new deck with front and back sides swapped?"

    // Review
    override val sessionComplete = "Session Complete"
    override fun reviewedCount(count: Int) = "Reviewed: $count"
    override fun againHardCount(again: Int, hard: Int) = "Again: $again  Hard: $hard"
    override fun goodEasyCount(good: Int, easy: Int) = "Good: $good  Easy: $easy"
    override val again = "Again"
    override val hard = "Hard"
    override val good = "Good"
    override val easy = "Easy"

    // Deck Edit
    override val newDeck = "New Deck"
    override val editDeck = "Edit Deck"
    override val deckName = "Deck Name"
    override val deckNamePlaceholder = "e.g. Japanese N5"
    override val dailyNewCardLimit = "Daily New Card Limit"
    override val saveChanges = "Save Changes"
    override val deleteDeckEditMessage = "Are you sure you want to delete this deck? All cards and progress will be permanently lost."
    override val deleteDeckButton = "Delete Deck"

    // Card Edit
    override val newCard = "New Card"
    override val editCard = "Edit Card"
    override val addCard = "Add Card"
    override val saveCard = "Save Card"
    override val deleteCard = "Delete Card"
    override val deleteCardMessage = "This card will be permanently deleted."
    override val front = "Front"
    override val backSide = "Back"
    override val questionPlaceholder = "Question or prompt"
    override val answerPlaceholder = "Answer"

    // Stats
    override val statistics = "Statistics"
    override val resetProgress = "Reset Progress"
    override val resetProgressMessage = "All review progress for this deck will be erased. Cards will be treated as new."
    override val resetCardProgressMessage = "This card's review progress will be erased. It will be treated as a new card."
    override val reset = "Reset"
    override val statsGuide = "Stats Guide"
    override val statsGuideBodySm2 = "New — Cards you haven't studied yet.\n\nLearning — Cards you got wrong and are re-learning.\n\nYoung — Cards you've reviewed, but with an interval under 21 days. After rating a card \"Good\" for the first time, it moves here with a 1-day interval.\n\nMature — Cards with an interval of 21+ days. These are well-known.\n\nDue Now — Total cards ready for review right now (new + overdue)."
    override val statsGuideBodyFsrs = "New — Cards you haven't studied yet.\n\nLearning — Cards going through learning steps (e.g., 1m, 10m) before entering the regular review schedule, or relearning steps after being forgotten.\n\nYoung — Cards in the review state with an interval under 21 days. FSRS tracks each card's stability and difficulty to schedule optimal reviews.\n\nMature — Cards with an interval of 21+ days. These have high memory stability.\n\nDue Now — Total cards ready for review right now (new + overdue)."
    override val gotIt = "Got it"
    override val newLabel = "New"
    override val learning = "Learning"
    override val young = "Young"
    override val mature = "Mature"
    override fun dueNow(count: Int) = "Due Now: $count"
    override fun totalCards(count: Int) = "Total: $count cards"

    // Connection
    override val deviceConnection = "Device Connection"
    override val connectionDescription = "Connect to your KadoLite device's WiFi network to transfer decks."
    override val deviceSsid = "Device SSID"
    override val ssidPlaceholder = "KadoLite-XXYY"
    override val connect = "Connect"
    override val connecting = "Connecting..."
    override val connected = "Connected"
    override val disconnect = "Disconnect"

    // Transfer
    override val transfer = "Transfer"
    override val uploadToDevice = "Upload to Device"
    override val uploading = "Uploading..."
    override val uploadSuccessful = "Upload successful!"
    override val deviceDecks = "Device Decks"
    override fun freeMb(mb: Int) = "Free: $mb MB"

    // Partition
    override val batch = "Batch"
    override val manual = "Manual"
    override val existingPartitionsWarning = "Existing partitions will be replaced."
    override val cardsPerSubDeck = "Cards per sub-deck"
    override fun subDeckSummary(count: Int, batchSize: Int, lastSize: Int): String {
        val base = "$count sub-decks of $batchSize cards"
        return if (lastSize != batchSize) "$base (last has $lastSize)" else base
    }
    override val partition = "Partition"
    override val partitioning = "Partitioning..."
    override val saving = "Saving..."

    // Settings
    override val settings = "Settings"
    override val about = "About"
    override val appSettings = "App Settings"
    override val donate = "Donate"
    override fun versionLabel(version: String) = "Version $version"
    override val theme = "Theme"
    override val system = "System"
    override val light = "Light"
    override val dark = "Dark"
    override val cardFontSize = "Card Font Size"
    override val appFontSize = "App Font Size"
    override val language = "Language"

    // About
    override val aboutDescription = "Kado is a fully open source and minimal alternative to flashcard software. Our goal is to provide a simple, distraction-free way to study using spaced repetition."
    override val kadoLite = "Kado Lite"
    override val kadoLiteDescription = "We also developed a Lite version for ESP32 microcontrollers (specifically the ESP32-C6 with Waveshare 1.47\" Touch LCD) so you can study your flashcards on the go with a dedicated device."
    override val contribute = "Contribute"
    override val contributeDescription = "Kado is open to contributions! Visit our repository at:\nhttps://github.com/LisandroDiMeo/kado-app"
    override val builtWith = "Built With"
    override val builtWithDescription = "This app is built using Compose Multiplatform, sharing a single codebase for both Android and iOS."

    // Donate
    override val supportKado = "Support Kado"
    override val donateDescription = "Kado is a free and open source project. If you find it useful and would like to support its continued development, you can make a voluntary donation."
    override val donateDisclaimer = "Please note: donations are entirely voluntary and do not grant any digital content, features, services, or advantages within the app. All app features are and will remain free for everyone."

    // Import
    override val importComplete = "Import Complete"
    override val importFailed = "Import Failed"
    override val importing = "Importing..."
    override val extracting = "Extracted APKG file..."
    override val readingAnkiDb = "Reading Anki database..."
    override fun importingCards(count: Int) = "Importing $count cards..."
    override val processing = "Processing..."
    override val unknownError = "An unknown error occurred"
    override fun importedCards(count: Int, deckName: String) = "Imported $count cards into \"$deckName\""
    override val dismiss = "Dismiss"

    // Images
    override val images = "Images"
    override val noImagesAttached = "No images attached"
    override val addImage = "+ Add Image"

    // Help
    override val help = "Help"
    override val howToUseApp = "How to use the app"
    override val howToUseKadoLite = "How to use KadoLite"
    override val tutorialCreateDeckTitle = "Create a Deck"
    override val tutorialCreateDeckDesc = "Tap the + button on the home screen and select \"Create Deck\" to make a new flashcard deck."
    override val tutorialAddCardsTitle = "Add Cards"
    override val tutorialAddCardsDesc = "Open a deck and tap \"Add Card\" to create flashcards with a front and back side."
    override val tutorialImportAnkiTitle = "Import from Anki"
    override val tutorialImportAnkiDesc = "Tap the + button and select \"Import .apkg\" to import an existing Anki deck."
    override val tutorialReviewCardsTitle = "Review Cards"
    override val tutorialReviewCardsDesc = "Open a deck and tap \"Review\" to start studying. Cards are scheduled using spaced repetition."
    override val tutorialRateRecallTitle = "Rate Your Recall"
    override val tutorialRateRecallDesc = "After revealing the answer, rate how well you remembered it. This adjusts when you'll see the card again."
    override val tutorialPowerOnTitle = "Power On"
    override val tutorialPowerOnDesc = "Turn on your KadoLite device. It will create a WiFi network starting with \"KadoLite-\"."
    override val tutorialConnectTitle = "Connect"
    override val tutorialConnectDesc = "Go to the home screen and tap \"WiFi\" to connect your phone to the KadoLite device."
    override val tutorialTransferDeckTitle = "Transfer a Deck"
    override val tutorialTransferDeckDesc = "Open a deck, tap \"Transfer\" and select the deck or sub-deck you want to send to the device."
    override val tutorialReviewDeviceTitle = "Review on Device"
    override val tutorialReviewDeviceDesc = "Use the KadoLite buttons to flip cards and rate your recall directly on the e-ink screen."
    override val tutorialSyncBackTitle = "Sync Back"
    override val tutorialSyncBackDesc = "Connect again via WiFi to sync your review progress back to the app."

    // Tutorial Dialog
    override fun stepOf(current: Int, total: Int) = "Step $current of $total"

    // DeckCard
    override fun dueCount(count: Int) = "$count due"
    override fun newCount(count: Int) = "$count new"

    // Algorithm / Scheduler
    override val scheduler = "Scheduler"
    override val schedulerSm2 = "SM-2"
    override val schedulerFsrs = "FSRS"
    override val sm2Algorithm = "SM-2 Algorithm"
    override val fsrsAlgorithm = "FSRS Algorithm"

    // FSRS Settings
    override val fsrsSettings = "FSRS Settings"
    override val desiredRetention = "Desired Retention"
    override val desiredRetentionHint = "Target recall rate (0.70 - 0.99)"
    override val learningSteps = "Learning Steps"
    override val learningStepsHint = "Intervals for new cards (e.g., 1m, 10m)"
    override val relearningSteps = "Relearning Steps"
    override val relearningStepsHint = "Intervals for forgotten cards (e.g., 10m)"
    override val maxInterval = "Maximum Interval (days)"
    override val maxIntervalHint = "Longest delay between reviews"
    override val enableFuzzing = "Enable Fuzzing"
    override val enableFuzzingHint = "Adds random variation to spread reviews"
    override val resetToDefaults = "Reset to Defaults"

    // SM-2 Parameters
    override val ease = "Ease Factor"
    override val easeDescription = "Controls how quickly intervals grow. Higher ease means longer intervals."
    override val easeHint = "Range: 1.3 - 4.0 (default: 2.5)"

    // Algorithm Explanations
    override val sm2SimpleExplanation = "SM-2 is a classic spaced repetition algorithm. Each card has an ease factor that determines how quickly review intervals grow. When you rate a card Easy, the ease increases and intervals grow faster. When you rate Again, the card resets to a short interval. Hard slightly reduces the ease, while Good keeps it stable."
    override val fsrsSimpleExplanation = "FSRS (Free Spaced Repetition Scheduler) uses a scientific model of memory to schedule reviews. It tracks two key properties for each card: stability (how long you'll remember it) and difficulty (how hard the card is for you). FSRS calculates the optimal review time to maintain your desired recall rate, adapting to your performance over time."
    override val sm2TechnicalDetails = "SM-2 uses an ease factor (EF) that multiplies the previous interval:\n\n• New cards start with EF = 2.5\n• Again: resets interval, EF -= 0.2\n• Hard: interval × 1.2, EF -= 0.1\n• Good: interval × EF\n• Easy: interval × EF × 1.3, EF += 0.2\n\nEF is clamped to [1.3, 4.0]. Intervals are clamped to [1, 36500] days."
    override val fsrsTechnicalDetails = "FSRS uses a memory model based on the forgetting curve:\n\nRetrievability: R = (1 + t/S × factor)^decay\n\nWhere S is stability (days until 90% recall), t is time elapsed.\n\nAfter each review, stability and difficulty are updated using 21 optimized parameters. The model accounts for:\n• Memory strength grows more when retrievability is low\n• Harder cards gain stability more slowly\n• Forgetting (Again) reduces stability based on current difficulty\n\nThe optimal interval is calculated to maintain your desired retention rate."

    // Algorithm Parameter Explanations
    override val desiredRetentionExplanation = "The probability of successfully recalling a card when it comes up for review. A value of 0.9 means you'll remember 90% of cards. Higher values lead to shorter intervals (more reviews), lower values lead to longer intervals (fewer reviews but more forgetting)."
    override val learningStepsExplanation = "When you see a new card for the first time, it goes through learning steps before entering the regular review schedule. Each step defines how long to wait before showing the card again. For example, '1m, 10m' means show the card again after 1 minute, then after 10 minutes, before scheduling a full review."
    override val relearningStepsExplanation = "When you forget a card (rate Again), it enters relearning. These steps work like learning steps but for cards you previously knew. After completing all relearning steps, the card returns to the regular review schedule with adjusted stability."
    override val maxIntervalExplanation = "The maximum number of days between reviews. Even if the algorithm calculates a longer interval, it will be capped at this value. The default of 36500 days (~100 years) effectively means no limit."
    override val enableFuzzingExplanation = "Adds a small random variation to review intervals. This prevents cards that were reviewed together from always coming due on the same day, spreading your workload more evenly. The variation is proportional to the interval length: ±15% for short intervals, ±5% for long ones."

    // Algorithm Detail UI
    override val howItWorks = "How It Works"
    override val technicalDetails = "Technical Details"
    override val parameters = "Parameters"

    // Bulk Edit
    override val selectCards = "Select cards"
    override fun selectedCount(count: Int) = "$count selected"
    override val deleteSelected = "Delete selected"
    override fun deleteSelectedMessage(count: Int) =
        if (count == 1) "1 card will be deleted." else "$count cards will be deleted."
    override val updateFromApkg = "Update from APKG…"
    override val updateDeckTitle = "Update deck"
    override fun patchNeedsBackfillMessage(nullCount: Int): String =
        "This deck was imported before stable card IDs were tracked ($nullCount cards). " +
            "To safely apply an update, please re-import the original APKG first to backfill " +
            "identifiers. Your progress will be preserved."
    override val patchReimportOriginal = "Re-import original"
    override fun patchBackfillResult(matched: Int, unmatched: Int): String {
        val base = "Backfilled $matched cards. $unmatched unmatched."
        return if (unmatched == 0) {
            "$base You can now apply the update."
        } else {
            "$base Unmatched cards can't be patched."
        }
    }
    override val patchContinue = "Continue"
    override val patchInvalidApkg = "Not a valid APKG file"
    override val patchReadDbFailed = "Failed to read APKG database"
    override val patchNoCards = "No cards found in APKG"
    override val patchParseFailed = "Failed to parse APKG"
    override val patchClose = "Close"
    override val patchSessionMissing = "Patch session expired"
    override val patchNoChangesTitle = "Already up to date"
    override val patchNoChangesMessage = "This deck already matches the APKG you selected. Nothing to update."
    override val patchPreviewIntro =
        "Review the changes before applying. Modified cards keep their progress; " +
            "added cards start fresh; removed cards will be deleted unless you tick to keep."
    override fun patchAddedSection(count: Int) = "Added ($count)"
    override fun patchModifiedSection(count: Int) = "Modified ($count)"
    override fun patchRemovedSection(count: Int) = "Removed ($count)"
    override val patchKeepHint = "Tick a card to keep it instead of deleting."
    override fun patchMoreItems(count: Int) = "+$count more"
    override fun patchLoadMoreLeft(count: Int) = "Load more ($count left)"
    override fun patchKeptCount(count: Int) =
        if (count == 1) "1 card will be kept." else "$count cards will be kept."
    override val patchApply = "Apply update"
    override val patchApplying = "Applying…"
    override val patchDiscard = "Discard"
    override val patchBefore = "Before:"
    override val patchAfter = "After:"
    override val patchWillKeep = "Will be kept"
    override val patchWillDelete = "Will be deleted"
    override val patchParsing = "Reading APKG…"
    override val patchBackfilling = "Backfilling identifiers…"
    override val bulkEdit = "Bulk Edit"
    override val cardFrontRule = "Replace on Front"
    override val cardBackRule = "Replace on Back"
    override val findPattern = "Find"
    override val replacePattern = "Replace"
    override val isRegex = "Regex"
    override val previewChanges = "Preview Changes"
    override val saveRule = "Save Rule"
    override val savedRules = "Saved Rules"
    override val commitChanges = "Commit Changes"
    override val ruleName = "Rule Name"
    override val noSavedRules = "No saved rules"
    override val invalidRegex = "Invalid regex pattern"
    override fun cardsAffected(count: Int) = "$count cards will be modified"
    override val selectAll = "Select All"
    override val deselectAll = "Deselect All"
    override val changesSaved = "Changes saved"
    override val enabled = "Enabled"
    override val noChangesFound = "No cards matched the pattern"
    override val regexMatchesEmpty = "Pattern must not match empty strings"

    // Validation Errors
    override val errorRetentionRange = "Must be between 0.70 and 0.99"
    override val errorInvalidNumber = "Must be a valid number"
    override val errorPositiveInteger = "Must be a positive whole number"
    override val errorInvalidStepsFormat = "Use comma-separated times (e.g., 1m, 10m)"
}

object EsStrings : AppStrings {
    // Common
    override val appName = "Kado"
    override val cancel = "Cancelar"
    override val confirm = "Confirmar"
    override val delete = "Eliminar"
    override val remove = "Quitar"
    override val clone = "Clonar"
    override val edit = "✏️"
    override val done = "Listo"
    override val retry = "Reintentar"
    override val save = "Guardar"
    override val back = "Atrás"
    override val next = "Siguiente"

    // Home
    override val noDecksTitle = "No hay mazos"
    override val noDecksSubtitle = "Toca + para crear tu primer mazo"
    override val deleteDeck = "Eliminar Mazo"
    override val deleteDeckMessage = "Esto eliminará permanentemente este mazo y todas sus tarjetas."
    override val importApkg = "Importar APKG"
    override val createDeck = "Crear Mazo"

    // Deck Detail
    override fun reviewCount(count: Int) = "Repasar ($count)"
    override val stats = "Estadísticas"
    override val transferToDevice = "Enviar al Dispositivo"
    override val partitionDeck = "Dividir Mazo"
    override val subDecks = "Sub-mazos"
    override fun partLabel(index: Int) = "Parte ${index + 1}"
    override fun cardStats(cards: Int, due: Int, new: Int) = "$cards tarjetas\n$due pendientes\n$new nuevas"
    override val review = "Repasar"
    override val send = "Enviar"
    override fun cardsCount(count: Int) = "$count tarjetas"
    override val noCardsTitle = "No hay tarjetas"
    override val noCardsSubtitle = "Toca + para agregar tu primera tarjeta"
    override val removeSubDeck = "Quitar Sub-mazo"
    override val removeSubDeckMessage = "¿Quitar este sub-mazo? Las tarjetas quedarán sin asignar."
    override val cloneSubDeck = "Clonar Sub-mazo"
    override val cloneSubDeckMessage = "¿Crear un nuevo mazo independiente de este sub-mazo?"
    override val createReversedDeck = "Crear Mazo Invertido"
    override val createReversedDeckMessage = "¿Crear un nuevo mazo con frente y dorso invertidos?"

    // Review
    override val sessionComplete = "Sesión Completa"
    override fun reviewedCount(count: Int) = "Repasadas: $count"
    override fun againHardCount(again: Int, hard: Int) = "Otra vez: $again  Difícil: $hard"
    override fun goodEasyCount(good: Int, easy: Int) = "Bien: $good  Fácil: $easy"
    override val again = "Otra vez"
    override val hard = "Difícil"
    override val good = "Bien"
    override val easy = "Fácil"

    // Deck Edit
    override val newDeck = "Nuevo Mazo"
    override val editDeck = "Editar Mazo"
    override val deckName = "Nombre del Mazo"
    override val deckNamePlaceholder = "ej. Japonés N5"
    override val dailyNewCardLimit = "Límite Diario de Tarjetas Nuevas"
    override val saveChanges = "Guardar Cambios"
    override val deleteDeckEditMessage = "¿Estás seguro de que quieres eliminar este mazo? Todas las tarjetas y el progreso se perderán permanentemente."
    override val deleteDeckButton = "Eliminar Mazo"

    // Card Edit
    override val newCard = "Nueva Tarjeta"
    override val editCard = "Editar Tarjeta"
    override val addCard = "Agregar Tarjeta"
    override val saveCard = "Guardar Tarjeta"
    override val deleteCard = "Eliminar Tarjeta"
    override val deleteCardMessage = "Esta tarjeta será eliminada permanentemente."
    override val front = "Frente"
    override val backSide = "Dorso"
    override val questionPlaceholder = "Pregunta o consigna"
    override val answerPlaceholder = "Respuesta"

    // Stats
    override val statistics = "Estadísticas"
    override val resetProgress = "Reiniciar Progreso"
    override val resetProgressMessage = "Todo el progreso de repaso de este mazo será borrado. Las tarjetas serán tratadas como nuevas."
    override val resetCardProgressMessage = "El progreso de repaso de esta tarjeta será borrado. Será tratada como una tarjeta nueva."
    override val reset = "Reiniciar"
    override val statsGuide = "Guía de Estadísticas"
    override val statsGuideBodySm2 = "Nuevas — Tarjetas que aún no has estudiado.\n\nAprendiendo — Tarjetas que respondiste mal y estás re-aprendiendo.\n\nJóvenes — Tarjetas que has repasado, pero con un intervalo menor a 21 días. Después de calificar una tarjeta como \"Bien\" por primera vez, se mueve aquí con un intervalo de 1 día.\n\nMaduras — Tarjetas con un intervalo de 21+ días. Son las que conoces bien.\n\nPendientes — Total de tarjetas listas para repasar ahora (nuevas + atrasadas)."
    override val statsGuideBodyFsrs = "Nuevas — Tarjetas que aún no has estudiado.\n\nAprendiendo — Tarjetas que están pasando por los pasos de aprendizaje (ej. 1m, 10m) antes de entrar al calendario regular de repasos, o por pasos de reaprendizaje después de ser olvidadas.\n\nJóvenes — Tarjetas en estado de repaso con un intervalo menor a 21 días. FSRS rastrea la estabilidad y dificultad de cada tarjeta para programar repasos óptimos.\n\nMaduras — Tarjetas con un intervalo de 21+ días. Tienen alta estabilidad de memoria.\n\nPendientes — Total de tarjetas listas para repasar ahora (nuevas + atrasadas)."
    override val gotIt = "Entendido"
    override val newLabel = "Nuevas"
    override val learning = "Aprendiendo"
    override val young = "Jóvenes"
    override val mature = "Maduras"
    override fun dueNow(count: Int) = "Pendientes: $count"
    override fun totalCards(count: Int) = "Total: $count tarjetas"

    // Connection
    override val deviceConnection = "Conexión al Dispositivo"
    override val connectionDescription = "Conéctate a la red WiFi de tu dispositivo KadoLite para transferir mazos."
    override val deviceSsid = "SSID del Dispositivo"
    override val ssidPlaceholder = "KadoLite-XXYY"
    override val connect = "Conectar"
    override val connecting = "Conectando..."
    override val connected = "Conectado"
    override val disconnect = "Desconectar"

    // Transfer
    override val transfer = "Transferir"
    override val uploadToDevice = "Enviar al Dispositivo"
    override val uploading = "Enviando..."
    override val uploadSuccessful = "¡Envío exitoso!"
    override val deviceDecks = "Mazos del Dispositivo"
    override fun freeMb(mb: Int) = "Libre: $mb MB"

    // Partition
    override val batch = "Lote"
    override val manual = "Manual"
    override val existingPartitionsWarning = "Las particiones existentes serán reemplazadas."
    override val cardsPerSubDeck = "Tarjetas por sub-mazo"
    override fun subDeckSummary(count: Int, batchSize: Int, lastSize: Int): String {
        val base = "$count sub-mazos de $batchSize tarjetas"
        return if (lastSize != batchSize) "$base (el último tiene $lastSize)" else base
    }
    override val partition = "Dividir"
    override val partitioning = "Dividiendo..."
    override val saving = "Guardando..."

    // Settings
    override val settings = "Ajustes"
    override val about = "Acerca de"
    override val appSettings = "Ajustes de la App"
    override val donate = "Donar"
    override fun versionLabel(version: String) = "Versión $version"
    override val theme = "Tema"
    override val system = "Sistema"
    override val light = "Claro"
    override val dark = "Oscuro"
    override val cardFontSize = "Tamaño de Fuente de Tarjeta"
    override val appFontSize = "Tamaño de Fuente de la App"
    override val language = "Idioma"

    // About
    override val aboutDescription = "Kado es una alternativa completamente de código abierto y minimalista al software de tarjetas de estudio. Nuestro objetivo es proporcionar una forma simple y sin distracciones de estudiar usando repetición espaciada."
    override val kadoLite = "Kado Lite"
    override val kadoLiteDescription = "También desarrollamos una versión Lite para microcontroladores ESP32 (específicamente el ESP32-C6 con pantalla táctil Waveshare 1.47\") para que puedas estudiar tus tarjetas con un dispositivo dedicado."
    override val contribute = "Contribuir"
    override val contributeDescription = "¡Kado está abierto a contribuciones! Visita nuestro repositorio en:\nhttps://github.com/LisandroDiMeo/kado-app"
    override val builtWith = "Hecho Con"
    override val builtWithDescription = "Esta app está construida usando Compose Multiplatform, compartiendo una única base de código para Android e iOS."

    // Donate
    override val supportKado = "Apoyar a Kado"
    override val donateDescription = "Kado es un proyecto gratuito y de código abierto. Si te resulta útil y deseas apoyar su desarrollo continuo, puedes hacer una donación voluntaria."
    override val donateDisclaimer = "Nota: las donaciones son completamente voluntarias y no otorgan ningún contenido digital, funcionalidad, servicio o ventaja dentro de la app. Todas las funciones de la app son y seguirán siendo gratuitas para todos."

    // Import
    override val importComplete = "Importación Completa"
    override val importFailed = "Error de Importación"
    override val importing = "Importando..."
    override val extracting = "Extrayendo archivo APKG..."
    override val readingAnkiDb = "Leyendo base de datos de Anki..."
    override fun importingCards(count: Int) = "Importando $count tarjetas..."
    override val processing = "Procesando..."
    override val unknownError = "Ocurrió un error desconocido"
    override fun importedCards(count: Int, deckName: String) = "Se importaron $count tarjetas a \"$deckName\""
    override val dismiss = "Cerrar"

    // Images
    override val images = "Imágenes"
    override val noImagesAttached = "Sin imágenes adjuntas"
    override val addImage = "+ Agregar Imagen"

    // Help
    override val help = "Ayuda"
    override val howToUseApp = "Cómo usar la app"
    override val howToUseKadoLite = "Cómo usar KadoLite"
    override val tutorialCreateDeckTitle = "Crear un Mazo"
    override val tutorialCreateDeckDesc = "Toca el botón + en la pantalla principal y selecciona \"Crear Mazo\" para hacer un nuevo mazo de tarjetas."
    override val tutorialAddCardsTitle = "Agregar Tarjetas"
    override val tutorialAddCardsDesc = "Abre un mazo y toca \"Agregar Tarjeta\" para crear tarjetas con un frente y un dorso."
    override val tutorialImportAnkiTitle = "Importar desde Anki"
    override val tutorialImportAnkiDesc = "Toca el botón + y selecciona \"Importar .apkg\" para importar un mazo existente de Anki."
    override val tutorialReviewCardsTitle = "Repasar Tarjetas"
    override val tutorialReviewCardsDesc = "Abre un mazo y toca \"Repasar\" para comenzar a estudiar. Las tarjetas se programan usando repetición espaciada."
    override val tutorialRateRecallTitle = "Calificar tu Recuerdo"
    override val tutorialRateRecallDesc = "Después de ver la respuesta, califica qué tan bien la recordaste. Esto ajusta cuándo volverás a ver la tarjeta."
    override val tutorialPowerOnTitle = "Encender"
    override val tutorialPowerOnDesc = "Enciende tu dispositivo KadoLite. Creará una red WiFi que comienza con \"KadoLite-\"."
    override val tutorialConnectTitle = "Conectar"
    override val tutorialConnectDesc = "Ve a la pantalla principal y toca \"WiFi\" para conectar tu teléfono al dispositivo KadoLite."
    override val tutorialTransferDeckTitle = "Transferir un Mazo"
    override val tutorialTransferDeckDesc = "Abre un mazo, toca \"Transferir\" y selecciona el mazo o sub-mazo que quieres enviar al dispositivo."
    override val tutorialReviewDeviceTitle = "Repasar en el Dispositivo"
    override val tutorialReviewDeviceDesc = "Usa los botones de KadoLite para voltear tarjetas y calificar tu recuerdo directamente en la pantalla."
    override val tutorialSyncBackTitle = "Sincronizar"
    override val tutorialSyncBackDesc = "Conéctate nuevamente por WiFi para sincronizar tu progreso de repaso con la app."

    // Tutorial Dialog
    override fun stepOf(current: Int, total: Int) = "Paso $current de $total"

    // DeckCard
    override fun dueCount(count: Int) = "$count pendientes"
    override fun newCount(count: Int) = "$count nuevas"

    // Algorithm / Scheduler
    override val scheduler = "Planificador"
    override val schedulerSm2 = "SM-2"
    override val schedulerFsrs = "FSRS"
    override val sm2Algorithm = "Algoritmo SM-2"
    override val fsrsAlgorithm = "Algoritmo FSRS"

    // FSRS Settings
    override val fsrsSettings = "Ajustes de FSRS"
    override val desiredRetention = "Retención Deseada"
    override val desiredRetentionHint = "Tasa de recuerdo objetivo (0.70 - 0.99)"
    override val learningSteps = "Pasos de Aprendizaje"
    override val learningStepsHint = "Intervalos para tarjetas nuevas (ej. 1m, 10m)"
    override val relearningSteps = "Pasos de Reaprendizaje"
    override val relearningStepsHint = "Intervalos para tarjetas olvidadas (ej. 10m)"
    override val maxInterval = "Intervalo Máximo (días)"
    override val maxIntervalHint = "Retraso máximo entre revisiones"
    override val enableFuzzing = "Habilitar Variación"
    override val enableFuzzingHint = "Agrega variación aleatoria para distribuir revisiones"
    override val resetToDefaults = "Restablecer Valores"

    // SM-2 Parameters
    override val ease = "Factor de Facilidad"
    override val easeDescription = "Controla qué tan rápido crecen los intervalos. Mayor facilidad significa intervalos más largos."
    override val easeHint = "Rango: 1.3 - 4.0 (predeterminado: 2.5)"

    // Algorithm Explanations
    override val sm2SimpleExplanation = "SM-2 es un algoritmo clásico de repetición espaciada. Cada tarjeta tiene un factor de facilidad que determina qué tan rápido crecen los intervalos de repaso. Cuando calificas una tarjeta como Fácil, la facilidad aumenta y los intervalos crecen más rápido. Cuando calificas Otra vez, la tarjeta se reinicia a un intervalo corto. Difícil reduce ligeramente la facilidad, mientras que Bien la mantiene estable."
    override val fsrsSimpleExplanation = "FSRS (Free Spaced Repetition Scheduler) utiliza un modelo científico de la memoria para programar repasos. Rastrea dos propiedades clave de cada tarjeta: estabilidad (cuánto tiempo la recordarás) y dificultad (qué tan difícil es la tarjeta para ti). FSRS calcula el momento óptimo de repaso para mantener tu tasa de recuerdo deseada, adaptándose a tu rendimiento con el tiempo."
    override val sm2TechnicalDetails = "SM-2 usa un factor de facilidad (EF) que multiplica el intervalo anterior:\n\n• Las tarjetas nuevas comienzan con EF = 2.5\n• Otra vez: reinicia el intervalo, EF -= 0.2\n• Difícil: intervalo × 1.2, EF -= 0.1\n• Bien: intervalo × EF\n• Fácil: intervalo × EF × 1.3, EF += 0.2\n\nEF se limita a [1.3, 4.0]. Los intervalos se limitan a [1, 36500] días."
    override val fsrsTechnicalDetails = "FSRS usa un modelo de memoria basado en la curva del olvido:\n\nRecuperabilidad: R = (1 + t/S × factor)^decaimiento\n\nDonde S es la estabilidad (días hasta 90% de recuerdo), t es el tiempo transcurrido.\n\nDespués de cada repaso, la estabilidad y la dificultad se actualizan usando 21 parámetros optimizados. El modelo considera:\n• La fuerza de la memoria crece más cuando la recuperabilidad es baja\n• Las tarjetas más difíciles ganan estabilidad más lentamente\n• Olvidar (Otra vez) reduce la estabilidad según la dificultad actual\n\nEl intervalo óptimo se calcula para mantener tu tasa de retención deseada."

    // Algorithm Parameter Explanations
    override val desiredRetentionExplanation = "La probabilidad de recordar exitosamente una tarjeta cuando aparece para repaso. Un valor de 0.9 significa que recordarás el 90% de las tarjetas. Valores más altos generan intervalos más cortos (más repasos), valores más bajos generan intervalos más largos (menos repasos pero más olvido)."
    override val learningStepsExplanation = "Cuando ves una tarjeta nueva por primera vez, pasa por pasos de aprendizaje antes de entrar al calendario regular de repasos. Cada paso define cuánto esperar antes de mostrar la tarjeta de nuevo. Por ejemplo, '1m, 10m' significa mostrar la tarjeta después de 1 minuto, luego después de 10 minutos, antes de programar un repaso completo."
    override val relearningStepsExplanation = "Cuando olvidas una tarjeta (calificas Otra vez), entra en reaprendizaje. Estos pasos funcionan como los pasos de aprendizaje pero para tarjetas que ya conocías. Después de completar todos los pasos de reaprendizaje, la tarjeta vuelve al calendario regular de repasos con estabilidad ajustada."
    override val maxIntervalExplanation = "El número máximo de días entre repasos. Incluso si el algoritmo calcula un intervalo más largo, se limitará a este valor. El valor predeterminado de 36500 días (~100 años) significa efectivamente sin límite."
    override val enableFuzzingExplanation = "Agrega una pequeña variación aleatoria a los intervalos de repaso. Esto evita que tarjetas repasadas juntas siempre venzan el mismo día, distribuyendo tu carga de trabajo más uniformemente. La variación es proporcional a la longitud del intervalo: ±15% para intervalos cortos, ±5% para largos."

    // Algorithm Detail UI
    override val howItWorks = "Cómo Funciona"
    override val technicalDetails = "Detalles Técnicos"
    override val parameters = "Parámetros"

    // Bulk Edit
    override val selectCards = "Seleccionar tarjetas"
    override fun selectedCount(count: Int) = "$count seleccionadas"
    override val deleteSelected = "Eliminar seleccionadas"
    override fun deleteSelectedMessage(count: Int) =
        if (count == 1) "Se eliminará 1 tarjeta." else "Se eliminarán $count tarjetas."
    override val updateFromApkg = "Actualizar desde APKG…"
    override val updateDeckTitle = "Actualizar mazo"
    override fun patchNeedsBackfillMessage(nullCount: Int): String =
        "Este mazo se importó antes de que se rastrearan IDs estables de tarjeta ($nullCount tarjetas). " +
            "Para aplicar una actualización de forma segura, primero vuelve a importar el APKG original " +
            "para rellenar los identificadores. Tu progreso se conservará."
    override val patchReimportOriginal = "Reimportar original"
    override fun patchBackfillResult(matched: Int, unmatched: Int): String {
        val base = "Se rellenaron $matched tarjetas. $unmatched sin coincidencia."
        return if (unmatched == 0) {
            "$base Ya puedes aplicar la actualización."
        } else {
            "$base Las tarjetas sin coincidencia no se pueden parchear."
        }
    }
    override val patchContinue = "Continuar"
    override val patchInvalidApkg = "Archivo APKG no válido"
    override val patchReadDbFailed = "No se pudo leer la base de datos del APKG"
    override val patchNoCards = "No se encontraron tarjetas en el APKG"
    override val patchParseFailed = "No se pudo analizar el APKG"
    override val patchClose = "Cerrar"
    override val patchSessionMissing = "La sesión de parche caducó"
    override val patchNoChangesTitle = "Ya está al día"
    override val patchNoChangesMessage = "Este mazo ya coincide con el APKG que seleccionaste. No hay nada que actualizar."
    override val patchPreviewIntro =
        "Revisa los cambios antes de aplicarlos. Las tarjetas modificadas conservan su progreso; " +
            "las añadidas empiezan desde cero; las eliminadas se borrarán salvo que las marques para conservar."
    override fun patchAddedSection(count: Int) = "Añadidas ($count)"
    override fun patchModifiedSection(count: Int) = "Modificadas ($count)"
    override fun patchRemovedSection(count: Int) = "Eliminadas ($count)"
    override val patchKeepHint = "Marca una tarjeta para conservarla en lugar de eliminarla."
    override fun patchMoreItems(count: Int) = "+$count más"
    override fun patchLoadMoreLeft(count: Int) = "Cargar más (quedan $count)"
    override fun patchKeptCount(count: Int) =
        if (count == 1) "Se conservará 1 tarjeta." else "Se conservarán $count tarjetas."
    override val patchApply = "Aplicar actualización"
    override val patchApplying = "Aplicando…"
    override val patchDiscard = "Descartar"
    override val patchBefore = "Antes:"
    override val patchAfter = "Después:"
    override val patchWillKeep = "Se conservará"
    override val patchWillDelete = "Se eliminará"
    override val patchParsing = "Leyendo APKG…"
    override val patchBackfilling = "Rellenando identificadores…"
    override val bulkEdit = "Edición Masiva"
    override val cardFrontRule = "Reemplazar en el Frente"
    override val cardBackRule = "Reemplazar en el Dorso"
    override val findPattern = "Buscar"
    override val replacePattern = "Reemplazar"
    override val isRegex = "Regex"
    override val previewChanges = "Vista Previa"
    override val saveRule = "Guardar Regla"
    override val savedRules = "Reglas Guardadas"
    override val commitChanges = "Aplicar Cambios"
    override val ruleName = "Nombre de la Regla"
    override val noSavedRules = "No hay reglas guardadas"
    override val invalidRegex = "Patrón regex inválido"
    override fun cardsAffected(count: Int) = "$count tarjetas serán modificadas"
    override val selectAll = "Seleccionar Todo"
    override val deselectAll = "Deseleccionar Todo"
    override val changesSaved = "Cambios guardados"
    override val enabled = "Habilitado"
    override val noChangesFound = "Ninguna tarjeta coincide con el patrón"
    override val regexMatchesEmpty = "El patrón no debe coincidir con cadenas vacías"

    // Validation Errors
    override val errorRetentionRange = "Debe estar entre 0.70 y 0.99"
    override val errorInvalidNumber = "Debe ser un número válido"
    override val errorPositiveInteger = "Debe ser un número entero positivo"
    override val errorInvalidStepsFormat = "Usa tiempos separados por comas (ej. 1m, 10m)"
}
