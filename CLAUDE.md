# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

aMetro is an Android app (Java) for offline transit map navigation. It bundles 236+ PMZ-format transit maps, renders them on-device, and supports route planning. No network access, no ads, no tracking.

- **Min SDK:** 24 (Android 7.0) | **Target/Compile SDK:** 36 (Android 15)
- **Java version:** 11
- **Package ID:** `io.github.romangolovanov.apps.ametro`

## Build Commands

```bash
# Debug APK
./gradlew assembleDebug

# Release APK
./gradlew assembleRelease

# Unit tests (minimal coverage currently)
./gradlew test

# Instrumentation tests (requires connected device/emulator)
./gradlew connectedAndroidTest
```

Output APKs land in `app/build/outputs/apk/`.

Dependencies are managed via the version catalog at `gradle/libs.versions.toml`.

## Architecture

### Application Singleton (`app/ApplicationEx.java`)
Central state holder. Lazily initializes `IconProvider`, `MapCatalogProvider`, and `MapInfoLocalizationProvider`. Also holds the currently open map (`MapContainer`), active scheme name, enabled transports, and selected stations for route planning. Everything that needs global state goes through here.

### Map Format (PMZ)
Maps are ZIP archives stored in `app/src/main/assets/map_files/`. The `model/serialization/` package parses PMZ contents into the domain model. Key classes: `MapScheme`, `MapSchemeLine`, `MapSchemeStation`, `MapSchemeSegment`, `MapTransportScheme`. Jackson is used for JSON metadata parsing.

### Catalog (`catalog/`)
`MapCatalogProvider` loads and indexes all 236+ bundled maps. `MapInfoLocalizationProvider` provides map names in 24 languages. `MapCatalogSerializer` handles JSON deserialization of map metadata.

### Rendering Engine (`render/`)
`CanvasRenderer` drives the full rendering pipeline. Drawing is split into element classes: `SegmentElement`, `StationElement`, `TransferElement`. `ClippingTree` provides spatial partitioning for performance. `RenderProgram` and `RenderConstants` hold rendering configuration.

### Route Planning (`routes/`)
`MapRouteProvider` calculates routes. `DijkstraHeap` implements the shortest-path algorithm. Routes are represented as `MapRoute` / `MapRoutePart`.

### UI Layer (`ui/`)
- **Activities:** `Map` (main map view), `MapList`, `StationDetails`, `SettingsList`, `About`
- **Fragments:** map list, settings, station map sub-views
- **Gesture handling:** `MultiTouchController` (pan/zoom)
- **Custom drawer:** checkbox, header, and splitter item types
- View Binding is enabled project-wide.

### Key Dependencies
- **Jackson 2.20.0** — JSON parsing for map metadata and catalog
- **AndroidSVG 1.4** — SVG rendering for country flag and transport icons
- **AndroidX** — AppCompat, Material, ConstraintLayout, Navigation, Lifecycle
