package com.smartmssa.si7atne

import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord.createTextRecord
import android.nfc.NfcAdapter
import android.os.Bundle
import android.widget.Toast
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.smartmssa.si7atne.ui.*
import com.smartmssa.si7atne.ui.theme.Si7atneTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Si7atneTheme(dynamicColor = false) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()

                    // If token exists, skip login screen
                    val startDestination = if (SessionManager.authToken == null) "login" else "patient_code"

                    ObservePatientStateForNavigation(viewModel, navController)

                    NavHost(
                        navController = navController,
                        startDestination = startDestination
                    ) {
                        composable("login") {
                            LoginScreen(
                                viewModel = viewModel,
                                onLoginSuccess = { token ->
                                    SessionManager.authToken = token
                                    navController.navigate("patient_code") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("patient_code") {
                            PatientCodeScreen(viewModel)
                        }
                        composable("patient_details") {
                            PatientDetailsScreen(
                                onBackClicked = { navController.popBackStack() },
                                viewModel = viewModel,
                                onWriteNfcClicked = { nni ->
                                    enableNfcWriteMode(nni)
                                }
                            )
                        }
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
    private var nfcWriteMessage: String? = null
    private var writeModeEnabled = false

    private fun enableNfcWriteMode(message: String) {
        nfcWriteMessage = message
        writeModeEnabled = true
       // Toast.makeText(this, "Tap an NFC card to write NNI", Toast.LENGTH_LONG).show()
    }

    private fun handleNfcIntent(intent: Intent) {
        val action = intent.action

        if (writeModeEnabled && nfcWriteMessage != null) {
            writeTextToNfc(intent, nfcWriteMessage!!)
            writeModeEnabled = false
            nfcWriteMessage = null
            return
        }

        val patientCode = parsePatientCodeFromIntent(intent) ?: return
        viewModel.getPatientDetails(patientCode)
    }
    private fun writeTextToNfc(intent: Intent, text: String) {
        try {
            val tag = intent.getParcelableExtra<NdefMessage>(NfcAdapter.EXTRA_TAG)
            val ndefMessage = createTextRecord(text)
            val nfcTag = intent.getParcelableExtra<android.nfc.Tag>(NfcAdapter.EXTRA_TAG)

            val ndef = android.nfc.tech.Ndef.get(nfcTag)
            if (ndef != null) {
                ndef.connect()
                ndef.writeNdefMessage(ndefMessage)
                ndef.close()
                Toast.makeText(this, "NNI saved to NFC successfully!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Tag is not NDEF compatible", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Write error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    private fun createTextRecord(text: String): NdefMessage {
        val lang = "en"
        val langBytes = lang.toByteArray(Charsets.US_ASCII)
        val textBytes = text.toByteArray(Charsets.UTF_8)
        val payload = ByteArray(1 + langBytes.size + textBytes.size)

        payload[0] = langBytes.size.toByte()
        System.arraycopy(langBytes, 0, payload, 1, langBytes.size)
        System.arraycopy(textBytes, 0, payload, 1 + langBytes.size, textBytes.size)

        val record = android.nfc.NdefRecord(
            android.nfc.NdefRecord.TNF_WELL_KNOWN,
            android.nfc.NdefRecord.RTD_TEXT,
            ByteArray(0),
            payload
        )

        return NdefMessage(arrayOf(record))
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

@Composable
private fun ObservePatientStateForNavigation(viewModel: MainViewModel, navController: NavController) {
    val patientState by viewModel.patientState.collectAsState()
    LaunchedEffect(patientState) {
        if (patientState is PatientState.Success) {
            if (navController.currentDestination?.route != "patient_details") {
                navController.navigate("patient_details")
            }
        }
    }
}