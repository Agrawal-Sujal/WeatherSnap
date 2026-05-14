# WeatherSnap

A polished Android app to search live weather, capture photo evidence, compress images, and save weather reports locally.

## Tech Stack

- **Kotlin** + **Jetpack Compose** (Material 3)
- **MVVM** with `ViewModel` + `StateFlow` + `Coroutines`
- **Hilt** for dependency injection
- **Navigation Compose** with animated transitions
- **Retrofit** + **Gson** + **OkHttp** logging interceptor
- **Room Database** for local persistence
- **CameraX** for custom camera implementation
- **Coil** for image loading

## Setup & Run

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- Android SDK 35
- Minimum SDK: 24 (Android 7.0)

### Steps
1. Clone or unzip the repository
2. Open in Android Studio
3. Let Gradle sync complete
4. Run on a physical device (camera requires real hardware) or emulator with camera support
5. Grant camera permission when prompted

### API
Uses [Open-Meteo](https://open-meteo.com/) — no API key required.
- Geocoding: `https://geocoding-api.open-meteo.com/v1/search`
- Weather: `https://api.open-meteo.com/v1/forecast`

## App Flow

1. **Weather Screen** — Type 3+ letters to get city autocomplete suggestions (debounced, cached). Select a city, tap Search to load weather. Tap "Create Report" when weather is shown.
2. **Create Report Screen** — Shows the locked weather snapshot. Tap "Capture Photo" to open the custom camera.
3. **Custom Camera Screen** — Live CameraX preview. Tap Capture to take photo, Close to cancel.
4. **Back on Report Screen** — Photo preview appears with animated fade-in. Original and compressed image sizes are shown. Add optional field notes. Tap "Save Report".
5. **Saved Reports Screen** — All reports displayed as cards with image, weather details, timestamps, file sizes, and notes.

## Developer Judgment Challenge — Lifecycle/Data-Loss Protection

### Problem
The create-report flow spans multiple screens and can be interrupted by device rotation, backgrounding, or process death. A naive implementation would lose: the weather snapshot, the captured photo path, and the typed notes.

### Solution Approach

**Two-layer protection:**

**Layer 1 — `SharedWeatherViewModel` (activity-scoped):**
The weather data selected on the Weather Screen is immediately stored in a Hilt-provided `SharedWeatherViewModel` that lives at the Activity scope. This VM survives configuration changes (rotation). The `ReportViewModel` calls `initWeatherData()` which is guarded by a null-check — it only accepts the weather once, so re-composition or re-navigation never silently replaces the snapshot with a freshly-fetched value.

**Layer 2 — `SavedStateHandle` in `ReportViewModel`:**
The captured image file path, original/compressed sizes, and notes are persisted in `SavedStateHandle` as soon as they're set. `SavedStateHandle` is backed by the system-managed state bundle that survives process death (as long as the task remains in the recents). On the next cold start (same task), the ViewModel re-reads these values and restores the draft — no duplicate reports are created because the report is only written to Room on explicit Save.

**Temp file cleanup:**
- Raw captures go to `cache/raw_captures/` (cleared by the system under storage pressure).
- Compressed files go to `files/images/`. When "Retake Photo" is used, the previous compressed file is deleted before the new one is written.
- On successful save, the compressed file remains because the Room DB references it. If the user abandons the flow (navigates back), the draft path in `SavedStateHandle` naturally expires with the task.

### Tradeoffs
- `SharedWeatherViewModel` is activity-scoped via Hilt's default scope in `WeatherSnapNavGraph`, meaning it holds data as long as the Activity is alive. This is acceptable for a single-user flow.
- `SavedStateHandle` protects against process death but not app uninstall. Files in `filesDir` survive app restarts but not uninstalls.
- Keeping compressed files in `filesDir` (not cache) means the OS won't delete them unexpectedly, but they require explicit cleanup on report discard. A future improvement could add a periodic cleanup job for orphaned draft images.
- No duplicate saves: `savedSuccessfully` in `ReportUiState` triggers navigation away and `clearDraft()` removes the `SavedStateHandle` keys, preventing double-saves on recomposition.
