# First Setup

There is no universal setup because Castix supports six backend paths.

## Common sequence

1. Install and open Castix.
2. Review backend status.
3. Select a backend.
4. Complete its authorization.
5. Select target apps when supported.
6. Test playback.
7. Configure optional black-screen mode.

## Accessibility

Open Castix, choose Accessibility, follow the Android settings flow, enable the Castix Accessibility service, return to Castix, confirm status, and test.

## Shizuku

Prepare a compatible Shizuku environment, start it, select Shizuku in Castix, authorize Castix when requested, confirm status, and test.

## Dhizuku

Prepare a compatible Dhizuku environment and any required Device Owner/device-management state, select Dhizuku, authorize Castix, confirm status, and test.

## Root

Use only on a device already rooted with a compatible root environment. Select Root, approve Castix in the root manager, confirm status, and test.

## LSPosed

Prepare a compatible LSPosed environment, configure the Castix integration/module and relevant scope as required by the current release, then test. Castix does not bundle the LSPosed runtime.

## LSPatch

Prepare a compatible LSPatch environment, configure the Castix integration according to the current LSPatch workflow, and test the resulting target application. Castix does not bundle LSPatch.

## Black-screen mode

After playback works, configure clock style, scale, vertical position, spacing, and Quick Settings support where available.

> [!NOTE]
> Exact external-backend screens and prompts are controlled by those environments and may change independently of Castix.

---

## Wiki navigation

[Home](Home) · [Getting Started](Getting-Started) · [Installation](Installation) · [First Setup](First-Setup) · [Backends](Backends) · [Features](Features) · [Troubleshooting](Troubleshooting) · [FAQ](FAQ) · [Development](Development) · [Contributing](Contributing)
