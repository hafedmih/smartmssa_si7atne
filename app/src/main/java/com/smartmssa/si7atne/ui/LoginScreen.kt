package com.smartmssa.si7atne.ui

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.smartmssa.si7atne.WriteNfcActivity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: MainViewModel,
    onLoginSuccess: (String) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val loginState by viewModel.loginState.collectAsState()
    val context = LocalContext.current
    var menuExpanded by remember { mutableStateOf(false) }

    // --- KEY CHANGE: Wrap the screen in a Scaffold to hold the TopAppBar ---
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Login") },
                actions = {
                    // This creates the "..." menu icon
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    // This is the dropdown menu that appears when the icon is clicked
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Write to NFC Card") },
                            onClick = {
                                menuExpanded = false // Close the menu
                                // Launch the new WriteNfcActivity
                                context.startActivity(Intent(context, WriteNfcActivity::class.java))
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    // Set the background color to the primary color from your theme
                    containerColor = MaterialTheme.colorScheme.primary,
                    // Set the title text color to be readable on the primary color
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    // Set the navigation icon (back button) color
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    // Set the action icons (menu icon) color
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // Apply padding from the Scaffold
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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

            // The TextButton is now removed from here.

            when (val state = loginState) {
                is LoginState.Loading -> CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
                is LoginState.Success -> {
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
}