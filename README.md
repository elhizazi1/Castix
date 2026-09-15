# Castix

Castix is an Android utility for managing background playback and related playback restrictions through several privilege/accessibility backends.

## Features

- Multiple backend options: **Shizuku, Dhizuku, Root, LSPosed, LSPatch, and Android Accessibility**.
- Backend status and permission checks from the main interface.
- Target-app selection for supported playback workflows.
- AMOLED / black-screen mode with configurable clock styles.
- Customizable clock scale, vertical position, and spacing.
- Quick Settings support for the black-screen mode.
- Multilingual interface, including Arabic, English, Chinese, Japanese, French, Spanish, Portuguese, German, Italian, Russian, and Turkish.
- No Firebase or Google Play Services dependency in the current build.

## Requirements

- Android 7.0 (API 24) or newer.
- Some features require an appropriate backend to be installed and configured on the device.
- Root, Shizuku, Dhizuku, LSPosed, LSPatch, and Accessibility features are optional; Castix does not require all of them at the same time.

## Building

### Android Studio

1. Clone or download this repository.
2. Open the project in Android Studio.
3. Allow Gradle to synchronize the project.
4. Build the desired variant from the Gradle/Build menu.

### Command line

Debug build:

```bash
./gradlew assembleDebug
```

Release build:

```bash
./gradlew assembleRelease
```

The release configuration used for distribution may require the signing environment variables configured by the project maintainer. F-Droid builds should use their own signing process.

## License

Castix is licensed under the **GNU General Public License v3.0 or later**.

Copyright (C) 2026 Jamal El Hizazi.

See the [`LICENSE`](LICENSE) file for the complete license text.

## Third-party components

Castix uses open-source Android libraries and the Shizuku API/provider. Their respective licenses remain applicable to those components. See the project dependency declarations and the upstream projects for their individual license terms.

## Disclaimer

Castix is provided without warranty. Availability and behavior of privilege backends depend on the Android version, device configuration, installed services, and the permissions granted by the user.

## Third-party compatibility APIs

Castix includes minimal Xposed-compatible API stubs for interoperability with LSPosed and LSPatch. These stubs are not an embedded Xposed/LSPosed runtime. See `THIRD_PARTY_NOTICES.md` for attribution and licensing context.
