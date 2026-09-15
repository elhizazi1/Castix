# 🔐 Permissions and Privacy

> [!NOTE]
> This page is a technical project overview, not legal advice.

## Open source

Castix is open source and licensed under GPL-3.0-or-later. Source: https://github.com/elhizazi1/Castix

## Accessibility

The Accessibility backend uses Android Accessibility capabilities for supported workflows. Treat this as a sensitive capability.

## Shizuku

Shizuku is optional. Its authorization model applies when the Shizuku backend is used.

## Dhizuku / Device Owner

Dhizuku is optional. Its environment may involve Device Owner/device-management capabilities with administrative implications.

## Root

Root is optional and highly privileged. Castix does not provide a root solution.

## LSPosed and LSPatch

Both are optional external environments. Castix does not package their runtimes.

## Compatibility stubs

The repository contains minimal Xposed-compatible API stubs for LSPosed/LSPatch interoperability. They are not an embedded runtime. `THIRD_PARTY_NOTICES.md` contains compatibility information.

## Firebase and Google Play Services

The current build does not include Firebase or Google Play Services dependencies.

## Online account

Core functionality does not require an online account.

## Network wording

The repository includes networking libraries, so this Wiki intentionally does not make an absolute claim that Castix can never perform network operations. Verify current implementation-specific behavior against the source.

## Bug-report privacy

Remove account identifiers, tokens, private content, signing information, and unrelated personal data from logs/screenshots before sharing them.

---

## Wiki navigation

[Home](Home) · [Getting Started](Getting-Started) · [Installation](Installation) · [First Setup](First-Setup) · [Backends](Backends) · [Features](Features) · [Troubleshooting](Troubleshooting) · [FAQ](FAQ) · [Development](Development) · [Contributing](Contributing)
