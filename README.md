# Fusion Launcher — Android source repository

A native Android HOME launcher with six selectable interface inspirations:
Samsung One UI, Windows Desktop, macOS, iOS, Fusion Hybrid, and Nokia Lumia / Windows Phone.

## Build with GitHub (no local Android Studio required)

1. Create a **new empty GitHub repository**.
2. Upload **the contents of this ZIP** to the repository root (not the outer folder).
3. Commit to `main`.
4. Open **Actions → Build Android APK → Run workflow**, or let the push trigger it.
5. After the workflow succeeds, open the workflow run → **Artifacts → FusionLauncher-debug-apk**.
6. Download the artifact ZIP, extract `app-debug.apk`, and install on an Android phone.
7. Set Fusion Launcher as the default Home app in Android Settings → Apps → Default apps → Home app.

Alternatively open this repository in Android Studio (JDK 17, Android SDK 35) and build `:app:assembleDebug`.
The GitHub workflow installs Gradle 8.9; a Gradle wrapper is not required.

## Included functionality

- Android HOME intent handling and user-initiated default-launcher selection.
- Installed-app list with icons, search and launch.
- Six mode selectors with different headers and layout treatments.
- Nokia Lumia tile-style grid, WP 8.1 / Windows 10 Mobile option, accent and transparency settings.
- Persistent grid size, dark theme, card shape, labels, clock, compact mode, dock and accent.
- Long-press apps to pin, hide or open Android app info.
- Hidden-app management.
- Settings export via Android share sheet and import from JSON.
- Android wallpaper picker shortcut.
- GitHub Actions debug APK workflow.

## Limitations and roadmap (not implemented)

This is a substantial starter implementation, **not a production-complete replica of six operating systems**.
The six modes share a native UI foundation and do not yet reproduce every interaction or visual element of their inspirations.
Missing: genuine app-provided Live Tile data, AppWidgetHost widgets, drag-and-drop home-screen pages, icon-pack import,
full custom gesture mapping, notification integration, system-level quick-settings replacement, real floating windows,
custom icon drawing, advanced animations, wallpaper rendering inside the launcher, and automatic scheduled profiles.
Android prevents third-party HOME launchers from replacing privileged OS components without special access.
The Windows/macOS-style interfaces cannot make arbitrary apps run in desktop windows on all phones.

## Testing status

The source archive and file structure have been checked, but **no Android SDK/Gradle compilation or on-device testing** was available in the authoring environment. If the GitHub workflow reports a compile error, share its build log for correction.

## Privacy

No analytics, advertising SDK, network permission or cloud backend. Launcher preferences are stored locally.
The launcher uses Android's package visibility query for launchable apps.

## License / branding

This project uses original launcher code and system app icons. It is not affiliated with Samsung, Apple, Microsoft or Nokia.
Product names identify UI inspirations, not official endorsements. Avoid redistributing proprietary system assets.
