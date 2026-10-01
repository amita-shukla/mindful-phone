# mindful

An Android app that asks you to pause before using your phone. Each time you **unlock** your device, a prompt appears so you can briefly note **why** you picked it up—or tap **SOS** to skip when you're in a hurry.

## What it does

- Runs a lightweight background monitor while enabled
- Shows a full-screen prompt on unlock (overlay when permitted)
- Single text field for your intention
- **SOS — skip for now** for urgent moments: calls, payments
- No accounts, no cloud—everything stays on your device

## Requirements

- Android 7.0 (API 24) or higher
- **Display over other apps** (required for reliable prompts)
- **Notifications** (Android 13+) so the monitoring service can stay active
- **Disable battery optimization** (recommended on many OEMs)

## Getting started (developers)

1. Open the project in **Android Studio**.
2. Connect a device with **USB debugging** enabled, or use an emulator.
3. Run the **app** configuration (`Run ▶`).

Debug APK output:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Getting started (on your phone)

1. Install and open **mindful**.
2. Tap **Allow display over other apps** and enable it for mindful.
3. Optionally tap **Disable battery optimization** and allow it.
4. Allow **Notifications** if Android asks.
5. Tap **Start monitoring**. You should see a persistent **“Mindful is active”** notification.
6. Tap **Test prompt now** to confirm the UI appears.
7. Lock the phone, then **unlock fully** (PIN / biometric / pattern). The prompt should show shortly after unlock.

## How it works

| Component | Role |
|-----------|------|
| `MainActivity` | One-time setup: permissions and starting monitoring |
| `UnlockMonitorService` | Foreground service that keeps unlock detection alive |
| `UnlockDetection` | Listens for unlock via `USER_PRESENT`, `USER_UNLOCKED`, and keyguard polling after `SCREEN_ON` |
| `UnlockPrompt` | Shows the prompt as a system overlay (fallback: `PopupActivity`) |
| `BootReceiver` | Restarts monitoring after reboot if you already enabled it |

Unlock detection uses more than one signal because some manufacturers do not deliver `ACTION_USER_PRESENT` reliably to background apps.

## Permissions

| Permission | Why |
|------------|-----|
| `SYSTEM_ALERT_WINDOW` | Draw the prompt over the home screen and other apps |
| `POST_NOTIFICATIONS` | Required notification for the foreground service (Android 13+) |
| `FOREGROUND_SERVICE` / `FOREGROUND_SERVICE_SPECIAL_USE` | Legitimate background unlock monitoring (Android 14+) |
| `RECEIVE_BOOT_COMPLETED` | Resume monitoring after device restart |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Reduce the system killing the monitor (optional but recommended) |

## Troubleshooting

**App crashes on “Start monitoring”**  
Use a recent build. Android 14+ requires the correct foreground service type when starting the service.

**“Test prompt now” does nothing**  
Overlay permission is missing or off. Check **Settings → Apps → mindful → Display over other apps**.

**Test works, but unlock does not show the prompt**  
Confirm the **Mindful is active** notification is present. Lock the phone completely (screen off), then unlock—not just wake the screen while still on the lock screen. On Samsung, Xiaomi, Oppo, Vivo, etc., also allow **autostart** / **unrestricted background** for mindful.

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
