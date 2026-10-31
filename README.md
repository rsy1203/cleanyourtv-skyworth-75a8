# CleanYourTV — Skyworth 75A8 (Android 6.0 / API 23)

A device-specific port of [CleanYourTV](https://github.com/Gathin23/cleanyourtv) for the **Skyworth 75A8** running **Coocaa UI 9.0121117-211170 on Android 6.0 (API 23)**.

This fork carries the upstream launcher forward to an older Android release and adds the Coocaa input-source integration needed by this TV. It is an independent, community-maintained port, not an official release of CleanYourTV or Skyworth.

> **Tested device:** Skyworth 75A8 · Coocaa UI 9.0121117-211170 · Android 6.0 / API 23 · ARM 32-bit (`armeabi-v7a`)
>
> **Test status:** installed and exercised on the physical TV. See [FORK.md](FORK.md) for the compatibility notes and test coverage.

## What this port changes

- Lowers the launcher's `minSdk` from API 26 to API 23, with guards or fallbacks for framework APIs unavailable on Android 6.0.
- Adds Java library desugaring and the AndroidX Core compatibility dependency used by the API 23 build.
- Uses system sans-serif fonts on API 23–25 and retains the Outfit font resources on API 26 and newer.
- Handles Skyworth firmware that does not provide the standard Android TV Input Framework service, avoiding a launcher crash when querying TV inputs.
- Reads the Coocaa source list from `content://com.skyworth.tv.sdk.provider/system/source_list` and switches inputs through the Coocaa provider method. HDMI 3 was tested with a connected set-top box.
- Adapts Home-key handling so the optional `HomeKeyService` can intercept the Skyworth remote's standard `KEYCODE_HOME` event. The service is opt-in and must be enabled by the user.

The Skyworth configuration enables the Coocaa Inputs integration, the launcher's own Settings button, and Home-key handling for this remote. These are device-specific defaults in this fork; they are not proposed as defaults for upstream CleanYourTV.

## Verified on the TV

The following were tested on the physical Skyworth 75A8:

- CleanYourTV starts and remains usable as the home screen.
- D-pad Up, Down, Left, Right and OK navigation.
- The launcher's Settings screen and its entry to the TV's system Settings.
- Coocaa Inputs list discovery, including HDMI, AV and tuner entries.
- Switching to HDMI 3 and displaying the connected set-top box.
- The physical remote's Home key, including short and held presses, returning to CleanYourTV from the launcher and another app.
- The original Skyworth `com.tianci.tv/.SkyLauncher` remains installed and enabled as a fallback. This port does not require replacing or disabling it as the system Home app.

This is a report for one model and one firmware build. Other Skyworth or Coocaa models and firmware versions are not implied to be compatible.

## Build from source

Requirements:

- JDK 17 or newer (the tested build used Temurin JDK 21).
- Android SDK Platform 34 and Build Tools 34.0.0.
- The Gradle wrapper included in this repository (Gradle 8.9 in the tested build).

From the repository root:

```bash
cd launcher
./gradlew --no-daemon testReleaseUnitTest lintRelease assembleRelease --console=plain
```

The APK is written to:

```text
launcher/app/build/outputs/apk/release/app-release.apk
```

No prebuilt APK is committed. The release build verified on 31 October 2026 completed successfully; its APK was **825,523 bytes**. This is larger than the upstream reference launcher's 589,824-byte (576 KiB) size gate. This fork does **not** claim to meet that upstream size limit.

## Install and enable Home-key handling

Build the APK yourself, then sideload it using Android Debug Bridge:

```bash
adb install -r launcher/app/build/outputs/apk/release/app-release.apk
```

Open CleanYourTV once. To make the remote's Home button return to CleanYourTV, enable **Home button** (`HomeKeyService`) in the TV's Accessibility settings. This is an explicit, optional user action. The service requests remote key-event filtering; it does not request access to window contents. On this tested firmware it was enabled through ADB because the service needed to be added to Android's enabled accessibility-service list.

Do not overwrite an existing `enabled_accessibility_services` list blindly: it may contain other accessibility services. Record the current setting first and preserve any services already enabled. Turning off this service in Accessibility settings restores the previous Home-key behavior.

**Keep the stock Skyworth launcher enabled.** This port was tested with `com.tianci.tv/.SkyLauncher` still installed and enabled. Do not run the upstream `launcher/build-and-install.sh` on this TV as a substitute for the device-specific procedure; this fork has not adapted or validated that script's stock-launcher handover for this firmware.

## Permissions and network behaviour

The launcher manifest retains the permissions already present in upstream CleanYourTV, including network-state and Wi-Fi-state access, fine location for displaying the Wi-Fi network name on Android versions that require it, and `WRITE_SECURE_SETTINGS` for the upstream optional accessibility-service repair path. It also retains the upstream Yandex TV source permission, which is unrelated to this Skyworth integration.

The Skyworth-specific changes add **no `INTERNET` permission**, analytics, telemetry, network service, or remote backend. The Coocaa input integration uses the TV's local content provider. The optional accessibility service filters remote key events and does not request window-content retrieval.

## Scope and safety

- No root access, firmware flashing, package uninstalling, or modification of the TV's key-layout file is required.
- The stock Skyworth launcher is deliberately kept enabled.
- The original upstream project remains the source of the common launcher and tools. This fork retains the upstream project tree and adds the Skyworth/API 23 adaptation and its device-specific documentation.
- Back up your TV configuration before making changes. Use ADB only on a TV you own or are authorised to administer.

## Upstream project and licence

- Upstream: [Gathin23/CleanYourTV](https://github.com/Gathin23/cleanyourtv)
- Port notes: [FORK.md](FORK.md)
- Licence: MIT; see [LICENSE](LICENSE). The Outfit typeface retains its separate SIL Open Font License 1.1 notice in `launcher/FONT-LICENSE-OFL.txt`.

Skyworth and Coocaa are trademarks of their respective owners. This project is independent and is not affiliated with or endorsed by those companies.
