# Simple Notes

<img alt="Logo" src="graphics/icon.png" width="120" />

<div style="display:flex; gap:8px; flex-wrap:wrap;">
<img alt="Notes" src="graphics/screenshots/screenshot-notes.png" width="22%">
<img alt="Notebooks" src="graphics/screenshots/screenshot-notebooks.png" width="22%">
<img alt="Inside notebook" src="graphics/screenshots/screenshot-inside-notebook.png" width="22%">
<img alt="Customize Widgets HomeScreen" src="graphics/screenshots/screenshot-home-widgets.png" width="22%">
</div>

No ads or unnecessary permissions. It is fully open source and provides customizable colors. The lack of internet access and encrypted exports gives you more privacy than typical note apps.

## History

Simple-Notes (GPL-3.0) was my favorite FOSS notes app until the project was sold to a shady Israeli company named ZipoApps in 2023. I forked it to keep it alive, FOSS and updated. I will release new versions as long as I have time and energy. Code contributions on Github are very welcome :)

This fork is not affiliated with Simple Mobile Tools, Fossify or ZipoApps.

APKs are published on [GitHub Releases](https://github.com/t0ma5/Simple-Notes/releases)
- `Simple-Notes_<version>-FOSS-arm64-v8a.apk` (most phones)
- `Simple-Notes_<version>-FOSS-armeabi-v7a.apk`
- `Simple-Notes_<version>-FOSS-x86_64.apk` (emulators)
- `Simple-Notes_<version>-FOSS-universal.apk` (all included)

## New features in this fork

- **Notebooks** → Create, rename, delete, pin, reorder and lock collections of notes. Count is left, `:::` is right, on the bottom row of each card. Tapping `:::` opens the same menu as a long-press.
- **Notebooks/Notes View** - Toolbar icon to the right of search switches notebooks vs a two-column notes card grid (notes is the default after install).
- **Read-only notes** → Overflow Make read-only / Allow editing. Blocks typing, checklist checks, and counter buttons. Survives app restart.
- **Lock Notes** → Lock a text note, checklist or counter note with Fingerprint, Pattern or Pin #. Hides preview until unlock; lock icon sits in the lower-right of the card.
- **Encrypted exports** → Use a password to encrypt exports using AES. Can also export in plain text with no password.
- **Search** notes and notebooks from the notebooks screen.
- **Drag & Drop** notebooks with `:::`. Long-press a note card to reorder. Note cards show 4 preview lines; each source line stays on one row and ellipsizes instead of wrapping.
- **Recycle bin** for deleted notes and notebooks.
- **Markdown** preview for text notes.
- Pasting formatted paste becomes **plain text**. Composing keyboards still work.
- **Tags** on notes, searchable from the notebooks screen.
- **Undo & redo** for text, checklist, and counter edits.
- Uncheck all checklist items, collapse checked items, and sort each checklist independently. The add-item dialog stays open if you tap outside it.
- **Counter Notes** with colored increment/decrement buttons.
- **Move** notes, checklist items or counters between notebooks.
- **Widgets Settings** → Customize widget colors on updates every Notes widget, not only the first one.
- **Delete** the last remaining note.
- No donation, rate, or “what's new” popups.
- Android 16 → compileSdk and **targetSdk 36**.
- **applicationId** and Kotlin packages are `tomato.simple.notes`, so app can install even with Simple Notes Pro present for testing/comparing.

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
