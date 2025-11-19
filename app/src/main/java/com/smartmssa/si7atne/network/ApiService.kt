package com.smartmssa.si7atne.network

import com.smartmssa.si7atne.data.LoginRequest
import com.smartmssa.si7atne.data.LoginResponse
import com.smartmssa.si7atne.data.MedicalHistoryResponse
import com.smartmssa.si7atne.data.Patient
import com.smartmssa.si7atne.data.PrescriptionsResponse
import com.smartmssa.si7atne.data.TreatmentsResponse
// ... import other models
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("api/v1/si7atne/authentication/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @GET("api/v1/si7atne/medical/patients/{code}")
    suspend fun getPatient(
        @Path("code") code: String,
        @Header("Authorization") token: String
    ): Response<Patient>

    @GET("api/v1/si7atne/medical/treatments")
    suspend fun getTreatments(
        @Query("ids") ids: String,
        @Header("Authorization") token: String
    ): Response<TreatmentsResponse>

    @GET("api/v1/si7atne/medical/prescriptions")
    suspend fun getPrescriptions(
        @Query("ids") ids: String,
        @Header("Authorization") token: String
    ): Response<PrescriptionsResponse>

    @GET("api/v1/si7atne/medical/medical-history")
    suspend fun getMedicalHistory(
        @Query("ids") ids: String,
        @Header("Authorization") token: String
    ): Response<MedicalHistoryResponse>
}
// Define request and response models for all endpoints