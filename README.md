# Cosmos — NASA Media Explorer

A Kotlin Multiplatform app targeting Android (Jetpack Compose) and iOS (SwiftUI). Pulls from three NASA public APIs: Astronomy Picture of the Day, the EPIC Earth camera, and a 30-day APOD archive. All business logic lives in shared Kotlin — the platform layers are thin UI only.

---

## Screens

| Screen | Description |
|---|---|
| **Today** | Today's APOD — image with pinch-to-zoom or ExoPlayer/AVPlayer for video days |
| **Archive** | 30-day grid of past APODs, tap any cell to open Media Detail |
| **Earth** | NASA EPIC camera — browse Earth photos by date, 2-column grid |
| **Media Detail** | Full-screen zoomable image or video player, favorite toggle |
| **Favorites** | Saved APODs, persisted locally via SQLDelight |

---

## Architecture

All business logic is in `commonMain`. Data flows strictly one direction:

```
NASA API / SQLite
      ↓
  Repository        ← cache-first: emit cached value, then fetch fresh
      ↓
  UseCase           ← one class per operation, wraps a repository call
      ↓
  ViewModel         ← emits StateFlow<UiState<T>>
      ↓
  UI                ← Android: Compose   iOS: SwiftUI via ViewModelHelper
```

`UiState<T>` is a sealed class with three states: `Loading`, `Success(data: T)`, and `Error(message: String)`. Every screen handles all three exhaustively.

iOS can't consume generic Kotlin `StateFlow<UiState<T>>` directly due to Swift generics limitations. Each ViewModel gets a corresponding `*ViewModelHelper` in `iosMain` that collects the flow and calls typed Swift callbacks (`onLoading`, `onSuccess`, `onError`).

---

## Source layout

```
composeApp/src/
│
├── commonMain/                         # Shared across Android + iOS
│   └── kotlin/.../cosmos/
│       ├── domain/
│       │   ├── UiState.kt              # sealed class Loading / Success / Error
│       │   ├── model/                  # Apod, EpicImage, MediaType (Image | Video)
│       │   ├── repository/             # Interfaces: ApodRepository, EpicRepository, FavoriteRepository
│       │   └── usecase/                # GetTodayApod, GetApodArchive, GetApodByDate,
│       │                               # GetEpicImages, GetEpicAvailableDates,
│       │                               # GetFavorites, IsFavorite, ToggleFavorite
│       │
│       ├── data/
│       │   ├── remote/
│       │   │   ├── ApodApiService.kt   # Ktor client for APOD endpoints
│       │   │   ├── EpicApiService.kt   # Ktor client for EPIC endpoints
│       │   │   ├── dto/                # Wire types: ApodDto, EpicImageDto
│       │   │   └── mapper/             # DTO → domain model
│       │   ├── local/
│       │   │   ├── CosmosDatabase.sq   # SQLDelight schema (apod_cache, epic_*, favorites)
│       │   │   ├── ApodCache.kt
│       │   │   ├── EpicCache.kt
│       │   │   ├── FavoriteCache.kt
│       │   │   └── DatabaseDriverFactory.kt  # expect/actual — Android + iOS drivers
│       │   └── repository/             # ApodRepositoryImpl, EpicRepositoryImpl, FavoriteRepositoryImpl
│       │
│       └── presentation/
│           ├── today/                  # TodayViewModel
│           ├── archive/                # ArchiveViewModel, ArchiveUiState
│           ├── epic/                   # EpicViewModel, EpicUiState
│           ├── detail/                 # MediaDetailViewModel, MediaDetailUiState
│           └── favorites/              # FavoritesViewModel
│
├── androidMain/                        # Android-only
│   └── kotlin/.../cosmos/
│       ├── MainActivity.kt
│       ├── di/AppModule.kt             # Koin module — all singletons + factories
│       └── ui/
│           ├── CosmosApp.kt            # NavHost + bottom bar (Today/Archive/Earth/Favorites)
│           ├── today/                  # TodayScreen, TodayScreenSkeleton (shimmer)
│           ├── archive/                # ArchiveScreen
│           ├── epic/                   # EpicScreen
│           ├── detail/                 # MediaDetailScreen (ZoomableImage, ExoPlayer)
│           ├── favorites/              # FavoritesScreen
│           ├── component/              # Shimmer modifier
│           └── theme/                  # CosmosColor, CosmosType, CosmosSpacing, CosmosTheme
│
├── iosMain/                            # iOS-only Kotlin
│   └── kotlin/.../cosmos/
│       ├── data/
│       │   ├── remote/NasaApiClient.kt         # actual — Darwin HTTP engine
│       │   └── local/DatabaseDriverFactory.kt  # actual — NativeSqliteDriver
│       ├── di/IosModule.kt                      # startKoin() called from Swift
│       └── presentation/
│           ├── today/TodayViewModelHelper.kt
│           ├── archive/ArchiveViewModelHelper.kt
│           ├── epic/EpicViewModelHelper.kt
│           ├── detail/MediaDetailViewModelHelper.kt
│           └── favorites/FavoritesViewModelHelper.kt
│
└── commonTest/ + androidUnitTest/      # Repository, use case, and ViewModel tests

iosApp/iosApp/                          # SwiftUI app
├── CosmosApp.swift                     # Entry point — calls startKoin()
├── ContentView.swift                   # TabView root
├── TodayView.swift
├── ArchiveView.swift
├── EarthView.swift
├── MediaDetailView.swift               # Heart button, zoomable AsyncImage, AVPlayer
├── FavoritesView.swift
└── VideoPlayerView.swift               # AVPlayer wrapper
```

---

## Key patterns

**Cache-then-network** — repositories emit the cached value immediately if present, then fetch fresh data from the network. An error is only emitted if the cache is empty and the network call fails.

**`expect`/`actual`** — used for `DatabaseDriverFactory` (Android: `AndroidSqliteDriver`, iOS: `NativeSqliteDriver`) and the Ktor HTTP engine (Android: OkHttp, iOS: Darwin). These are the only intentional platform seams.

**Koin DI** — `appModule` (Android) and `startKoin()` (iOS) register the same graph: singletons for core infrastructure (HTTP client, DB, caches, services, repositories), factories for use cases and ViewModels.

**`MediaType` sealed class** — `Apod` carries either `MediaType.Image(url)` or `MediaType.Video(url)`. NASA APOD returns YouTube embed URLs on video days. Platform layers decide how to render: ExoPlayer on Android, AVPlayer on iOS.

---

## Stack

| Concern | Library |
|---|---|
| Networking | Ktor (`ktor-client-core`, `-okhttp`, `-darwin`) |
| Caching | SQLDelight 2 |
| Images | Coil |
| Video (Android) | Media3 / ExoPlayer |
| DI | Koin 4 |
| Navigation (Android) | `androidx.navigation.compose` |
| Shared ViewModels | `androidx.lifecycle:lifecycle-viewmodel` KMP artifact |
| Testing | `kotlin.test` + Turbine |

---

## Running locally

Requires a [NASA API key](https://api.nasa.gov). Add it to `local.properties` (not committed):

```
nasa.api.key=YOUR_KEY_HERE
```

```bash
# Android
./gradlew :composeApp:installDebug

# Tests
./gradlew :composeApp:test

# iOS — open iosApp/iosApp.xcodeproj in Xcode and run
```
