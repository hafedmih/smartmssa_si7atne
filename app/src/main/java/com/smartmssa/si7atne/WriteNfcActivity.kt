package com.smartmssa.si7atne

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.smartmssa.si7atne.ui.theme.Si7atneTheme
import java.io.IOException
import androidx.compose.ui.platform.LocalContext
import android.app.Activity
import androidx.compose.ui.Alignment

class WriteNfcActivity : ComponentActivity() {

    private var nfcAdapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null

    // This state variable holds the code we intend to write.
    private var codeToWrite by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC is not available on this device.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Create a PendingIntent to handle NFC intents for this activity.
        // FLAG_MUTABLE is required for Android 12+
        val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)

        setContent {
            Si7atneTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    WriteNfcScreen(
                        isReadyToWrite = codeToWrite != null,
                        codePrepared = codeToWrite,
                        onWriteClicked = { code ->
                            if (code.isBlank()) {
                                Toast.makeText(this, "Please enter a code to write.", Toast.LENGTH_SHORT).show()
                            } else {
                                codeToWrite = code
                                Toast.makeText(this, "Ready to write. Please tap an NFC card.", Toast.LENGTH_LONG).show()
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Enable foreground dispatch to intercept ANY tag while this activity is visible.
        // This is crucial for catching unformatted tags.
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, null, null)
    }

    override fun onPause() {
        super.onPause()
        // Disable foreground dispatch when the app is paused.
        nfcAdapter?.disableForegroundDispatch(this)
    }

    // This method is called whenever an NFC tag is detected while the activity is in the foreground.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val message = codeToWrite
        // We only proceed if the user has clicked "Prepare to Write"
        if (message != null) {
            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
            if (tag != null) {
                // Attempt to write the message to the detected tag.
                writeMessageToTag(createTextRecord(message), tag)
            }
            // Reset the state so we don't accidentally write again.
            codeToWrite = null
        }
    }

    /**
     * Robust function to write an NDEF message to a tag.
     * It handles both already-formatted tags and unformatted (but formattable) tags.
     */
    private fun writeMessageToTag(ndefMessage: NdefMessage, tag: Tag) {
        try {
            // First, try to get an Ndef instance. This works for already formatted tags.
            val ndef = Ndef.get(tag)
            if (ndef != null) {
                ndef.connect()
                if (!ndef.isWritable) {
                    Toast.makeText(this, "This NFC tag is read-only.", Toast.LENGTH_SHORT).show()
                    ndef.close()
                    return
                }
                if (ndef.maxSize < ndefMessage.toByteArray().size) {
                    Toast.makeText(this, "Not enough space on this NFC tag.", Toast.LENGTH_SHORT).show()
                    ndef.close()
                    return
                }
                ndef.writeNdefMessage(ndefMessage)
                ndef.close()
                Toast.makeText(this, "Code saved to NFC successfully!", Toast.LENGTH_LONG).show()
            } else {
                // If the tag is not NDEF formatted, try to format it.
                val ndefFormatable = NdefFormatable.get(tag)
                if (ndefFormatable != null) {
                    ndefFormatable.connect()
                    ndefFormatable.format(ndefMessage)
                    ndefFormatable.close()
                    Toast.makeText(this, "New card formatted and code saved!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "This tag type is not supported for writing.", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: IOException) {
            // Handle IO exceptions during connect() or write()
            e.printStackTrace()
            Toast.makeText(this, "Write failed due to a communication error.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            // Handle other exceptions (e.g., FormatException)
            e.printStackTrace()
            Toast.makeText(this, "Write failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Creates a standard NDEF Text Record.
     */
    private fun createTextRecord(text: String): NdefMessage {
        val lang = "en" // Language code
        val langBytes = lang.toByteArray(Charsets.US_ASCII)
        val textBytes = text.toByteArray(Charsets.UTF_8)

        // The payload format includes a status byte, the language code, and the text
        val payload = ByteArray(1 + langBytes.size + textBytes.size)
        payload[0] = langBytes.size.toByte() // Status byte

        System.arraycopy(langBytes, 0, payload, 1, langBytes.size)
        System.arraycopy(textBytes, 0, payload, 1 + langBytes.size, textBytes.size)

        val record = android.nfc.NdefRecord(
            android.nfc.NdefRecord.TNF_WELL_KNOWN,
            android.nfc.NdefRecord.RTD_TEXT,
            ByteArray(0), // No ID
            payload
        )

        return NdefMessage(arrayOf(record))
    }
}


/**
 * The user interface for the writing screen.
 */
@Composable
fun WriteNfcScreen(
    isReadyToWrite: Boolean,
    codePrepared: String?,
    onWriteClicked: (String) -> Unit
) {
    var code by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
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
            text = "Write Code to NFC Card",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(32.dp))
        OutlinedTextField(
            value = code,
            onValueChange = { code = it },
            label = { Text("Enter Patient Code") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { onWriteClicked(code) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Prepare to Write")
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Show a message to the user when the app is ready to write
        if (isReadyToWrite && codePrepared != null) {
            Text(
                text = "Ready to write code '$codePrepared'.\nPlease tap an NFC card on the back of the phone.",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Button(
            onClick = {
                // To close an activity from a Composable, we cast the context and call finish()
                (context as? Activity)?.finish()
            },
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text("Back to Main App")
        }
    }
}