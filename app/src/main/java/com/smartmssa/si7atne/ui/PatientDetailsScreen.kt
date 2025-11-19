package com.smartmssa.si7atne.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smartmssa.si7atne.data.MedicalHistoryItem
import com.smartmssa.si7atne.data.Medicine
import com.smartmssa.si7atne.data.Prescription
import com.smartmssa.si7atne.data.Treatment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDetailsScreen(
    onBackClicked: () -> Unit,
    viewModel: MainViewModel,
    onLogoutClicked: () -> Unit

) {
    val patientState by viewModel.patientState.collectAsState()

    // This effect guarantees the patient state is reset when the user leaves this screen.
    // This is vital for correct back navigation.
    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetPatientState()
        }
    }

    Scaffold(
        topBar = {
            MainAppBar(
                title = "Patient Medical Record",
                showBackButton = true, // This screen needs a back button
                onBackClicked = onBackClicked,
                onLogoutClicked = onLogoutClicked
            )
        },

    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when (val state = patientState) {
                // ... (The content of the screen is the same as before)
                is PatientState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item { PatientInfoCard(state = state) }
                        state.treatments?.let {
                            if (it.treatments.isNotEmpty()) {
                                item { SectionHeader("Treatments") }
                                items(it.treatments) { treatment -> TreatmentCard(treatment) }
                            }
                        }
                        state.prescriptions?.let {
                            if (it.prescriptions.isNotEmpty()) {
                                item { SectionHeader("Prescriptions") }
                                items(it.prescriptions) { prescription -> PrescriptionCard(prescription) }
                            }
                        }
                        state.medicalHistory?.let {
                            if (it.medical_history.isNotEmpty()) {
                                item { SectionHeader("Medical History") }
                                items(it.medical_history) { historyItem -> MedicalHistoryCard(historyItem) }
                            }
                        }
                    }
                }
                is PatientState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                else -> {
                    // Show a loading indicator while the state is loading or idle
                    CircularProgressIndicator()
                }
            }
        }
    }
}

// ... (The rest of the file with Card composables remains the same)

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )
}

@Composable
fun PatientInfoCard(state: PatientState.Success) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "${state.patient.nom} ${state.patient.prenom}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Divider(modifier = Modifier.padding(vertical = 4.dp))
            InfoRow("ID Code:", state.patient.identification_code)
            InfoRow("NNI:", state.patient.nni)
            InfoRow("Age:", state.patient.age)
            InfoRow("Gender:", state.patient.sexe)
            InfoRow("Blood Type:", state.patient.blood_type)
            InfoRow("Date of Birth:", state.patient.dateNaissance)
        }
    }
}

@Composable
fun TreatmentCard(treatment: Treatment) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(treatment.treatment_type, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("Ref: ${treatment.reference}", style = MaterialTheme.typography.bodySmall)
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            InfoRow("Date:", treatment.treatment_date)
            InfoRow("Doctor:", treatment.doctor_name)
            InfoRow("Institution:", treatment.institution)
            if (treatment.acts.isNotEmpty()) {
                Text("Acts:", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp))
                treatment.acts.forEach { act ->
                    Text("- ${act.act_name} (${act.price})")
                }
            }
        }
    }
}

@Composable
fun PrescriptionCard(prescription: Prescription) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(prescription.reference, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            InfoRow("Date:", prescription.date)
            InfoRow("Doctor:", prescription.doctor_name)
            InfoRow("Status:", prescription.state)
            if (prescription.medicines.isNotEmpty()) {
                Text("Medicines (${prescription.medicines.size}):", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp))
                prescription.medicines.forEach { medicine ->
                    MedicineInfo(medicine)
                }
            }
        }
    }
}

@Composable
fun MedicineInfo(medicine: Medicine) {
    Text("- ${medicine.medicine_name}: ${medicine.dose}mg, ${medicine.frequency} times per ${medicine.frequency_unit} for ${medicine.duration} ${medicine.duration_period}")
}

@Composable
fun MedicalHistoryCard(history: MedicalHistoryItem) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(history.type, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("Ref: ${history.reference}", style = MaterialTheme.typography.bodySmall)
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            InfoRow("Date:", history.date)
            InfoRow("Status:", history.status)
            if (history.diagnosis.isNotBlank()) {
                InfoRow("Diagnosis:", history.diagnosis)
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row {
        Text(label, fontWeight = FontWeight.SemiBold, modifier = Modifier.width(120.dp))
        Text(value)
    }
}