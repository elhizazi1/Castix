# Castix engine reliability patch

This is a conservative source patch for GitHub Actions builds. The existing Gradle project and `.github/workflows` are preserved.

## Changed
- Foreground detection: prefer active/focused accessibility window over stale click/focus events, clear cached package when service disconnects.
- Engine selector: distinguish the selected engine from any actually usable engine; do not display 'no engine active' when another is usable.
- LSPosed and LSPatch: remove fabricated success from unsupported command and playback operations.
- Dhizuku: remove misleading `sh -c` execution under ordinary app UID; do not claim Device Owner dispatch without a DPM integration.
- Accessibility: only claim supported foreground detection; do not claim power-management, app-ops or audio policy changes.
- LSPosed toggle: do not persist UI state as a verified injected hook.
- Android version: report the device release and API level instead of a hardcoded Android 17 mapping.

## Limitations
- This patch does **not** implement APK patching, Xposed hooks, or Dhizuku DPM binder operations. Unsupported operations now fail explicitly instead of silently claiming success.
- Android runtime behavior and compilation were not verified here: the environment cannot fetch the Gradle distribution. Run the existing GitHub Actions `Build Castix Android` workflow and review its output.
- The root cause of third-party patched-app crashes remains uninvestigated pending logs.
