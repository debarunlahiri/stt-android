package com.debarunlahiri.stt.data.remote

import com.debarunlahiri.stt.data.model.HealthCheckResponse
import com.debarunlahiri.stt.data.model.LanguageDetectionRequest
import com.debarunlahiri.stt.data.model.LanguageDetectionResponse
import com.debarunlahiri.stt.data.model.TranscriptionResponse
import com.debarunlahiri.stt.data.model.TranslationRequest
import com.debarunlahiri.stt.data.model.TranslationResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface SttApiService {
    
    @GET("health")
    suspend fun healthCheck(): Response<HealthCheckResponse>
    
    @Multipart
    @POST("v1/transcribe")
    suspend fun transcribeAudio(
        @Part audioPart: MultipartBody.Part,
        @Query("language") language: String = "auto",
        @Query("enable_word_timestamps") enableWordTimestamps: Boolean = true,
        @Query("enable_diarization") enableDiarization: Boolean = false
    ): Response<TranscriptionResponse>
    
    @POST("v1/translate")
    suspend fun translateText(
        @Body request: TranslationRequest
    ): Response<TranslationResponse>
    
    @POST("v1/detect-language")
    suspend fun detectLanguage(
        @Body request: LanguageDetectionRequest
    ): Response<LanguageDetectionResponse>
    
    @POST("v1/messenger/send")
    suspend fun sendToMessenger(
        @Body request: Map<String, String>
    ): Response<Map<String, Any>>
}
