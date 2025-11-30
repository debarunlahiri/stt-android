package com.debarunlahiri.stt.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TranslationRequest(
    @Json(name = "text")
    val text: String,
    @Json(name = "source_language")
    val sourceLanguage: String = "auto",
    @Json(name = "target_language")
    val targetLanguage: String = "en"
)

@JsonClass(generateAdapter = true)
data class TranslationResponse(
    @Json(name = "english_text")
    val englishText: String,
    @Json(name = "hindi_text")
    val hindiText: String,
    @Json(name = "korean_text")
    val koreanText: String,
    @Json(name = "source_language")
    val sourceLanguage: String,
    @Json(name = "detected_language")
    val detectedLanguage: String,
    @Json(name = "detection_confidence")
    val detectionConfidence: Double,
    @Json(name = "processing_time_sec")
    val processingTimeSec: Double,
    @Json(name = "translation_applied")
    val translationApplied: Boolean
)
