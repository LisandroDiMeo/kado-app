package com.kado.app.presentation.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kado.app.di.AppDependencies
import com.kado.app.domain.model.Deck
import com.kado.app.domain.model.StatsRange
import com.kado.app.domain.model.StatsSnapshot
import com.kado.app.domain.repository.DeckRepository
import com.kado.app.domain.usecase.CalculateDeckStatsUseCase
import com.kado.app.domain.usecase.DeckStats
import com.kado.app.domain.usecase.GetStatsSnapshotUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone

data class StatsUiState(
    val allDecks: List<Deck> = emptyList(),
    val selectedDeckIds: Set<Long> = emptySet(),
    val range: StatsRange = StatsRange.Days30,
    val cardStats: DeckStats = emptyDeckStats(),
    val snapshot: StatsSnapshot = StatsSnapshot.EMPTY,
    val charts: StatsChartsState = StatsChartsState.EMPTY,
    val isLoading: Boolean = true
) {
    val isAllDecks: Boolean get() = selectedDeckIds.isEmpty()

    val canReset: Boolean get() = selectedDeckIds.size == 1

    val title: String
        get() = when {
            isAllDecks -> "All decks"
            selectedDeckIds.size == 1 -> allDecks.firstOrNull { it.id in selectedDeckIds }?.name ?: ""
            else -> "${selectedDeckIds.size} decks"
        }

    val rangeLabel: String get() = when (range) {
        StatsRange.Days30 -> "Last 30 days"
        StatsRange.Days90 -> "Last 90 days"
        StatsRange.Year -> "Last year"
    }
}

private fun emptyDeckStats() = DeckStats(
    deck = null,
    totalCards = 0,
    newCards = 0,
    learningCards = 0,
    youngCards = 0,
    matureCards = 0,
    dueNow = 0
)

@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModel(
    initialDeckIds: List<Long>,
    private val repository: DeckRepository = AppDependencies.deckRepository,
    private val snapshotUseCase: GetStatsSnapshotUseCase = GetStatsSnapshotUseCase(repository),
    private val statsMapper: (StatsSnapshot) -> StatsChartsState = StatsChartsMapper::map,
    private val clock: () -> Long = { kotlin.time.Clock.System.now().epochSeconds },
    private val timeZone: TimeZone = TimeZone.currentSystemDefault()
) : ViewModel() {

    private val selected = MutableStateFlow(initialDeckIds.toSet())
    private val range = MutableStateFlow(StatsRange.Days30)
    private val cardStats = MutableStateFlow(emptyDeckStats())

    private val allDecks: StateFlow<List<Deck>> = repository.observeDecks()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val snapshot: StateFlow<StatsSnapshot> =
        combine(selected, range) { ids, r -> ids to r }
            .flatMapLatest { (ids, r) -> snapshotUseCase(ids.toList(), r, clock(), timeZone) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, StatsSnapshot.EMPTY)

    val uiState: StateFlow<StatsUiState> = combine(
        allDecks,
        selected,
        range,
        cardStats,
        snapshot
    ) { decks, sel, r, cs, snap ->
        StatsUiState(
            allDecks = decks,
            selectedDeckIds = sel,
            range = r,
            cardStats = cs,
            snapshot = snap,
            charts = statsMapper(snap),
            isLoading = false
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, StatsUiState())

    init {
        selected.onEach { loadCardStats(it) }.launchIn(viewModelScope)
    }

    fun toggleDeck(deckId: Long) {
        selected.value = if (deckId in selected.value) {
            selected.value - deckId
        } else {
            selected.value + deckId
        }
    }

    fun selectAllDecks() {
        selected.value = emptySet()
    }

    fun setRange(newRange: StatsRange) {
        range.value = newRange
    }

    fun resetProgress() {
        val id = selected.value.singleOrNull() ?: return
        viewModelScope.launch {
            repository.resetProgress(id)
            loadCardStats(setOf(id))
        }
    }

    private suspend fun loadCardStats(ids: Set<Long>) {
        val now = clock()
        val states = repository.getCardStates(ids.toList())
        val totalCards = if (ids.isEmpty()) {
            allDecks.value.sumOf { repository.getCardCount(it.id) }
        } else {
            ids.sumOf { repository.getCardCount(it) }
        }
        val deck = if (ids.size == 1) repository.getDeck(ids.first()) else null
        cardStats.value = CalculateDeckStatsUseCase.calculateStats(deck, totalCards, states, now)
    }
}
