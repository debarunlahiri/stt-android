# STT - Speech-to-Text Android Application

An Android application built with Jetpack Compose that provides speech-to-text transcription, language detection, and translation capabilities through a REST API backend.

## Features

- **Audio Transcription**: Record audio or upload audio files and convert speech to text
- **Language Detection**: Automatically detect the language of input text
- **Translation**: Translate text between multiple languages (English, Hindi, Korean)
- **Real-time Audio Recording**: Record audio directly within the app with waveform visualization
- **Audio Playback**: Play recorded audio files before transcription
- **Health Check**: Monitor API server connectivity
- **Messenger Integration**: Send transcribed text to messenger services

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **Dependency Injection**: Hilt
- **Networking**: Retrofit 2.9.0, OkHttp 4.12.0
- **JSON Parsing**: Moshi 1.15.0
- **Image Loading**: Coil 2.5.0
- **Coroutines**: Kotlin Coroutines 1.7.3
- **Navigation**: Navigation Compose 2.7.5
- **Network Debugging**: Chucker 4.0.0

## Project Structure

```
app/src/main/java/com/debarunlahiri/stt/
├── data/
│   ├── model/          # Data models for API responses
│   ├── remote/         # API service interface
│   └── repository/     # Repository pattern implementation
├── di/                 # Dependency injection modules
├── ui/
│   ├── components/     # Reusable UI components
│   ├── navigation/     # Navigation graph and screens
│   ├── screen/         # Screen composables
│   ├── theme/          # Material 3 theme configuration
│   └── viewmodel/      # ViewModels for state management
├── util/               # Utility classes and constants
├── MainActivity.kt
└── SttApplication.kt
```

## Requirements

- **Minimum SDK**: Android 7.0 (API 24)
- **Target SDK**: Android 36
- **Java Version**: 11
- **Kotlin**: 2.0.21
- **Gradle**: 8.13.1

## Setup Instructions

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd STT
   ```

2. **Configure API Base URL**
   
   Update the `BASE_URL` constant in `app/src/main/java/com/debarunlahiri/stt/util/Constants.kt`:
   ```kotlin
   const val BASE_URL = "http://YOUR_SERVER_IP:8000/"
   ```
   
   Note:
   - Use `10.0.2.2` for Android Emulator (localhost)
   - Use your actual server IP address for physical devices
   - Ensure your device/emulator is on the same network as the server

3. **Build and Run**
   - Open the project in Android Studio
   - Sync Gradle files
   - Build and run on an emulator or physical device

## Permissions

The app requires the following permissions:
- `INTERNET`: For API communication
- `RECORD_AUDIO`: For audio recording functionality
- `READ_EXTERNAL_STORAGE`: For reading audio files (Android 12 and below)
- `READ_MEDIA_AUDIO`: For reading audio files (Android 13+)

## API Endpoints

The app communicates with a backend API at the configured base URL. Supported endpoints:

- `GET /health` - Health check endpoint
- `POST /v1/transcribe` - Transcribe audio to text
  - Query parameters: `language`, `enable_word_timestamps`, `enable_diarization`
- `POST /v1/translate` - Translate text between languages
- `POST /v1/detect-language` - Detect the language of input text
- `POST /v1/messenger/send` - Send text to messenger service

## Supported Languages

- English (en)
- Hindi (hi)
- Korean (ko)
- Auto Detect

## File Limitations

- Maximum file size: 500 MB
- Maximum audio duration: 60 seconds
- Maximum recording duration: 30 seconds

## Architecture

The app follows the **MVVM (Model-View-ViewModel)** architecture pattern:

- **Model**: Data models and repository classes
- **View**: Jetpack Compose UI screens and components
- **ViewModel**: ViewModels that manage UI state and business logic

### Key Components

- **Repository Pattern**: Abstracts data sources and provides a single source of truth
- **Dependency Injection**: Hilt for managing dependencies
- **State Management**: ViewModels with Kotlin StateFlow/State
- **Navigation**: Navigation Compose for screen navigation

## Development

### Building from Source

1. Ensure you have Android Studio installed with the latest Android SDK
2. Open the project in Android Studio
3. Sync Gradle files and wait for dependencies to download
4. Connect an Android device or start an emulator
5. Click Run or press `Shift+F10`

### Testing

The project includes test directories:
- `app/src/test/` - Unit tests
- `app/src/androidTest/` - Instrumented tests

## Dependencies

All dependencies are managed through Gradle Version Catalog (`gradle/libs.versions.toml`). Key dependencies include:

- AndroidX Core, Lifecycle, Activity Compose
- Jetpack Compose BOM (2024.09.00)
- Hilt for dependency injection
- Retrofit and OkHttp for networking
- Moshi for JSON parsing
- Navigation Compose
- Coil for image loading
- Chucker for network debugging

## Notes

- The app uses Material 3 design system
- Network security configuration is set up in `app/src/main/res/xml/network_security_config.xml`
- ProGuard rules are defined in `app/proguard-rules.pro`
- KSP (Kotlin Symbol Processing) is used for code generation

## License

[Add your license information here]

## Author

Debarun Lahiri

