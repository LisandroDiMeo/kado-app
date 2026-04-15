package com.kado.app.presentation.screens.bulk_edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kado.app.di.AppDependencies
import com.kado.app.domain.model.BulkEditRule
import com.kado.app.domain.model.CardContent
import com.kado.app.domain.usecase.ApplyFindReplaceUseCase
import com.kado.app.domain.usecase.CardChange
import com.kado.app.domain.usecase.FindReplaceParams
import com.kado.app.domain.usecase.ValidateRegexUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

enum class BulkEditPhase { Editing, Preview }

data class BulkEditUiState(
    val phase: BulkEditPhase = BulkEditPhase.Editing,
    // Rule editor fields
    val frontFind: String = "",
    val frontReplace: String = "",
    val frontIsRegex: Boolean = false,
    val frontEnabled: Boolean = true,
    val backFind: String = "",
    val backReplace: String = "",
    val backIsRegex: Boolean = false,
    val backEnabled: Boolean = true,
    // Saved rules
    val savedRules: List<BulkEditRule> = emptyList(),
    val showSavedRulesDialog: Boolean = false,
    val showSaveRuleDialog: Boolean = false,
    // Preview
    val changes: List<CardChange> = emptyList(),
    val isProcessing: Boolean = false,
    val isDone: Boolean = false,
    // Validation
    val regexError: String? = null,
    val noChangesFound: Boolean = false
)

class BulkEditViewModel(private val deckId: Long) : ViewModel() {
    private val repository = AppDependencies.deckRepository
    private val ruleRepository = AppDependencies.bulkEditRuleRepository
    private val validateRegex = ValidateRegexUseCase()
    private val applyFindReplace = ApplyFindReplaceUseCase()

    private val _uiState = MutableStateFlow(BulkEditUiState())
    val uiState: StateFlow<BulkEditUiState> = _uiState

    init {
        ruleRepository.observeAll()
            .onEach { rules -> _uiState.value = _uiState.value.copy(savedRules = rules) }
            .launchIn(viewModelScope)
    }

    fun onFrontFindChange(value: String) {
        _uiState.value = _uiState.value.copy(frontFind = value, regexError = null, noChangesFound = false)
    }

    fun onFrontReplaceChange(value: String) {
        _uiState.value = _uiState.value.copy(frontReplace = value)
    }

    fun onBackFindChange(value: String) {
        _uiState.value = _uiState.value.copy(backFind = value, regexError = null, noChangesFound = false)
    }

    fun onBackReplaceChange(value: String) {
        _uiState.value = _uiState.value.copy(backReplace = value)
    }

    fun onFrontIsRegexChange(value: Boolean) {
        _uiState.value = _uiState.value.copy(frontIsRegex = value, regexError = null)
    }

    fun onBackIsRegexChange(value: Boolean) {
        _uiState.value = _uiState.value.copy(backIsRegex = value, regexError = null)
    }

    fun onFrontEnabledChange(value: Boolean) {
        _uiState.value = _uiState.value.copy(frontEnabled = value)
    }

    fun onBackEnabledChange(value: Boolean) {
        _uiState.value = _uiState.value.copy(backEnabled = value)
    }

    fun showSavedRulesDialog() {
        _uiState.value = _uiState.value.copy(showSavedRulesDialog = true)
    }

    fun dismissSavedRulesDialog() {
        _uiState.value = _uiState.value.copy(showSavedRulesDialog = false)
    }

    fun showSaveRuleDialog() {
        _uiState.value = _uiState.value.copy(showSaveRuleDialog = true)
    }

    fun dismissSaveRuleDialog() {
        _uiState.value = _uiState.value.copy(showSaveRuleDialog = false)
    }

