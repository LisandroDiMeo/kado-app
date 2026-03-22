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
    val reset: String
    val statsGuide: String
    val statsGuideBody: String
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
    override fun cardStats(cards: Int, due: Int, new: Int) = "$cards cards | $due due | $new new"
    override val review = "Review"
    override val send = "Send"
    override fun cardsCount(count: Int) = "$count cards"
    override val noCardsTitle = "No cards yet"
    override val noCardsSubtitle = "Tap + to add your first card"
    override val removeSubDeck = "Remove Sub-deck"
    override val removeSubDeckMessage = "Remove this sub-deck? Cards will become unassigned."
    override val cloneSubDeck = "Clone Sub-deck"
    override val cloneSubDeckMessage = "Create a new independent deck from this sub-deck?"

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
    override val reset = "Reset"
    override val statsGuide = "Stats Guide"
    override val statsGuideBody = "New — Cards you haven't studied yet.\n\nLearning — Cards you got wrong and are re-learning.\n\nYoung — Cards you've reviewed, but with an interval under 21 days. After rating a card \"Good\" for the first time, it moves here with a 1-day interval.\n\nMature — Cards with an interval of 21+ days. These are well-known.\n\nDue Now — Total cards ready for review right now (new + overdue)."
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
    override fun cardStats(cards: Int, due: Int, new: Int) = "$cards tarjetas | $due pendientes | $new nuevas"
    override val review = "Repasar"
    override val send = "Enviar"
    override fun cardsCount(count: Int) = "$count tarjetas"
    override val noCardsTitle = "No hay tarjetas"
    override val noCardsSubtitle = "Toca + para agregar tu primera tarjeta"
    override val removeSubDeck = "Quitar Sub-mazo"
    override val removeSubDeckMessage = "¿Quitar este sub-mazo? Las tarjetas quedarán sin asignar."
    override val cloneSubDeck = "Clonar Sub-mazo"
    override val cloneSubDeckMessage = "¿Crear un nuevo mazo independiente de este sub-mazo?"

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
    override val reset = "Reiniciar"
    override val statsGuide = "Guía de Estadísticas"
    override val statsGuideBody = "Nuevas — Tarjetas que aún no has estudiado.\n\nAprendiendo — Tarjetas que respondiste mal y estás re-aprendiendo.\n\nJóvenes — Tarjetas que has repasado, pero con un intervalo menor a 21 días. Después de calificar una tarjeta como \"Bien\" por primera vez, se mueve aquí con un intervalo de 1 día.\n\nMaduras — Tarjetas con un intervalo de 21+ días. Son las que conoces bien.\n\nPendientes — Total de tarjetas listas para repasar ahora (nuevas + atrasadas)."
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
}
