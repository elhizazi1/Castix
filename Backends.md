# 🛠️ Backends

| Backend | Separate environment? | Root required? | Shizuku required? | Typical use |
|---|---|---|---|---|
| Accessibility | No | No | No | Supported Accessibility workflows |
| Shizuku | Yes | Not necessarily | Yes | Workflows using Shizuku authorization |
| Dhizuku | Yes | Depends on environment | No | Device Owner-related workflows |
| Root | Yes | Yes | No | Workflows requiring root authorization |
| LSPosed | Yes | Environment-dependent | No | Xposed/LSPosed integration |
| LSPatch | Yes | No root necessarily | No | LSPatch-based integration |

## Selection

Use the least complex backend that meets your workflow. Accessibility is not dependent on root or Shizuku. The other environments are optional.

## Status checks

Castix provides backend status and permission checks from the main interface. Use them before troubleshooting.

> [!WARNING]
> Privileged environments can provide powerful capabilities. Enable only what you understand and trust.

See [Accessibility](Accessibility), [Shizuku](Shizuku), [Dhizuku](Dhizuku), [Root](Root), [LSPosed](LSPosed), and [LSPatch](LSPatch).

---

## Wiki navigation

[Home](Home) · [Getting Started](Getting-Started) · [Installation](Installation) · [First Setup](First-Setup) · [Backends](Backends) · [Features](Features) · [Troubleshooting](Troubleshooting) · [FAQ](FAQ) · [Development](Development) · [Contributing](Contributing)
