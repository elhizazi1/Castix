# LSPosed Backend

LSPosed is an **optional** external environment.

Official project: https://github.com/LSPosed/LSPosed

Castix does not bundle the LSPosed runtime.

## Compatibility stubs

The Castix repository contains minimal Xposed-compatible API stubs under legacy `de.robv.android.xposed` namespaces for interoperability with LSPosed/LSPatch. They are not an embedded Xposed/LSPosed runtime.

## Setup

1. Prepare LSPosed using its official documentation.
2. Install Castix normally.
3. Configure the Castix integration/module.
4. Apply relevant scope/configuration required by the current release.
5. Reload/restart components when required.
6. Test the target workflow.

## Troubleshooting

Confirm LSPosed is active, the Castix integration is enabled, target scope is correct, and Android/LSPosed/Castix/target-app versions are compatible.

## Security

Module/injection environments can alter application behavior. Use trusted tools and sources.

---

## Wiki navigation

[Home](Home) · [Getting Started](Getting-Started) · [Installation](Installation) · [First Setup](First-Setup) · [Backends](Backends) · [Features](Features) · [Troubleshooting](Troubleshooting) · [FAQ](FAQ) · [Development](Development) · [Contributing](Contributing)
