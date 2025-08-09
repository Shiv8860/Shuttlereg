package com.example.shuttlereg.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.shuttlereg.presentation.ui.screens.*
import com.example.shuttlereg.presentation.viewmodel.AuthViewModel

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Auth : Screen("auth")
    object TournamentList : Screen("tournament_list")
    object Registration : Screen("registration/{tournamentId}") {
        fun createRoute(tournamentId: String) = "registration/$tournamentId"
    }
    object Profile : Screen("profile")
}

@Composable
fun ShuttleRegNavigation(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val isSignedIn by authViewModel.isSignedIn.collectAsState()
    
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashComplete = {
                    navController.navigate(
                        if (isSignedIn) Screen.TournamentList.route else Screen.Auth.route
                    ) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Auth.route) {
            AuthScreen(
                onAuthSuccess = {
                    navController.navigate(Screen.TournamentList.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.TournamentList.route) {
            TournamentListScreen(
                onTournamentSelected = { tournament ->
                    navController.navigate(Screen.Registration.createRoute(tournament.id))
                },
                onNavigateToAuth = {
                    navController.navigate(Screen.Profile.route)
                }
            )
        }
        
        composable(Screen.Registration.route) { backStackEntry ->
            val tournamentId = backStackEntry.arguments?.getString("tournamentId") ?: ""
            RegistrationScreen(
                tournamentId = tournamentId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onRegistrationComplete = {
                    navController.navigate(Screen.TournamentList.route) {
                        popUpTo(Screen.TournamentList.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onSignOut = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}