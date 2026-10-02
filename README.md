# Mindful
### Do you catch yourself picking up your phone without even realizing it?
Mindful is an Android app that asks you to pause before using your phone. Each time you **unlock** your device, a prompt appears so you can briefly note **why** you picked it up—or tap **In a hurry — skip** when you need to move on.

## Download

**Recommended:** install from **[GitHub Releases](https://github.com/amita-shukla/mindful-phone/releases)** (APK attached to each release).

APKs are **not** stored in the git repo (they bloat history and change every build). Releases are the right place to host installable builds.

### Install the APK on your phone

1. Open the latest release on GitHub and download **`mindful-app.apk`**.
2. Open the file on your phone (Files app or browser downloads).
3. Allow **Install unknown apps** for your browser or file manager if Android asks.
4. Open **Mindful** → **Allow display over other apps** → allow **Notifications** if asked → **Turn on unlock prompts**.

**Consumer installs:** Prefer **[Google Play](https://play.google.com/console)** when you publish. Play installs grant overlay permission through normal system settings (not sideload “restricted settings”). GitHub APKs are fine for testers.

### Publish a new release (maintainers)

**Option A — tag push (CI uploads the APK):**

```bash
git tag v1.0.0
git push origin v1.0.0
```

The [Release APK workflow](.github/workflows/release-apk.yml) builds a **release** APK and attaches **`mindful-app.apk`** to the GitHub release. Creating a release only in the GitHub UI (without pushing a `v*` tag or running the workflow) will **not** add an APK—you must use a tag push or upload the file yourself.

**Option B — manual upload:**

```bash
./gradlew assembleRelease
gh release upload v1.0.0 app/build/outputs/apk/release/mindful-app.apk --clobber
# Or create release and upload in one step:
# gh release create v1.0.0 app/build/outputs/apk/release/mindful-app.apk --title "Mindful 1.0 — Pause before you scroll"
```

**Option C — build only (no release):** GitHub → **Actions** → **Build and release APK** → **Run workflow**, then download the artifact from the run.

### Other distribution options -- Work in Progress

| Channel | When to use |
|---------|-------------|
| **[Google Play](https://play.google.com/console)** | Public app, updates, trust |
| **[Firebase App Distribution](https://firebase.google.com/docs/app-distribution)** | Private beta testers |
| **[F-Droid](https://f-droid.org/)** | Free/open source, privacy-focused catalog |

## What it does

- Runs a lightweight background monitor while enabled
- Shows a full-screen prompt on unlock
- Single text field for your intention
- **SOS — skip for now** for urgent moments: calls, payments
- No accounts, no cloud—everything stays on your device

## Requirements

- Android 7.0 (API 24) or higher
- **Notifications** (Android 13+) so unlock prompts can stay active in the background
- **Disable battery optimization** (recommended on many OEMs)

## Getting started (developers)

1. Open the project in **Android Studio**.
2. Connect a device with **USB debugging** enabled, or use an emulator.
3. Run the **app** configuration (`Run ▶`).

Release APK (for sharing):

```text
app/build/outputs/apk/release/mindful-app.apk
```

Debug APK (local development only):

```text
app/build/outputs/apk/debug/mindful-app-debug.apk
```

## Getting started (on your phone)

1. Install and open **Mindful** (from Play, a release APK, or Android Studio).
2. Tap **Allow display over other apps** and enable Mindful in system settings.
3. Optionally tap **Turn off battery optimization** and allow it.
4. Allow **Notifications** if Android asks.
5. Tap **Turn on unlock prompts**. You should see a persistent **“Mindful is on”** notification.
6. Tap **Preview prompt** to confirm the UI appears.
7. Lock the phone, then **unlock fully** (PIN / biometric / pattern). The prompt should appear as a full-screen overlay shortly after unlock.

## How it works

| Component | Role |
|-----------|------|
| `MainActivity` | One-time setup: permissions and starting monitoring |
| `UnlockMonitorService` | Foreground service that keeps unlock detection alive |
| `UnlockDetection` | Listens for unlock via `USER_PRESENT`, `USER_UNLOCKED`, and keyguard polling after `SCREEN_ON` |
| `UnlockPrompt` | Shows full-screen overlay on unlock (`PopupActivity` fallback if overlay unavailable) |
| `BootReceiver` | Restarts monitoring after reboot if you already enabled it |

Unlock detection uses more than one signal because some manufacturers do not deliver `ACTION_USER_PRESENT` reliably to background apps.

## Permissions

| Permission | Why |
|------------|-----|
| `SYSTEM_ALERT_WINDOW` | Display the unlock prompt over the home screen and other apps |
| `POST_NOTIFICATIONS` | Required notification for the foreground service (Android 13+) |
| `FOREGROUND_SERVICE` / `FOREGROUND_SERVICE_SPECIAL_USE` | Legitimate background unlock monitoring (Android 14+) |
| `RECEIVE_BOOT_COMPLETED` | Resume monitoring after device restart |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Reduce the system killing the monitor (optional but recommended) |

## Troubleshooting

**App crashes on “Turn on unlock prompts”**  
Use a recent build. Android 14+ requires the correct foreground service type when starting the service.

**“Preview prompt” does nothing**  
Reinstall the latest build. If it still fails, check that no other app is blocking full-screen activities.

**Preview works, but unlock does not show the prompt**  
Confirm **display over other apps** is allowed for Mindful and the **Mindful is on** notification is present. Lock the phone completely (screen off), then unlock—not just wake the screen while still on the lock screen. On Samsung, Xiaomi, Oppo, Vivo, etc., also allow **autostart** / **unrestricted background** for Mindful.

**Prompt every time feels too frequent**  
This is intentional for v1; saving motives or cooldowns would be a future enhancement.

## Project structure

```text
app/src/main/java/com/example/mindful/
  MainActivity.kt          # Setup UI
  UnlockMonitorService.kt  # Foreground service
  UnlockDetection.kt       # Unlock broadcasts + keyguard polling
  UnlockPrompt.kt          # Overlay / activity prompt
  PopupActivity.kt         # Fallback prompt UI
  BootReceiver.kt          # BOOT_COMPLETED
  MindfulPrefs.kt          # Monitoring enabled flag
  MindfulNotifications.kt  # Notification channel
```

## Tech stack

- Kotlin
- Android SDK 34 (min SDK 24)
- AppCompat + XML layouts for the prompt and setup screen
- Jetpack Compose theme scaffolding (not used for the main flow)

## License

No license file is included yet. Add one if you plan to publish or open-source the project.
