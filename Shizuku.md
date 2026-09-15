# Shizuku Backend

Shizuku is an **optional** backend environment.

Official project: https://github.com/RikkaApps/Shizuku

## Important clarification

Castix does not require a separately installed Shizuku environment for basic installation or operation. The current Castix build does declare `dev.rikka.shizuku:api` and `dev.rikka.shizuku:provider` dependencies for integration. This is distinct from making Shizuku a mandatory user prerequisite.

## Setup

1. Prepare Shizuku using its official project documentation.
2. Start the Shizuku service.
3. Open Castix and select Shizuku.
4. Authorize Castix when requested.
5. Confirm backend status.
6. Test the target workflow.

## Troubleshooting

Confirm Shizuku is running, authorization is active, Castix has not been restricted in the background, and reopen both applications after restarting the service.

## Security

Shizuku authorization grants privileged capabilities. Only authorize trusted applications.

---

## Wiki navigation

[Home](Home) · [Getting Started](Getting-Started) · [Installation](Installation) · [First Setup](First-Setup) · [Backends](Backends) · [Features](Features) · [Troubleshooting](Troubleshooting) · [FAQ](FAQ) · [Development](Development) · [Contributing](Contributing)
