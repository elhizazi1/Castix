# 🧰 Troubleshooting

## Playback stops

Check backend status, authorization, target-app selection, battery/background restrictions, manufacturer power management, and target-app behavior.

## Accessibility not working

Confirm Castix's Accessibility service is enabled, re-check backend status, and inspect battery/background restrictions.

## Shizuku not detected

Confirm Shizuku is running, Castix is authorized, background restrictions are not stopping it, and reopen Castix after restarting Shizuku.

## Dhizuku not detected

Confirm Dhizuku is active, required Device Owner/device-management state is correct, and Castix is authorized.

## Root not detected

Confirm the device is rooted, Castix is authorized by the root manager, and root works independently.

## LSPosed issues

Confirm LSPosed is active, Castix integration is enabled, target scope is correct, and versions are compatible.

## LSPatch issues

Confirm LSPatch configuration, Castix integration, target-app build, and Android compatibility.

## Battery optimization

Aggressive battery management can stop services or background processes. Check Android's battery/background controls for Castix and the target app where relevant.

## Manufacturer restrictions

Manufacturer ROMs can impose additional background, overlay, or permission restrictions. Exact menus differ by device.

## Target-app problems

If only one target app fails, its playback lifecycle or version may be responsible. Compare with another supported application where practical.

## Before opening an issue

Include:
- device model
- Android version
- Castix version
- selected backend
- target app and version when relevant
- reproduction steps
- expected result
- actual result
- logs/screenshots after removing sensitive information

Do not include passwords, tokens, signing secrets, or private account data.

## Issue template recommendation

```text
## Environment
- Device:
- Android version:
- Castix version:
- Backend:
- Target app:

## Steps to reproduce
1.
2.
3.

## Expected result

## Actual result

## Additional information
- Battery/background settings:
- Sanitized logs/screenshots:
```

Report bugs: https://github.com/elhizazi1/Castix/issues

---

## Wiki navigation

[Home](Home) · [Getting Started](Getting-Started) · [Installation](Installation) · [First Setup](First-Setup) · [Backends](Backends) · [Features](Features) · [Troubleshooting](Troubleshooting) · [FAQ](FAQ) · [Development](Development) · [Contributing](Contributing)
