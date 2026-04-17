package com.kado.app.presentation.screens.stats

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kado.app.domain.model.Deck
import com.kado.app.domain.model.StatsRange
import com.kado.app.presentation.components.ConfirmDialog
import com.kado.app.presentation.components.EmptyState
import com.kado.app.presentation.components.KadoTopBar
import com.kado.app.presentation.components.LoadingState
import com.kado.app.presentation.components.StatBar
import com.kado.app.presentation.components.charts.BarChart
import com.kado.app.presentation.components.charts.BarChartEntry
import com.kado.app.presentation.components.charts.CalendarHeatmap
import com.kado.app.presentation.components.charts.ChartLegend
import com.kado.app.presentation.components.charts.LegendItem
import com.kado.app.presentation.components.charts.StackedBarChart
import com.kado.app.presentation.localization.S
import kotlinx.coroutines.launch

private val statsTabs = listOf("Calendar", "Reviews", "Answers", "Time of day")

// Generous min height so switching tabs doesn't resize the page; sized for the tallest tab
// (Time of day renders two 200dp charts stacked with labels).
private val TAB_CONTENT_MIN_HEIGHT = 520.dp

@Composable
fun StatsScreen(
    initialDeckIds: List<Long>,
    onBack: () -> Unit,
    vm: StatsViewModel = viewModel { StatsViewModel(initialDeckIds) }
) {
    val uiState by vm.uiState.collectAsState()
    var showResetDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        ConfirmDialog(
            title = S().resetProgress,
            message = S().resetProgressMessage,
            confirmLabel = S().reset,
            onConfirm = {
                showResetDialog = false
                vm.resetProgress()
            },
            onDismiss = { showResetDialog = false }
        )
    }

    Scaffold(topBar = { KadoTopBar(title = S().statistics, onBack = onBack) }) { padding ->
        if (uiState.isLoading) {
            LoadingState(Modifier.padding(padding))
            return@Scaffold
        }
        StatsScreenBody(
            uiState = uiState,
            padding = padding,
            onToggleDeck = vm::toggleDeck,
            onSelectAllDecks = vm::selectAllDecks,
            onSetRange = vm::setRange,
            onResetRequested = { showResetDialog = true }
        )
    }
}

@Composable
private fun StatsScreenBody(
    uiState: StatsUiState,
    padding: PaddingValues,
    onToggleDeck: (Long) -> Unit,
    onSelectAllDecks: () -> Unit,
    onSetRange: (StatsRange) -> Unit,
    onResetRequested: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(padding)
            .padding(16.dp)
    ) {
        Text(uiState.title, style = MaterialTheme.typography.headlineSmall)
        Text(
            uiState.rangeLabel,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))

        DeckSelectorChipRow(
            decks = uiState.allDecks,
            selected = uiState.selectedDeckIds,
            onSelectAll = onSelectAllDecks,
            onToggle = onToggleDeck
        )
        Spacer(Modifier.height(12.dp))

        StatsRangeSelector(range = uiState.range, onSetRange = onSetRange)
        Spacer(Modifier.height(20.dp))

        CardStateSummary(uiState)
        Spacer(Modifier.height(24.dp))

        Text("Review activity", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        if (uiState.snapshot.isEmpty) {
            EmptyStatsHint()
        } else {
            StatsTabs(uiState)
        }

        if (uiState.canReset) {
            Spacer(Modifier.height(32.dp))
            OutlinedButton(onClick = onResetRequested, modifier = Modifier.fillMaxWidth()) {
                Text(S().resetProgress, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun DeckSelectorChipRow(
    decks: List<Deck>,
    selected: Set<Long>,
    onSelectAll: () -> Unit,
    onToggle: (Long) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selected.isEmpty(),
            onClick = onSelectAll,
            label = { Text("All decks") }
        )
        decks.forEach { deck ->
            FilterChip(
                selected = deck.id in selected,
                onClick = { onToggle(deck.id) },
                label = { Text(deck.name) }
            )
        }
    }
}

@Composable
private fun StatsRangeSelector(range: StatsRange, onSetRange: (StatsRange) -> Unit) {
    val ranges = StatsRange.entries
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        ranges.forEachIndexed { index, r ->
            SegmentedButton(
                selected = range == r,
                onClick = { onSetRange(r) },
                shape = SegmentedButtonDefaults.itemShape(index, ranges.size)
            ) {
                Text(
                    when (r) {
                        StatsRange.Days30 -> "30d"
                        StatsRange.Days90 -> "90d"
                        StatsRange.Year -> "1y"
                    }
                )
            }
        }
    }
}

@Composable
private fun CardStateSummary(uiState: StatsUiState) {
    val stats = uiState.cardStats
    StatBar(S().newLabel, stats.newCards, stats.totalCards, MaterialTheme.colorScheme.tertiary)
    Spacer(Modifier.height(12.dp))
    StatBar(S().learning, stats.learningCards, stats.totalCards, MaterialTheme.colorScheme.error)
    Spacer(Modifier.height(12.dp))
    StatBar(S().young, stats.youngCards, stats.totalCards, MaterialTheme.colorScheme.primary)
    Spacer(Modifier.height(12.dp))
    StatBar(S().mature, stats.matureCards, stats.totalCards, MaterialTheme.colorScheme.secondary)
    Spacer(Modifier.height(16.dp))
    Text(
        S().dueNow(stats.dueNow),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary
    )
    Text(S().totalCards(stats.totalCards), style = MaterialTheme.typography.bodyLarge)
}

@Composable
private fun EmptyStatsHint() {
    EmptyState(
        title = "No reviews yet",
        subtitle = "Review some cards to start tracking your progress.",
        modifier = Modifier.height(200.dp)
    )
}

@Composable
private fun StatsTabs(uiState: StatsUiState) {
    val pagerState = rememberPagerState(pageCount = { statsTabs.size })
    val scope = rememberCoroutineScope()

    PrimaryTabRow(selectedTabIndex = pagerState.currentPage) {
        statsTabs.forEachIndexed { index, title ->
            Tab(
                selected = pagerState.currentPage == index,
                onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                text = { Text(title) }
            )
        }
    }
    Spacer(Modifier.height(12.dp))
    HorizontalPager(
        verticalAlignment = Alignment.Top,
        state = pagerState,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = TAB_CONTENT_MIN_HEIGHT)
    ) { page ->
        when (page) {
            0 -> CalendarTab(uiState)
            1 -> ReviewsTab(uiState)
            2 -> AnswersTab(uiState)
            3 -> TimeOfDayTab(uiState)
            else -> Unit
        }
    }
}