    fun previewChanges() {
        val state = _uiState.value

        // Validate regex patterns
        if (state.frontEnabled && state.frontIsRegex && state.frontFind.isNotEmpty()) {
            val error = validateRegex(state.frontFind)
            if (error != null) {
                _uiState.value = state.copy(regexError = error)
                return
            }
        }
        if (state.backEnabled && state.backIsRegex && state.backFind.isNotEmpty()) {
            val error = validateRegex(state.backFind)
            if (error != null) {
                _uiState.value = state.copy(regexError = error)
                return
            }
        }

        _uiState.value = state.copy(isProcessing = true, noChangesFound = false)

        viewModelScope.launch {
            val cards = repository.getCards(deckId)
            val params = FindReplaceParams(
                frontFind = state.frontFind,
                frontReplace = state.frontReplace,
                frontIsRegex = state.frontIsRegex,
                frontEnabled = state.frontEnabled,
                backFind = state.backFind,
                backReplace = state.backReplace,
                backIsRegex = state.backIsRegex,
                backEnabled = state.backEnabled
            )
            val changes = applyFindReplace(cards, params)

            if (changes.isEmpty()) {
                _uiState.value = _uiState.value.copy(isProcessing = false, noChangesFound = true)
            } else {
                _uiState.value = _uiState.value.copy(
                    phase = BulkEditPhase.Preview,
                    changes = changes,
                    isProcessing = false
                )
            }
        }
    }

    fun commitChanges() {
        val selectedChanges = _uiState.value.changes.filter { it.isSelected }
        if (selectedChanges.isEmpty()) return

        _uiState.value = _uiState.value.copy(isProcessing = true)

        viewModelScope.launch {
            val updatedCards = selectedChanges.map { change ->
                change.card.copy(
                    front = CardContent.PlainText(change.newFront),
                    back = CardContent.PlainText(change.newBack)
                )
            }
            repository.bulkUpdateCards(updatedCards)
            _uiState.value = _uiState.value.copy(isProcessing = false, isDone = true)
        }
    }

    fun toggleCardSelection(cardId: Long) {
        _uiState.value = _uiState.value.copy(
            changes = _uiState.value.changes.map { change ->
                if (change.card.id == cardId) {
                    change.copy(isSelected = !change.isSelected)
                } else {
                    change
                }
            }
        )
    }

    fun selectAll() {
        _uiState.value = _uiState.value.copy(
            changes = _uiState.value.changes.map { it.copy(isSelected = true) }
        )
    }

    fun deselectAll() {
        _uiState.value = _uiState.value.copy(
            changes = _uiState.value.changes.map { it.copy(isSelected = false) }
        )
    }

    fun backToEditing() {
        _uiState.value = _uiState.value.copy(
            phase = BulkEditPhase.Editing,
            changes = emptyList()
        )
    }

    fun saveRule(name: String) {
        val state = _uiState.value
        viewModelScope.launch {
            ruleRepository.insert(
                BulkEditRule(
                    name = name,
                    frontFindPattern = state.frontFind,
                    frontReplacePattern = state.frontReplace,
                    frontIsRegex = state.frontIsRegex,
                    frontEnabled = state.frontEnabled,
                    backFindPattern = state.backFind,
                    backReplacePattern = state.backReplace,
                    backIsRegex = state.backIsRegex,
                    backEnabled = state.backEnabled,
                    createdAt = kotlin.time.Clock.System.now().epochSeconds
                )
            )
            _uiState.value = _uiState.value.copy(showSaveRuleDialog = false)
        }
    }

    fun loadRule(ruleId: Long) {
        viewModelScope.launch {
            val rule = ruleRepository.getById(ruleId) ?: return@launch
            _uiState.value = _uiState.value.copy(
                frontFind = rule.frontFindPattern,
                frontReplace = rule.frontReplacePattern,
                frontIsRegex = rule.frontIsRegex,
                frontEnabled = rule.frontEnabled,
                backFind = rule.backFindPattern,
                backReplace = rule.backReplacePattern,
                backIsRegex = rule.backIsRegex,
                backEnabled = rule.backEnabled,
                showSavedRulesDialog = false
            )
        }
    }

    fun deleteRule(ruleId: Long) {
        viewModelScope.launch {
            ruleRepository.deleteById(ruleId)
        }
    }
}
