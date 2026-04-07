package com.kado.app.presentation.screens.algorithm_detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kado.app.domain.model.AlgorithmInfo
import com.kado.app.domain.model.ParameterInfo
import com.kado.app.presentation.components.KadoTopBar
import com.kado.app.presentation.localization.AppStrings
import com.kado.app.presentation.localization.S

@Composable
fun AlgorithmDetailScreen(
    algorithmId: String,
    focusParameter: String?,
    onBack: () -> Unit
) {
    val strings = S()
    val info = getAlgorithmInfo(algorithmId, strings)

    val title = when (algorithmId) {
        "sm2" -> strings.sm2Algorithm
        "fsrs" -> strings.fsrsAlgorithm
        else -> ""
    }

    Scaffold(
        topBar = {
            KadoTopBar(
                title = title,
                onBack = onBack
            )
        }
    ) { padding ->
        if (info == null) return@Scaffold

        val focusedParam = focusParameter?.let { key ->
            info.parameters.find { it.key == key }
        }

        var expanded by remember { mutableStateOf(false) }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Simple explanation card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = strings.howItWorks,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = info.simpleExplanation,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // 2. Focused parameter highlight
            if (focusedParam != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = focusedParam.name,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = focusedParam.description,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = focusedParam.hint,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }

            // 3. All parameters section
            item {
                Text(
                    text = strings.parameters,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            items(info.parameters) { parameter ->
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = parameter.name,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = parameter.description,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // 4. Expandable technical details
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = strings.technicalDetails,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = if (expanded) "▲" else "▼",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                AnimatedVisibility(visible = expanded) {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = info.technicalDetails,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun getAlgorithmInfo(algorithmId: String, strings: AppStrings): AlgorithmInfo? {
    return when (algorithmId) {
        "sm2" -> AlgorithmInfo(
            id = "sm2",
            simpleExplanation = strings.sm2SimpleExplanation,
            parameters = listOf(
                ParameterInfo("ease", strings.ease, strings.easeDescription, strings.easeHint)
            ),
            technicalDetails = strings.sm2TechnicalDetails
        )
        "fsrs" -> AlgorithmInfo(
            id = "fsrs",
            simpleExplanation = strings.fsrsSimpleExplanation,
            parameters = listOf(
                ParameterInfo("desiredRetention", strings.desiredRetention, strings.desiredRetentionExplanation, strings.desiredRetentionHint),
                ParameterInfo("learningSteps", strings.learningSteps, strings.learningStepsExplanation, strings.learningStepsHint),
                ParameterInfo("relearningSteps", strings.relearningSteps, strings.relearningStepsExplanation, strings.relearningStepsHint),
                ParameterInfo("maxInterval", strings.maxInterval, strings.maxIntervalExplanation, strings.maxIntervalHint),
                ParameterInfo("enableFuzzing", strings.enableFuzzing, strings.enableFuzzingExplanation, strings.enableFuzzingHint)
            ),
            technicalDetails = strings.fsrsTechnicalDetails
        )
        else -> null
    }
}
