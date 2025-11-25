package com.debarunlahiri.stt.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class HealthCheckResponse(
    @Json(name = "status")
    val status: String,
    @Json(name = "model_loaded")
    val modelLoaded: Boolean,
    @Json(name = "device")
    val device: String,
    @Json(name = "supported_languages")
    val supportedLanguages: List<String>,
    @Json(name = "supported_audio_formats")
    val supportedAudioFormats: List<String>,
    @Json(name = "model_size")
    val modelSize: String,
    @Json(name = "gpu_available")
    val gpuAvailable: Boolean,
    @Json(name = "gpu_name")
    val gpuName: String?
)
