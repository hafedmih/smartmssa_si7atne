package com.smartmssa.si7atne.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppBar(
    title: String,
    showBackButton: Boolean,
    onBackClicked: () -> Unit,
    onLogoutClicked: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text(text = title) },
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = onBackClicked) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        },
        actions = {
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Menu")
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Logout") },
                        onClick = {
                            menuExpanded = false
                            onLogoutClicked()
                        }
                    )
                }
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