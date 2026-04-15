package com.kado.app.domain.usecase

import com.kado.app.domain.model.Card

data class FindReplaceParams(
    val frontFind: String,
    val frontReplace: String,
    val frontIsRegex: Boolean,
    val frontEnabled: Boolean,
    val backFind: String,
    val backReplace: String,
    val backIsRegex: Boolean,
    val backEnabled: Boolean
)

data class CardChange(val card: Card, val newFront: String, val newBack: String, val isSelected: Boolean = true)

class ApplyFindReplaceUseCase {

    operator fun invoke(cards: List<Card>, params: FindReplaceParams): List<CardChange> = cards.mapNotNull { card ->
        val originalFront = card.front.rawText
        val originalBack = card.back.rawText

        val newFront = if (params.frontEnabled && params.frontFind.isNotEmpty()) {
            applyReplace(originalFront, params.frontFind, params.frontReplace, params.frontIsRegex)
        } else {
            originalFront
        }

        val newBack = if (params.backEnabled && params.backFind.isNotEmpty()) {
            applyReplace(originalBack, params.backFind, params.backReplace, params.backIsRegex)
        } else {
            originalBack
        }

        if (newFront != originalFront || newBack != originalBack) {
            CardChange(card = card, newFront = newFront, newBack = newBack)
        } else {
            null
        }
    }

    private fun applyReplace(text: String, find: String, replace: String, isRegex: Boolean): String {
        if (find.isEmpty()) return text
        return if (isRegex) {
            try {
                Regex(find).replace(text, replace)
            } catch (e: Exception) {
                text
            }
        } else {
            text.replace(find, replace)
        }
    }
}
