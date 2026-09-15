# Castix

> **Background playback, your way.**

Castix is an open-source Android utility for managing background playback and related playback restrictions through six supported backend paths: Android Accessibility, Shizuku, Dhizuku, Root, LSPosed, and LSPatch.

## Project status

| Item | Value |
|---|---|
| Current version | 1.0 |
| versionCode | 1 |
| Application ID | `me.elhizazi.Castix` |
| Minimum Android | Android 7.0 / API 24 |
| Target SDK | 36 |
| Compile SDK | 36.1 |
| Kotlin | 2.2.10 |
| Java | 11 |
| Android Gradle Plugin | 9.1.1 |
| Gradle | 9.3.1 |
| License | GPL-3.0-or-later |
| Author | Jamal El Hizazi |

> [!NOTE]
> Backend availability and behavior can vary with Android version, device manufacturer, ROM, battery policies, target application behavior, installed services, and granted permissions.

## Quick navigation

### Users
- [Getting Started](Getting-Started)
- [Installation](Installation)
- [First Setup](First-Setup)
- [Backends](Backends)
- [Features](Features)
- [Troubleshooting](Troubleshooting)
- [FAQ](FAQ)
- [Permissions and Privacy](Permissions-and-Privacy)

### Backend guides
- [Accessibility](Accessibility)
- [Shizuku](Shizuku)
- [Dhizuku](Dhizuku)
- [Root](Root)
- [LSPosed](LSPosed)
- [LSPatch](LSPatch)

### Contributors
- [Development](Development)
- [Contributing](Contributing)

## Feature overview

- Six stable backend paths.
- Backend status and permission checks.
- Target-app selection for supported workflows.
- Background playback management and related playback restrictions.
- AMOLED / black-screen mode.
- Configurable clock styles, scale, vertical position, and spacing.
- Quick Settings support for black-screen mode.
- Floating Button workflows for supported applications.
- Lock-screen-related playback compatibility where supported.
- Multilingual interface: Arabic, English, Chinese, Japanese, French, Spanish, Portuguese, German, Italian, Russian, and Turkish.

## Official links

- Repository: https://github.com/elhizazi1/Castix
- Website: https://castix.elhizazi.me
- Releases: https://github.com/elhizazi1/Castix/releases
- Issues: https://github.com/elhizazi1/Castix/issues
- License: GPL-3.0-or-later

## Related projects

- Shizuku: https://github.com/RikkaApps/Shizuku
- Dhizuku: https://github.com/iamr0s/Dhizuku
- LSPosed: https://github.com/LSPosed/LSPosed
- LSPatch: https://github.com/LSPosed/LSPatch
- ShizuCoreFetch: https://github.com/elhizazi1/ShizuCoreFetch
- Documentation: https://docshizu.siwane.xyz/

## Distribution notes

Castix has an F-Droid packaging request; it must not be described as already available on F-Droid. It is also intended for the developer's ShizuCoreFetch store, where `requires_shizuku` is `false`.

## Important Shizuku clarification

Repository verification shows that the current build declares Shizuku API/provider dependencies. The documentation therefore distinguishes those integration libraries from a separately installed Shizuku environment. Shizuku remains optional for Castix users and is not required for basic installation or operation.

---

## Wiki navigation

[Home](Home) · [Getting Started](Getting-Started) · [Installation](Installation) · [First Setup](First-Setup) · [Backends](Backends) · [Features](Features) · [Troubleshooting](Troubleshooting) · [FAQ](FAQ) · [Development](Development) · [Contributing](Contributing)
