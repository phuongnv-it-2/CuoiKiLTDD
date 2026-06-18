package com.project24itb156.gglens.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.project24itb156.gglens.screens.*
import com.project24itb156.gglens.viewmodel.*

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val CAMERA = "camera"
    const val RESULT = "result"
    const val CHAT = "chat"
    const val HISTORY = "history"
    const val AI_DETAIL = "ai_detail/{id}"
    const val QR = "qr"

    fun aiDetail(id: String) = "ai_detail/$id"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val lensViewModel: LensViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()
    val chatViewModel: ChatViewModel = viewModel()
    val historyViewModel: HistoryViewModel = viewModel()

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
                },
                onOpenChat = {
                    navController.navigate(Routes.CHAT)
                },
                onOpenHistory = {
                    navController.navigate(Routes.HISTORY)
                },
                onOpenQr = {
                    navController.navigate(Routes.QR)
                }
            )
        }
        composable(Routes.CHAT) {
            ChatScreen(
                viewModel = chatViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.HISTORY) {
            HistoryScreen(
                viewModel = historyViewModel,
                onBack = { navController.popBackStack() },
                onItemClick = { aiId ->
                    navController.navigate(Routes.aiDetail(aiId))
                }
            )
        }
        composable(
            route = Routes.AI_DETAIL,
            arguments = listOf(navArgument("id") { type = NavType.StringType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: ""
            AiResultDetailScreen(
                aiResultId = id,
                onBack = { navController.popBackStack() }
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
        composable(Routes.QR) {
            val qrViewModel: QrViewModel = viewModel()
            QrScreen(
                viewModel = qrViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
