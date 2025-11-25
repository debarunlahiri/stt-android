package com.debarunlahiri.stt.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TranscriptionResponse(
    @Json(name = "text")
    val text: String,
    @Json(name = "language")
    val language: String,
    @Json(name = "detected_language")
    val detectedLanguage: String,
    @Json(name = "segments")
    val segments: List<Segment>,
    @Json(name = "processing_time_sec")
    val processingTimeSec: Double,
    @Json(name = "real_time_factor")
    val realTimeFactor: Double,
    @Json(name = "audio_duration_sec")
    val audioDurationSec: Double,
    @Json(name = "audio_file_url")
    val audioFileUrl: String? = null,
    @Json(name = "confidence")
    val confidence: Double?,
    @Json(name = "word_count")
    val wordCount: Int
)

@JsonClass(generateAdapter = true)
data class Segment(
    @Json(name = "start")
    val start: Double,
    @Json(name = "end")
    val end: Double,
    @Json(name = "text")
    val text: String,
    @Json(name = "words")
    val words: List<Word>?,
    @Json(name = "speaker")
    val speaker: String?,
    @Json(name = "language")
    val language: String?
)

@JsonClass(generateAdapter = true)
data class Word(
    @Json(name = "word")
    val word: String,
    @Json(name = "start")
    val start: Double,
    @Json(name = "end")
    val end: Double,
    @Json(name = "confidence")
    val confidence: Double?
)
