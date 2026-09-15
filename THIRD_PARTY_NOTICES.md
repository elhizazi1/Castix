# Third-party notices

## Xposed-compatible API stubs

Castix contains two small API-compatibility stubs under `de.robv.android.xposed` and `de.robv.android.xposed.callbacks` so the Castix Xposed hook can compile against the legacy Xposed-compatible interface used by LSPosed and LSPatch.

These files are **not** a bundled Xposed or LSPosed runtime. Castix does not package LSPosed, LSPatch, or another Xposed runtime inside the APK.

The API names and compatibility target follow the legacy Xposed API used by the LSPosed project. LSPosed is licensed under the GNU General Public License v3.0. See the upstream LSPosed project and its LICENSE file for the complete upstream licensing terms.
