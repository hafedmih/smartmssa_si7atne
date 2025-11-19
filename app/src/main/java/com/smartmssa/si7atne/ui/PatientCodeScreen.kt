package com.smartmssa.si7atne.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun PatientCodeScreen(
    viewModel: MainViewModel,
    onLogoutClicked: () -> Unit // Add this parameter
) {
    var patientCode by remember { mutableStateOf("") }
    val patientState by viewModel.patientState.collectAsState()

    // --- KEY CHANGE: Wrap the screen in a Scaffold ---
    Scaffold(
        topBar = {
            MainAppBar(
                title = "Find Patient",
                showBackButton = false, // This is the root screen of the main graph
                onBackClicked = { /* No action */ },
                onLogoutClicked = onLogoutClicked
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // Apply padding from the Scaffold
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Nfc,
                    contentDescription = "NFC Icon",
                    modifier = Modifier.size(100.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Scan Patient Card",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "OR",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(32.dp))
                OutlinedTextField(
                    value = patientCode,
                    onValueChange = { patientCode = it },
                    label = { Text("Enter Patient Code Manually") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        viewModel.getPatientDetails(patientCode)
                    },
                    enabled = patientCode.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Search Patient")
                }

                if (patientState is PatientState.Loading || patientState is PatientState.Error) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (patientState is PatientState.Loading) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Searching for patient...")
                        } else if (patientState is PatientState.Error) {
                            Text(
                                text = (patientState as PatientState.Error).message,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }
    }
}