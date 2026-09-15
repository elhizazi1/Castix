# 👨‍💻 Development

## Technology baseline

- Kotlin 2.2.10
- Java 11
- Android Gradle Plugin 9.1.1
- Gradle 9.3.1
- Compile SDK 36.1
- Target SDK 36
- Minimum SDK 24
- Jetpack Compose build features
- AndroidX, Room, Coroutines, Retrofit/OkHttp/Moshi
- Shizuku API/provider integration

## Repository structure

```text
Castix/
├── app/
├── fastlane/
├── gradle/
├── .github/
├── build.gradle.kts
├── settings.gradle.kts
├── gradlew
├── gradlew.bat
├── LICENSE
├── README.md
├── THIRD_PARTY_NOTICES.md
└── metadata.json
```

This is a high-level map; implementation details can change.

## Android Studio

Open the official repository in Android Studio, allow Gradle synchronization, select the desired variant, and build/test.

## Commands

```bash
./gradlew assembleDebug
./gradlew assembleRelease
```

## Signing security

Never commit or document passwords, keystore contents, private keys, Base64 signing material, GitHub Actions secrets, or access tokens. Keep release signing material outside source control.

## Compatibility stubs

Castix contains minimal Xposed-compatible API stubs for LSPosed/LSPatch interoperability. They are not an embedded runtime.

## Backend architecture

The six stable backend paths are Accessibility, Shizuku, Dhizuku, Root, LSPosed, and LSPatch. Contributions should not redesign, merge, rename, remove, or invent backend paths as part of unrelated work.

## Testing

Build the affected variant, test the affected workflow, verify backend status when relevant, and document device-specific limitations instead of claiming universal behavior.

---

## Wiki navigation

[Home](Home) · [Getting Started](Getting-Started) · [Installation](Installation) · [First Setup](First-Setup) · [Backends](Backends) · [Features](Features) · [Troubleshooting](Troubleshooting) · [FAQ](FAQ) · [Development](Development) · [Contributing](Contributing)
