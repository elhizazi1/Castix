# Contributing

Contributions are welcome when they improve Castix without introducing undocumented behavior or unnecessary architectural changes.

## Bug reports

Use https://github.com/elhizazi1/Castix/issues and include device model, Android version, Castix version, backend, target app, reproduction steps, expected result, actual result, and sanitized logs/screenshots.

## Feature requests

Describe the user problem, desired behavior, why existing functionality is insufficient, and relevant Android/device constraints. Do not base requests on invented APIs or undocumented internals.

## Pull requests

1. Explain the problem.
2. Explain the solution.
3. Keep the change focused.
4. Include relevant testing.
5. Update documentation when behavior changes.
6. Never include secrets.

## Documentation

Document behavior that exists in the current project, distinguish device-dependent behavior, and avoid inventing UI labels, APIs, permissions, or architecture.

## Backend stability

The project currently has six stable backend paths: Accessibility, Shizuku, Dhizuku, Root, LSPosed, and LSPatch. Do not redesign, merge, rename, remove, or invent backend paths in unrelated contributions.

## Testing expectations

When backend behavior changes, test the affected backend and verify that other paths remain unaffected where practical. Record the Android/device environment used.

## Security

Never submit passwords, keystores, private keys, GitHub tokens, API tokens, or credentials.

## License

Castix is licensed under GNU GPL-3.0-or-later. Contributions should be compatible with the project's licensing requirements.

---

## Wiki navigation

[Home](Home) · [Getting Started](Getting-Started) · [Installation](Installation) · [First Setup](First-Setup) · [Backends](Backends) · [Features](Features) · [Troubleshooting](Troubleshooting) · [FAQ](FAQ) · [Development](Development) · [Contributing](Contributing)
