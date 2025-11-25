package com.debarunlahiri.stt.data.repository

import com.debarunlahiri.stt.data.model.HealthCheckResponse
import com.debarunlahiri.stt.data.model.LanguageDetectionRequest
import com.debarunlahiri.stt.data.model.LanguageDetectionResponse
import com.debarunlahiri.stt.data.model.TranscriptionResponse
import com.debarunlahiri.stt.data.model.TranslationRequest
import com.debarunlahiri.stt.data.model.TranslationResponse
import okhttp3.MultipartBody

interface SttRepository {
    suspend fun healthCheck(): Result<HealthCheckResponse>
    
    suspend fun transcribeAudio(
        audioPart: MultipartBody.Part,
        language: String,
        enableWordTimestamps: Boolean
    ): Result<TranscriptionResponse>
    
    suspend fun translateText(
        text: String,
        sourceLanguage: String,
        targetLanguage: String
    ): Result<TranslationResponse>
    
    suspend fun detectLanguage(text: String): Result<LanguageDetectionResponse>
    
    suspend fun sendToMessenger(message: String): Result<String>
}
