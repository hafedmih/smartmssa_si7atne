package com.smartmssa.si7atne.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartmssa.si7atne.SessionManager
import com.smartmssa.si7atne.data.*
import com.smartmssa.si7atne.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val apiService = RetrofitInstance.api

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    private val _patientState = MutableStateFlow<PatientState>(PatientState.Idle)
    val patientState: StateFlow<PatientState> = _patientState

    // The login function remains the same. The token is saved in the SessionManager from the Activity.
    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading
            try {
                val response = apiService.login(LoginRequest(username, password))
                if (response.isSuccessful && response.body() != null) {
                    _loginState.value = LoginState.Success(response.body()!!.token)
                } else {
                    _loginState.value = LoginState.Error("Login failed")
                }
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "An error occurred")
            }
        }
    }

    // --- KEY CHANGE ---
    // This function no longer accepts a token. It retrieves it directly from the SessionManager.
    fun getPatientDetails(code: String) {
        viewModelScope.launch {
            // 1. Get the token from the central SessionManager.
            val token = SessionManager.authToken

            // 2. CRITICAL: Check if the token exists. If not, the user is not logged in.
            if (token == null) {
                _patientState.value = PatientState.Error("Session expired. Please log in again.")
                return@launch // Stop the function execution.
            }

            // 3. If the token exists, proceed with the API call.
            _patientState.value = PatientState.Loading
            try {
                val patientResponse = apiService.getPatient(code, "Bearer $token")
                if (patientResponse.isSuccessful) {
                    val patient = patientResponse.body()!!
                    val treatmentIds = patient.treatments.joinToString(",") { it.id }
                    val prescriptionIds = patient.prescriptions.joinToString(",") { it.id }
                    val medicalHistoryIds = patient.medical_history.joinToString(",") { it.id }

                    val treatments = apiService.getTreatments(treatmentIds, "Bearer $token").body()
                    val prescriptions = apiService.getPrescriptions(prescriptionIds, "Bearer $token").body()
                    val medicalHistory = apiService.getMedicalHistory(medicalHistoryIds, "Bearer $token").body()

                    _patientState.value = PatientState.Success(patient, treatments, prescriptions, medicalHistory)
                } else {
                    _patientState.value = PatientState.Error("Patient with code '$code' not found.")
                }
            } catch (e: Exception) {
                _patientState.value = PatientState.Error(e.message ?: "An unknown error occurred")
            }
        }
    }

    fun resetPatientState() {
        _patientState.value = PatientState.Idle
    }
}


// UI States remain unchanged
sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val token: String) : LoginState()
    data class Error(val message: String) : LoginState()
}

sealed class PatientState {
    object Idle : PatientState()
    object Loading : PatientState()
    data class Success(
        val patient: Patient,
        val treatments: TreatmentsResponse?,
        val prescriptions: PrescriptionsResponse?,
        val medicalHistory: MedicalHistoryResponse?
    ) : PatientState()
    data class Error(val message: String) : PatientState()
}