@Composable
private fun CalendarTab(uiState: StatsUiState) {
    Column {
        Text(
            "Reviews per day — darker = more reviews",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        CalendarHeatmap(cells = uiState.snapshot.heatmap)
    }
}

@Composable
private fun ReviewsTab(uiState: StatsUiState) {
    val daily = uiState.charts.daily
    Column {
        ChartLegend(
            items = listOf(
                LegendItem("Reviews / day", MaterialTheme.colorScheme.primary),
                LegendItem("Cumulative", MaterialTheme.colorScheme.tertiary)
            )
        )
        Spacer(Modifier.height(8.dp))
        BarChart(
            bars = daily.bars.map { BarChartEntry(it.value, it.cumulative) },
            yMax = daily.yMax,
            xLabels = daily.xLabels,
            yMaxSecondary = daily.yMaxSecondary,
            showCumulativeLine = true
        )
    }
}

@Composable
private fun AnswersTab(uiState: StatsUiState) {
    Column {
        ChartLegend(
            items = listOf(
                LegendItem("Again", MaterialTheme.colorScheme.error),
                LegendItem("Hard", MaterialTheme.colorScheme.secondary),
                LegendItem("Good", MaterialTheme.colorScheme.tertiary),
                LegendItem("Easy", MaterialTheme.colorScheme.primary)
            )
        )
        Spacer(Modifier.height(8.dp))
        StackedBarChart(
            entries = uiState.charts.ratingsBars,
            yMax = uiState.charts.ratingsYMax,
            xLabels = uiState.charts.ratingsXLabels
        )
    }
}

@Composable
private fun TimeOfDayTab(uiState: StatsUiState) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Hour of day", style = MaterialTheme.typography.titleSmall)
        Text(
            "When you review, by hour",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        BarChart(
            bars = uiState.charts.hourly.bars.map { BarChartEntry(it.value) },
            yMax = uiState.charts.hourly.yMax,
            xLabels = uiState.charts.hourly.xLabels
        )
        Spacer(Modifier.height(20.dp))
        Text("Day of week", style = MaterialTheme.typography.titleSmall)
        Text(
            "When you review, by weekday",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        BarChart(
            bars = uiState.charts.weekly.bars.map { BarChartEntry(it.value) },
            yMax = uiState.charts.weekly.yMax,
            xLabels = uiState.charts.weekly.xLabels
        )
    }
}
