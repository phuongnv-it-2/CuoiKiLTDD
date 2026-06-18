package com.project24itb156.gglens.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.project24itb156.gglens.screens.CameraScreen
import com.project24itb156.gglens.screens.LoginScreen
import com.project24itb156.gglens.screens.RegisterScreen
import com.project24itb156.gglens.screens.ResultScreen
import com.project24itb156.gglens.viewmodel.AuthViewModel
import com.project24itb156.gglens.viewmodel.LensViewModel

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val CAMERA = "camera"
    const val RESULT = "result"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val lensViewModel: LensViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()

    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) Routes.CAMERA else Routes.LOGIN
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Routes.CAMERA) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) }
            )
        }
        composable(Routes.REGISTER) {
            RegisterScreen(
                viewModel = authViewModel,
                onRegisterSuccess = {
                    navController.navigate(Routes.CAMERA) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }
        composable(Routes.CAMERA) {
            CameraScreen(
                viewModel = lensViewModel,
                onImageCaptured = {
                    navController.navigate(Routes.RESULT)
                }
            )
        }
        composable(Routes.RESULT) {
            ResultScreen(
                viewModel = lensViewModel,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}