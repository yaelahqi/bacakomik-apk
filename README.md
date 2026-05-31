# BacaKomik APK

Capacitor-based Android wrapper for [komik.pina.my.id](https://komik.pina.my.id/app).

## Auto build

Every push to `main` triggers GitHub Actions to build a fresh debug APK.

Download the latest APK from the [Actions tab](../../actions) → click latest workflow run → download `BacaKomik-apk` artifact.

For tagged releases (`git tag v1.0.0 && git push --tags`), the APK is auto-attached to a GitHub Release.

## Local development

```bash
npm install
npx cap sync android
cd android
./gradlew assembleDebug
```

Output APK: `android/app/build/outputs/apk/debug/app-debug.apk`

## Stack

- Capacitor 8
- Android (Java)
- Loads `https://komik.pina.my.id/app` directly via WebView
- Hardware back button → `WebView.goBack()`
