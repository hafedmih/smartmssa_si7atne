package com.smartmssa.si7atne

import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.smartmssa.si7atne.ui.*
import com.smartmssa.si7atne.ui.theme.Si7atneTheme

// Define clear, reusable routes for our graphs and screens
object AppRoutes {
    const val LOGIN_GRAPH = "login_graph"
    const val MAIN_GRAPH = "main_graph"
    const val LOGIN_SCREEN = "login"
    const val PATIENT_CODE_SCREEN = "patient_code"
    const val PATIENT_DETAILS_SCREEN = "patient_details"
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Si7atneTheme(dynamicColor = false) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()

                    // The logout action is defined once here
                    val onLogout: () -> Unit = {
                        SessionManager.authToken = null
                        // Navigate to the login graph and clear the entire app history
                        navController.navigate(AppRoutes.LOGIN_GRAPH) {
                            popUpTo(0) { // Pops the entire back stack
                                inclusive = true
                            }
                        }
                    }

                    // This observer handles automatic navigation when a patient is found
                    ObservePatientStateForNavigation(viewModel, navController)

                    // Determine the starting point based on whether the user is already logged in
                    val startDestination = if (SessionManager.authToken == null) AppRoutes.LOGIN_GRAPH else AppRoutes.MAIN_GRAPH

                    NavHost(
                        navController = navController,
                        startDestination = startDestination
                    ) {
                        // Graph 1: The "unauthenticated" part of the app
                        loginGraph(navController, viewModel)

                        // Graph 2: The "authenticated" part of the app
                        mainGraph(navController, viewModel, onLogout)
                    }
                }
            }
        }
        intent?.let { handleNfcIntent(it) }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNfcIntent(intent)
    }

    private fun handleNfcIntent(intent: Intent) {
        val patientCode = parsePatientCodeFromIntent(intent) ?: return
        viewModel.getPatientDetails(patientCode)
    }

    private fun parsePatientCodeFromIntent(intent: Intent): String? {
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            val rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
            rawMessages?.map { it as NdefMessage }?.forEach { message ->
                message.records.forEach { record ->
                    val payload = record.payload
                    val languageCodeLength = payload[0].toInt() and 0x3F
                    return String(payload, languageCodeLength + 1, payload.size - languageCodeLength - 1, Charsets.UTF_8)
                }
            }
        }
        return null
    }
}

/**
 * Defines the navigation graph for the unauthenticated user flow.
 * Its only screen is the login screen.
 */
private fun NavGraphBuilder.loginGraph(navController: NavController, viewModel: MainViewModel) {
    navigation(
        startDestination = AppRoutes.LOGIN_SCREEN,
        route = AppRoutes.LOGIN_GRAPH
    ) {
        composable(AppRoutes.LOGIN_SCREEN) {
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = { token ->
                    SessionManager.authToken = token
                    // Navigate to the main app graph and destroy the login graph from history
                    navController.navigate(AppRoutes.MAIN_GRAPH) {
                        popUpTo(AppRoutes.LOGIN_GRAPH) {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}

/**
 * Defines the navigation graph for the authenticated user flow.
 */
private fun NavGraphBuilder.mainGraph(navController: NavController, viewModel: MainViewModel, onLogout: () -> Unit) {
    navigation(
        startDestination = AppRoutes.PATIENT_CODE_SCREEN,
        route = AppRoutes.MAIN_GRAPH
    ) {
        composable(AppRoutes.PATIENT_CODE_SCREEN) {
            PatientCodeScreen(
                viewModel = viewModel,
                onLogoutClicked = onLogout
            )
        }
        composable(AppRoutes.PATIENT_DETAILS_SCREEN) {
            PatientDetailsScreen(
                onBackClicked = { navController.popBackStack() },
                viewModel = viewModel,
                onLogoutClicked = onLogout
            )
        }
    }
}


/**
 * A dedicated observer that listens to the patientState and triggers navigation to the details screen.
 */
@Composable
private fun ObservePatientStateForNavigation(viewModel: MainViewModel, navController: NavController) {
    val patientState by viewModel.patientState.collectAsState()
    LaunchedEffect(patientState) {
        if (patientState is PatientState.Success) {
            if (navController.currentDestination?.route != AppRoutes.PATIENT_DETAILS_SCREEN) {
                navController.navigate(AppRoutes.PATIENT_DETAILS_SCREEN)
            }
        }
    }
}