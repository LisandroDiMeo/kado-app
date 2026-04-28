package com.kado.app.domain.model

sealed interface DeckPatchGate {
    data object Ready : DeckPatchGate
    data class NeedsBackfill(val nullGuidCount: Int) : DeckPatchGate
}

data class BackfillResult(val matched: Int, val unmatched: Int)

data class PatchCounts(val added: Int, val modified: Int, val removed: Int, val unchanged: Int) {
    val totalChanges: Int get() = added + modified + removed
}

data class DeckPatchHandle(val sessionId: String, val deckId: Long, val deckName: String, val counts: PatchCounts)

sealed interface DeckPatchPreviewItem {
    data class Added(val front: String, val back: String) : DeckPatchPreviewItem
    data class Modified(
        val cardId: Long,
        val oldFront: String,
        val oldBack: String,
        val newFront: String,
        val newBack: String
    ) : DeckPatchPreviewItem
    data class Removed(val cardId: Long, val front: String, val back: String) : DeckPatchPreviewItem
}
