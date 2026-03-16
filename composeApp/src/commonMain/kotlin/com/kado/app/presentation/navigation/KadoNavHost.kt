package com.kado.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.kado.app.presentation.screens.card_edit.CardEditScreen
import com.kado.app.presentation.screens.connection.ConnectionScreen
import com.kado.app.presentation.screens.deck_detail.DeckDetailScreen
import com.kado.app.presentation.screens.deck_edit.DeckEditScreen
import com.kado.app.presentation.screens.home.HomeScreen
import com.kado.app.presentation.screens.review.ReviewScreen
import com.kado.app.presentation.screens.settings.AboutScreen
import com.kado.app.presentation.screens.settings.AppSettingsScreen
import com.kado.app.presentation.screens.settings.DonateScreen
import com.kado.app.presentation.screens.settings.SettingsScreen
import com.kado.app.presentation.screens.stats.StatsScreen
import com.kado.app.presentation.screens.partition.PartitionScreen
import com.kado.app.presentation.screens.transfer.TransferScreen

@Composable
fun KadoNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier
    ) {
        composable<HomeRoute> {
            HomeScreen(
                onDeckClick = { deckId -> navController.navigate(DeckDetailRoute(deckId)) },
                onCreateDeck = { navController.navigate(DeckEditRoute()) },
                onConnectionClick = { navController.navigate(ConnectionRoute) },
                onSettingsClick = { navController.navigate(SettingsRoute) }
            )
        }

        composable<DeckDetailRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<DeckDetailRoute>()
            DeckDetailScreen(
                deckId = route.deckId,
                onBack = { navController.popBackStack() },
                onEditDeck = { navController.navigate(DeckEditRoute(route.deckId)) },
                onAddCard = { navController.navigate(CardEditRoute(route.deckId)) },
                onEditCard = { cardId -> navController.navigate(CardEditRoute(route.deckId, cardId)) },
                onReview = { navController.navigate(ReviewRoute(route.deckId)) },
                onStats = { navController.navigate(StatsRoute(route.deckId)) },
                onTransfer = { navController.navigate(TransferRoute(route.deckId)) },
                onPartition = { navController.navigate(PartitionRoute(route.deckId)) },
                onReviewSubDeck = { subDeckIndex -> navController.navigate(ReviewRoute(route.deckId, subDeckIndex)) },
                onTransferSubDeck = { subDeckIndex -> navController.navigate(TransferRoute(route.deckId, subDeckIndex)) }
            )
        }

        composable<DeckEditRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<DeckEditRoute>()
            DeckEditScreen(
                deckId = route.deckId,
                onBack = { navController.popBackStack() },
                onDeleted = { navController.popBackStack(HomeRoute, inclusive = false) }
            )
        }

        composable<CardEditRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<CardEditRoute>()
            CardEditScreen(
                deckId = route.deckId,
                cardId = route.cardId,
                onBack = { navController.popBackStack() }
            )
        }

        composable<ReviewRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<ReviewRoute>()
            val lifecycleOwner = LocalLifecycleOwner.current
            val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()
            val isInteractive = lifecycleState.isAtLeast(Lifecycle.State.RESUMED)
            val subDeckIndex = if (route.subDeckIndex == -1) null else route.subDeckIndex
            ReviewScreen(
                deckId = route.deckId,
                subDeckIndex = subDeckIndex,
                onBack = { navController.popBackStack() }
            )
        }

        composable<StatsRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<StatsRoute>()
            StatsScreen(
                deckId = route.deckId,
                onBack = { navController.popBackStack() }
            )
        }

        composable<ConnectionRoute> {
            ConnectionScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable<TransferRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<TransferRoute>()
            val subDeckIndex = if (route.subDeckIndex == -1) null else route.subDeckIndex
            TransferScreen(
                deckId = route.deckId,
                subDeckIndex = subDeckIndex,
                onBack = { navController.popBackStack() }
            )
        }

        composable<PartitionRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<PartitionRoute>()
            PartitionScreen(
                deckId = route.deckId,
                onBack = { navController.popBackStack() }
            )
        }

        composable<SettingsRoute> {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onAboutClick = { navController.navigate(AboutRoute) },
                onAppSettingsClick = { navController.navigate(AppSettingsRoute) },
                onDonateClick = { navController.navigate(DonateRoute) }
            )
        }

        composable<AboutRoute> {
            AboutScreen(onBack = { navController.popBackStack() })
        }

        composable<AppSettingsRoute> {
            AppSettingsScreen(onBack = { navController.popBackStack() })
        }

        composable<DonateRoute> {
            DonateScreen(onBack = { navController.popBackStack() })
        }
    }
}
