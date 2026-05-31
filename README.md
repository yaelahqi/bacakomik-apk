# Pina Komik (Android Native)

Native Android app built with Kotlin + Jetpack Compose. Inspired by Komiku+ UI, powered by our own backend at `https://komik.pina.my.id`.

## Build

GitHub Actions auto-builds debug APK on every push to `main`.
Download from the **Actions** tab → latest run → **Artifacts → PinaKomik-apk**.

## Local build

```bash
./gradlew :app:assembleDebug
# APK -> app/build/outputs/apk/debug/app-debug.apk
```

Requirements: JDK 17, Android SDK 34, ANDROID_HOME set.

## API endpoints (consumed)

- `GET /api/v1/list?page=1&type=all&order=update`
- `GET /api/v1/search?q=...`
- `GET /api/v1/manga/{slug}`
- `GET /api/v1/chapter/{slug...}`
- `GET /api/img?u=...` (CDN proxy)

## Tech

- Kotlin 2.0 + Jetpack Compose Material3
- Ktor + OkHttp for networking
- Coil for images
- DataStore for library/history
- Navigation Compose
