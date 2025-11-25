package com.debarunlahiri.stt.data.repository

import com.debarunlahiri.stt.data.model.HealthCheckResponse
import com.debarunlahiri.stt.data.model.LanguageDetectionRequest
import com.debarunlahiri.stt.data.model.LanguageDetectionResponse
import com.debarunlahiri.stt.data.model.TranscriptionResponse
import com.debarunlahiri.stt.data.model.TranslationRequest
import com.debarunlahiri.stt.data.model.TranslationResponse
import com.debarunlahiri.stt.data.remote.SttApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import retrofit2.Response
import javax.inject.Inject

class SttRepositoryImpl @Inject constructor(
    private val apiService: SttApiService
) : SttRepository {
    
    override suspend fun healthCheck(): Result<HealthCheckResponse> = withContext(Dispatchers.IO) {
        safeApiCall { apiService.healthCheck() }
    }
    
    override suspend fun transcribeAudio(
        audioPart: MultipartBody.Part,
        language: String,
        enableWordTimestamps: Boolean
    ): Result<TranscriptionResponse> = withContext(Dispatchers.IO) {
        safeApiCall {
            apiService.transcribeAudio(
                audioPart = audioPart,
                language = language,
                enableWordTimestamps = enableWordTimestamps
            )
        }
    }
    
    override suspend fun translateText(
        text: String,
        sourceLanguage: String,
        targetLanguage: String
    ): Result<TranslationResponse> = withContext(Dispatchers.IO) {
        val request = TranslationRequest(
            text = text,
            sourceLanguage = sourceLanguage,
            targetLanguage = targetLanguage
        )
        safeApiCall { apiService.translateText(request) }
    }
    
    override suspend fun detectLanguage(text: String): Result<LanguageDetectionResponse> {
        return withContext(Dispatchers.IO) {
            safeApiCall {
                apiService.detectLanguage(LanguageDetectionRequest(text))
            }
        }
    }
    
    override suspend fun sendToMessenger(message: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.sendToMessenger(mapOf("message" to message))
                if (response.isSuccessful) {
                    Result.Success("Message sent successfully")
                } else {
                    Result.Error("Failed to send message: ${response.code()}")
                }
            } catch (e: Exception) {
                Result.Error(
                    message = "Failed to send message: ${e.message}",
                    exception = e
                )
            }
        }
    }
    
    private suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): Result<T> {
        return try {
            val response = apiCall()
            if (response.isSuccessful && response.body() != null) {
                Result.Success(response.body()!!)
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Unknown error occurred"
                Result.Error("Error ${response.code()}: $errorMessage")
            }
        } catch (e: Exception) {
            Result.Error(
                message = e.message ?: "Network error occurred",
                exception = e
            )
        }
    }
}
