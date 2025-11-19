package com.smartmssa.si7atne.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    viewModel: MainViewModel,
    onLoginSuccess: (String) -> Unit
) {
    var username by remember { mutableStateOf("test-api@smartms.com") }
    var password by remember { mutableStateOf("test1") }
    val loginState by viewModel.loginState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- KEY CHANGE ---
        // This card will only be visible when a code has been scanned pre-login.


        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { viewModel.login(username, password) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }

        when (val state = loginState) {
            is LoginState.Loading -> CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
            is LoginState.Success -> {
                // LaunchedEffect is crucial here to prevent multiple calls
                LaunchedEffect(state) {
                    onLoginSuccess(state.token)
                }
            }
            is LoginState.Error -> Text(
                state.message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 16.dp)
            )
            else -> {}
        }
    }
}