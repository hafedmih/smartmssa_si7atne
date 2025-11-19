package com.smartmssa.si7atne.data

data class LoginResponse(val token: String)
data class Patient(
    val id: String,
    val nom: String,
    val prenom: String,
    val dateNaissance: String,
    val sexe: String,
    val age: String,
    val blood_type: String,
    val nni: String,
    val identification_code: String,
    val treatments: List<TreatmentId>,
    val prescriptions: List<PrescriptionId>,
    val medical_history: List<MedicalHistoryId>
)
data class TreatmentId(val id: String)
data class PrescriptionId(val id: String)
data class MedicalHistoryId(val id: String)
// Add other data models for Treatment, Prescription, MedicalHistory details
data class LoginRequest(
    val username: String,
    val password: String
)
data class TreatmentsResponse(
    val treatments: List<Treatment>,
    val count: Int
)
data class Treatment(
    val id: String,
    val reference: String,
    val patient_id: String,
    val doctor_id: String,
    val doctor_name: String,
    val treatment_date: String,
    val treatment_type_id: String,
    val treatment_type: String,
    val institution: String,
    val acts: List<Act>
)
data class Act(
    val act_id: String,
    val act_name: String,
    val price: Double,
    val notes: String
)
data class PrescriptionsResponse(
    val prescriptions: List<Prescription>,
    val count: Int
)
data class Prescription(
    val id: String,
    val reference: String,
    val patient_id: String,
    val doctor_id: String,
    val doctor_name: String,
    val date: String,
    val datetime: String,
    val notes: String,
    val state: String,
    val medicines: List<Medicine>
)
data class Medicine(
    val medicine_id: String,
    val medicine_name: String,
    val dose: Int,
    val dose_unit_id: String,
    val duration: Int,
    val duration_period: String,
    val frequency: Int,
    val frequency_unit: String,
    val info: String
)
data class MedicalHistoryResponse(
    val medical_history: List<MedicalHistoryItem>,
    val count: Int
)
data class MedicalHistoryItem(
    val id: String,
    val patient_id: String,
    val date: String,
    val type_id: String,
    val type: String,
    val title: String,
    val description: String,
    val status: String,
    val reference: String,
    val diagnosis: String
)