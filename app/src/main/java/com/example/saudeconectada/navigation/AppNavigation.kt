package com.example.saudeconectada.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.saudeconectada.ui.screens.DoctorDashboardScreen
import com.example.saudeconectada.ui.screens.LoginScreen
import com.example.saudeconectada.ui.screens.PatientDashboardScreen
import com.example.saudeconectada.ui.screens.ProfileSelectionScreen
import com.example.saudeconectada.ui.screens.SignUpScreen

sealed class Screen(val route: String) {
    object Login : Screen("login_screen")
    object SignUp : Screen("signup_screen/{userType}") {
        fun createRoute(userType: String) = "signup_screen/$userType"
    }
    object ProfileSelection : Screen("profile_selection_screen")
    object PatientDashboard : Screen("patient_dashboard_screen")
    object DoctorDashboard : Screen("doctor_dashboard_screen")
}

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }

        composable(Screen.ProfileSelection.route) {
            ProfileSelectionScreen(navController = navController)
        }

        composable(
            route = Screen.SignUp.route,
            arguments = listOf(navArgument("userType") { type = NavType.StringType })
        ) { backStackEntry ->
            val userType = backStackEntry.arguments?.getString("userType") ?: "patient"
            SignUpScreen(navController = navController, userType = userType)
        }

        composable(Screen.PatientDashboard.route) {
            PatientDashboardScreen(navController = navController)
        }

        composable(Screen.DoctorDashboard.route) {
            DoctorDashboardScreen(navController = navController)
        }
    }
}
