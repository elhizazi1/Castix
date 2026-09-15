# Installation

## Requirements

- Android 7.0 / API 24 or newer.
- A compatible Android device.
- An additional backend environment only when the selected workflow requires it.

## GitHub Releases

Official releases: https://github.com/elhizazi1/Castix/releases

Prefer official release artifacts over unofficial mirrors.

## APK installation

1. Download a trusted Castix APK.
2. Confirm it is the Castix package `me.elhizazi.Castix`.
3. Android may require permission to install from the selected source.
4. Install and launch Castix.
5. Continue with [First Setup](First-Setup).

## Release vs debug

Release builds are intended for distribution. Debug builds are for development/testing and are not the same artifact.

Debug command:
```bash
./gradlew assembleDebug
```

Release command:
```bash
./gradlew assembleRelease
```

## Updating

Review the release, install the newer official build, then re-check backend authorization and test the target workflow.

## Basic troubleshooting

If installation fails, check Android version, APK source/integrity, installation-source restrictions, and compatibility with an existing installation.

---

## Wiki navigation

[Home](Home) · [Getting Started](Getting-Started) · [Installation](Installation) · [First Setup](First-Setup) · [Backends](Backends) · [Features](Features) · [Troubleshooting](Troubleshooting) · [FAQ](FAQ) · [Development](Development) · [Contributing](Contributing)
