# Simple Notes

<img alt="Logo" src="graphics/icon.png" width="120" />

<div style="display:flex; gap:8px; flex-wrap:wrap;">
<img alt="Notes" src="graphics/screenshots/screenshot-notes.png" width="22%">
<img alt="Notebooks" src="graphics/screenshots/screenshot-notebooks.png" width="22%">
<img alt="Inside notebook" src="graphics/screenshots/screenshot-inside-notebook.png" width="22%">
<img alt="Customize Widgets HomeScreen" src="graphics/screenshots/screenshot-home-widgets.png" width="22%">
</div>

## History

Simple-Notes (GPL-3.0) was my favorite FOSS notes app until the project was sold to a shady Israeli company named ZipoApps in 2023. I forked it to keep it alive, FOSS and updated. I will release new versions as long as I have time and energy. Code contributions on Github are very welcome :)

This is an independent fork of Simple Notes Pro 6.17.4. This fork is not affiliated with Simple Mobile Tools, Fossify or ZipoApps.

APKs are published on [GitHub Releases](https://github.com/t0ma5/Simple-Notes/releases) as `Simple-Notes_<version>-FOSS-arm64-v8a.apk` (most phones), `...-armeabi-v7a.apk`, `...-x86_64.apk`, and `...-FOSS-universal.apk` (all ABIs). Push a tag `vX.Y.Z` to attach signed APKs to a release. Every push, pull request, and manual **Build APK** run also uploads the APKs as a workflow artifact.

## New features ahead of upstream 6.17.0

- Notebooks: create, rename, delete, pin, reorder, and password-protect collections of notes. Toolbar icon to the right of search switches notebooks vs notes (notes is the default after install). Notebook cards show a centered icon; tapping the handle opens the same menu as a long-press.
- Search notes and notebooks from the notebooks screen.
- Pin individual notes.
- Recycle bin for deleted notes and notebooks.
- Markdown preview for text notes.
- Tags on notes, searchable from the notebooks screen.
- Home-screen note and notebook widgets, including a transparent background.
- Undo and redo for text, checklist, and counter edits.
- Read-only notes: lock a text note, checklist, or counter against accidental edits.
- Uncheck all checklist items, collapse checked items, and sort each checklist independently. The add-item dialog stays open if you tap outside it.
- Delete the last remaining note.
- Counter notes with colored increment/decrement buttons.
- Move checklist items or whole notes between notebooks.
- Optional encrypted exports.
- No donation, rate, or “what's new” popups.
- applicationId and Kotlin packages are `tomato.simple.notes`, so it can sit next to Simple Notes Pro.

Contains no ads or unnecessary permissions. It is fully open source and provides customizable colors. The lack of internet access and encrypted exports gives you more privacy than typical note apps.

## Build

Release APKs come from `assembleCoreRelease` (local and GitHub Actions). Names:

- `Simple-Notes_<version>-FOSS-arm64-v8a.apk`
- `Simple-Notes_<version>-FOSS-armeabi-v7a.apk`
- `Simple-Notes_<version>-FOSS-x86_64.apk`
- `Simple-Notes_<version>-FOSS-universal.apk`

| Item | Value |
| --- | --- |
| App version | 6.18.0 (versionCode 124) |
| minSdk | 23 |
| targetSdk / compileSdk | 36 (Android 16) |
| JVM bytecode | 17 |
| Gradle | 9.1.0 |
| Android Gradle Plugin | 8.13.2 |
| Kotlin / KSP | 2.2.10 |
| Room | 2.8.4 |
| Local JDK | 25 (do not point Gradle at JDK 17) |
| CI JDK | Temurin 17 |
| Commons | `SimpleMobileTools/Simple-Commons` @ `eceb48949e`, patched by `python scripts/patch_commons.py` |

Checkout Simple-Commons next to the app (gitignored `Simple-Commons/`), run `python scripts/patch_commons.py`, then `./gradlew assembleCoreRelease`. When that directory exists, Gradle `includeBuild`s it instead of the JitPack AAR. Sign with gitignored `keystore.properties` and `app/keystore.jks` (GitHub secrets `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`).
