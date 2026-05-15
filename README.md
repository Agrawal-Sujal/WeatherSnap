# WeatherSnap

A polished Android weather app that lets you search live weather for any city, capture a photo with a custom camera, compress it, add field notes, and save weather reports locally — all without an API key.

---

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Setup & Run](#setup--run)
- [App Flow](#app-flow)
- [Developer Judgment Challenge](#developer-judgment-challenge)
- [Testing](#testing)
- [Architecture Notes](#architecture-notes)
- [Known Tradeoffs](#known-tradeoffs)

---

## Features

- **City autocomplete** — suggestions appear after 2 characters with 300ms debounce; results are cached in Room for 24 hours to avoid redundant API calls
- **Live weather** — fetches from Open-Meteo (no API key required); displays temperature, condition, humidity, wind speed, and pressure
- **Custom CameraX screen** — full live preview built with CameraX; no device camera intent used
- **Image compression** — captured images are scaled to a max of 1280px and re-encoded at 30% JPEG quality; original and compressed sizes are both displayed
- **Room persistence** — reports are saved to a local Room database on a background coroutine; the saved reports list is driven by a `Flow` that updates reactively
- **Offline fallback** — when the device has no connectivity, the app shows an `Offline` state and prompts the user to view their saved reports instead of showing a generic error
- **Debug-only network logging** — `HttpLoggingInterceptor` is added to OkHttp only when `BuildConfig.DEBUG` is true
- **Draft recovery** — in-progress report state survives rotation, backgrounding, and process death (see [Developer Judgment Challenge](#developer-judgment-challenge))

---

## Tech Stack

| Layer | Library |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM (ViewModel + StateFlow) |
| DI | Hilt |
| Navigation | Navigation Compose |
| Networking | Retrofit + Gson + OkHttp |
| Local DB | Room |
| Camera | CameraX |
| Async | Coroutines |

**API:** [Open-Meteo](https://open-meteo.com/) — no API key required.

---

## Project Structure

```
app/src/main/java/com/trackzio/weathersnap/
│
├── data/
│   ├── local/
│   │   └── Database.kt              # Room DB, DAOs, TypeConverters, entities
│   ├── remote/
│   │   ├── api/
│   │   │   └── ApiInterfaces.kt     # Retrofit interfaces for geocoding + weather
│   │   └── model/
│   │       └── ApiModels.kt         # API response data classes
│   └── WeatherRepository.kt         # Single source of truth; city cache logic + offline handling
│
├── di/
│   └── AppModule.kt                 # Hilt module — OkHttp, Retrofit (two instances), Room, DAOs
│
├── domain/
│   └── model/
│       └── Models.kt                # Clean domain models (CityResult, WeatherData)
│
├── ui/
│   ├── navigation/
│   │   └── NavGraph.kt              # Type-safe nav graph with animated transitions
│   ├── screens/
│   │   ├── weather/
│   │   │   ├── WeatherScreen.kt
│   │   │   ├── WeatherViewModel.kt
│   │   │   └── SharedWeatherViewModel.kt
│   │   ├── report/
│   │   │   ├── CreateReportScreen.kt
│   │   │   └── ReportViewModel.kt
│   │   ├── camera/
│   │   │   └── CameraScreen.kt
│   │   └── saved/
│   │       ├── SavedReportsScreen.kt
│   │       └── SavedReportsViewModel.kt
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── Theme.kt                 # Dark olive/forest palette + shimmer modifier
│   │   └── Type.kt
│   └── util/
│       └── ClickUtils.kt            # Debounced click helpers
│
├── util/
│   └── ImageCompression.kt          # Bitmap scale + JPEG re-encode
│
├── MainActivity.kt
└── WeatherSnapApplication.kt
```

---

## Setup & Run

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or newer
- Android SDK 34
- A physical device or emulator running API 24+
- CameraX requires a physical device or an emulator with camera support enabled

### Steps

1. Clone the repository:
   ```bash
   git clone https://github.com/<your-username>/weathersnap.git
   cd weathersnap
   ```

2. Open the project in Android Studio.

3. Let Gradle sync complete (all dependencies are pulled from Maven Central and Google — no local setup needed).

4. Run the app on a physical device or a camera-capable emulator:
   ```
   Run → Run 'app'
   ```
   or via CLI:
   ```bash
   ./gradlew installDebug
   ```

No API keys, no `.env` files, no extra configuration required.

---

## App Flow

### 1. Weather Screen
- Type a city name; suggestions appear after **more than 2 characters**.
- Suggestions are debounced (300ms) and cached in Room for 24 hours — the same query will never hit the network twice within that window.
- Select a suggestion, then tap **Search** to fetch current weather (temperature, condition, humidity, wind speed, pressure).
- States shown: loading (shimmer), success, empty, error, and offline.
- On success, a **Create Report** button appears. A **Reports** button is always available in the top bar.

### 2. Create Report Screen
- Shows the selected weather snapshot (frozen at selection time — never re-fetched).
- Tap **Capture Photo** to open the custom camera.
- After capture, a compressed image preview is shown with original and compressed sizes.
- Enter optional field notes.
- Tap **Save Report** to persist to Room and navigate to the Saved Reports screen.

### 3. Custom Camera Screen
- Built with **CameraX** — no device camera intent is used.
- Shows a full live preview, a **Capture** button, and a **Close** button.
- On capture, the raw image file path is returned to the report screen via `NavBackStackEntry.savedStateHandle`.
- The report screen's `ReportViewModel` then compresses the image and cleans up the raw file.

### 4. Saved Reports Screen
- Displays all reports from Room, ordered by timestamp descending.
- Each card shows: captured image, city + weather details (as they were at report creation), original image size, compressed image size, notes, and saved timestamp.
- Shows an animated empty state when no reports exist.

---

## Developer Judgment Challenge

### Problem
If the user selects a city, opens Create Report, captures a photo, enters notes, and then rotates the device or the process is killed before saving — the in-progress report would normally be lost, or a duplicate could be created on re-entry.

### Approach: `SavedStateHandle` as the draft store

`ReportViewModel` uses `SavedStateHandle` to persist the draft state across all lifecycle events, including process death:

```kotlin
// On init — state is hydrated from SavedStateHandle automatically
private val _uiState = MutableStateFlow(ReportUiState(
    capturedImagePath = savedStateHandle["draft_image_path"],
    originalSizeKb    = savedStateHandle["draft_original_kb"] ?: 0L,
    compressedSizeKb  = savedStateHandle["draft_compressed_kb"] ?: 0L,
    notes             = savedStateHandle["draft_notes"] ?: ""
))

// On every change — state is written back immediately
savedStateHandle["draft_image_path"] = result.compressedFile.absolutePath
savedStateHandle["draft_notes"]      = notes
```

When the draft is saved to Room, `clearDraft()` removes all keys so the same draft is never saved twice.

**Frozen weather snapshot** — `SharedWeatherViewModel` is scoped to the Activity (not the nav back stack entry). The weather is set once when the user enters Create Report and is never re-fetched. `ReportViewModel.initWeatherData()` further guards against overwriting:

```kotlin
fun initWeatherData(data: WeatherData) {
    if (_weatherData.value == null) {   // only set once
        _weatherData.value = data
    }
}
```

**Temp file cleanup** — when a new photo is captured, the previous draft compressed file is deleted before the new one is saved. When the report is successfully persisted, the draft keys are cleared. The compressed file saved to the report is kept (it is the source of truth displayed in Saved Reports); the raw CameraX output file is deleted after compression.

### Tradeoffs

| Decision | Rationale | Tradeoff |
|---|---|---|
| `SavedStateHandle` for draft | Survives process death; no additional Room table or shared prefs needed | Primitives only — `WeatherData` is passed through `SharedWeatherViewModel` rather than serialised into `SavedStateHandle` |
| Activity-scoped `SharedWeatherViewModel` | Survives navigation to camera and back without re-fetching | ViewModel lives as long as the Activity; cleared manually on save |
| Delete old draft image on new capture | No indefinitely leaking temp files | If the delete fails (e.g. file already gone), the error is silently swallowed — acceptable since the new file is already in place |
| `fallbackToDestructiveMigration()` in Room | Simplifies schema changes during development | Not suitable for production where data must be migrated gracefully |

---

## Testing

### Unit Tests (`test/`)

| Test class | What it covers |
|---|---|
| `WeatherRepositoryTest` | Cache hit returns data without hitting the network; cache miss fetches from API and persists result |
| `WeatherViewModelTest` | Debounced city search emits Loading → Success; `fetchWeather` emits Loading → Success |
| `ReportViewModelTest` | Save flow emits Loading → Success and clears draft; `onNotesChange` updates both state and `SavedStateHandle` |
| `SavedReportsViewModelTest` | Loading trigger drives `flatMapLatest`; reports list is exposed correctly |

Tests use **MockK** for mocking and **Turbine** for testing `StateFlow` / `Flow` emissions.

Run unit tests:
```bash
./gradlew test
```

### Compose UI Tests (`androidTest/`)

| Test | What it covers |
|---|---|
| `weatherScreen_displaysEmptyState_initially` | Idle state shows placeholder text |
| `weatherScreen_displaysWeatherData_whenSuccess` | Success state shows city, condition, temperature, and Create Report button |
| `savedReportsScreen_displaysEmptyState_whenNoReports` | Empty list shows the empty state message |
| `createReportScreen_displaysWeatherAndPhotoPreview` | Create Report screen shows weather details and photo preview area |
| `weatherScreen_searchTriggersCallback` | Search button click triggers the `onSearch` callback |

Run instrumented tests (requires a connected device or emulator):
```bash
./gradlew connectedAndroidTest
```

---

## Architecture Notes

- **Two Retrofit instances** — geocoding and weather APIs have different base URLs; each gets its own `@Named` singleton in `AppModule`.
- **Debug-only logging** — `HttpLoggingInterceptor` is added conditionally on `BuildConfig.DEBUG`, keeping release builds clean.
- **IO-thread safety** — all Room operations are `suspend` functions called from `viewModelScope`; Hilt's default dispatcher for `@Singleton` DAOs is `Dispatchers.IO`.
- **City cache TTL** — cached suggestions expire after 24 hours; stale entries are served if the TTL has not elapsed, even across app restarts.
- **`NetworkUnavailableException`** — `IOException` from Retrofit is translated into a typed exception in `WeatherRepository`, which `WeatherViewModel` maps to `WeatherUiState.Offline`. This decouples the UI from Retrofit internals.
- **Debounced click utility** — `rememberDebouncedClick` and `rememberDebouncedClickParam` in `ClickUtils.kt` prevent double-submit on buttons throughout the app.

---

## Known Tradeoffs

- `fallbackToDestructiveMigration()` is used in the Room builder. This is intentional for the scope of this assignment; a production app would provide proper `Migration` objects.
- The dark olive theme is hardcoded (`isSystemInDarkTheme()` is respected in the API but the colour scheme is always dark to match the design reference).
- CameraX requires `CAMERA` permission to be granted at runtime. The app requests it on the camera screen; behaviour on permanent denial is a no-op close.