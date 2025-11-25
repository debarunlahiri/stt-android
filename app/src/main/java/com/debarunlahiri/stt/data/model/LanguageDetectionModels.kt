package com.debarunlahiri.stt.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LanguageDetectionRequest(
    @Json(name = "text")
    val text: String
)

@JsonClass(generateAdapter = true)
data class LanguageDetectionResponse(
    @Json(name = "detected_language")
    val detectedLanguage: String,
    @Json(name = "language_name")
    val languageName: String,
    @Json(name = "confidence")
    val confidence: Double,
    @Json(name = "all_detections")
    val allDetections: List<LanguageDetection>?
)

@JsonClass(generateAdapter = true)
data class LanguageDetection(
    @Json(name = "language")
    val language: String,
    @Json(name = "confidence")
    val confidence: Double
)
