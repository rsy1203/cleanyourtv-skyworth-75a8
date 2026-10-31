# Skyworth 75A8 port notes

This document records the changes that make this CleanYourTV fork run on one Skyworth 75A8 with Coocaa UI and Android 6.0 (API 23). The upstream project intentionally targets Android 8.0 (API 26) and newer; this fork keeps the lower minimum SDK in a separate repository rather than proposing a change to upstream's default launcher.

## Tested device and firmware

| Item | Observed value |
|---|---|
| TV model | Skyworth 75A8 |
| TV UI | Coocaa UI 9.0121117-211170 |
| Android version | Android 6.0 / API 23 |
| ABI | `armeabi-v7a` |
| Remote input device | `Skyworth_BT_RC86` |
| Key layout | `/system/usr/keylayout/Generic.kl` |
| Stock launcher | `com.tianci.tv/.SkyLauncher` |
| CleanYourTV package | `com.gathin.tvhome` |

Only this model/firmware combination has been verified by the maintainer of this fork. The exact firmware fingerprint and other hardware identifiers are intentionally not published.

## Porting changes

### Android API 23 compatibility

- Changed `minSdk` from 26 to 23.
- Enabled core-library desugaring and added `desugar_jdk_libs` for Java library APIs used by the project.
- Added AndroidX Core 1.7.0, which is compatible with the chosen API 23 baseline.
- Replaced the API 26 `Files.readAllLines()` call with a UTF-8 `BufferedReader`/`InputStreamReader` implementation.
- Guarded API 26 hardware bitmap usage and used a software bitmap on API 23–25.
- Added `PackageChangeTracker` so `PackageManager.getChangedPackages()` is called only on API 26 and newer; older releases use the existing package-broadcast path.
- Guarded `AccessibilityService.disableSelf()` and `UserManager.isUserUnlocked()` usage where those APIs are unavailable on API 23.
- Added fallbacks for API-level differences in network callbacks, locale access, remote key constants, TV input metadata and other framework calls.
- Kept Outfit font resources in `values-v26` and `values-vi-v26`; API 23–25 use system sans-serif mappings.
- Removed layout autosizing attributes unavailable on API 23 from the RTL layout variant.

### Skyworth Coocaa input sources

This firmware does not expose the standard Android TV `TvInputManager` Binder service. The launcher therefore guards the standard TV-input manager calls against a missing manager or runtime failure, so the Inputs UI does not crash on this device.

The Coocaa provider is queried at:

```text
content://com.skyworth.tv.sdk.provider/system/source_list
```

Source switching is performed through the provider method URI:

```text
content://com.skyworth.tv.sdk.provider/method
```

using the `METHOD_GOTO_SOURCE` operation and the source name/index returned by the list query. The old `com.skyworth.tv.provider` authority did not return the source list on this firmware. The corrected provider was queried on the TV, and HDMI 3 was successfully selected with a live set-top-box picture.

The Inputs integration is enabled through the Skyworth-specific `coocaa_sources` configuration flag. It is not enabled as a global upstream default.

### Physical Home key

The remote reports the Linux `KEY_HOMEPAGE` event, which the TV's `Generic.kl` maps to Android `KEYCODE_HOME`. The OK key maps to `KEYCODE_DPAD_CENTER`. No key-layout or firmware modification was made.

The fork adapts `HomeKeyService` to optionally handle `KEYCODE_HOME` when `home_long_press_home` is enabled. On this TV the accessibility service was enabled explicitly by the owner. The original stock launcher remains installed and enabled; the port does not change the system's default Home component. The service starts CleanYourTV explicitly when it consumes the Home event.

The accessibility-service XML requests key-event filtering and does not request window-content retrieval. The service is optional and can be disabled through the TV's Accessibility settings.

### Device-specific configuration

The fork's `config.xml` enables the Coocaa Sources integration, the launcher's own Settings button, and Home-key handling for the Skyworth remote. These are settings for the tested device, not new defaults for all CleanYourTV users.

## Validation record

On the physical Skyworth 75A8:

- Debug build, unit tests and lint completed successfully during development.
- Release `testReleaseUnitTest`, `lintRelease` and `assembleRelease` all completed successfully on 31 October 2026.
- The tested build environment used Temurin JDK 21.0.13, Gradle 8.9, Android SDK Platform 34, and Android Build Tools 34.0.0.
- The release APK measured 825,523 bytes (SHA-256 recorded locally by the maintainer; no APK or device-specific checksum is published here).
- D-pad navigation and OK activation worked.
- The launcher's Settings page and the link to system Settings opened correctly.
- Coocaa input-source discovery returned HDMI, AV and tuner entries; HDMI 3 switching was verified with a connected set-top box.
- The physical Home key was tested for short and held presses and when returning from another app. CleanYourTV opened as intended.
- The stock Skyworth launcher remained installed and enabled throughout testing.

The upstream launcher's 589,824-byte (576 KiB) APK size gate is not met by this API 23 port. The successful build is a fork-specific validation, not a claim that the upstream CI size check passes.

## Known limitations and unverified behaviour

- `Settings.Global.BOOT_COUNT` is not available on API 23. The port's boot-count helper returns an unavailable value on that release, so features that rely on the newer boot-count mechanism, such as the upstream first-Home/open-on-start tracking, are not verified as working on Android 6.0.
- Network callback compatibility was added for API 23, but complex combinations of multiple active networks and VPN transitions have not been separately validated on this TV.
- The standard Android TV input framework is unavailable on this firmware. The tested Coocaa provider integration is specific to the observed provider contract and may differ on other Coocaa firmware versions.
- Cold boot behaviour, every HDMI/AV/tuner variant, firmware updates, and other Skyworth models have not been exhaustively tested.
- The upstream install script's stock-launcher handover has not been adapted or validated for this model. Keep the original Skyworth launcher enabled and use a device-specific, reversible installation procedure.

## Dependency and licence notes

The port retains the upstream launcher dependencies and adds `com.android.tools:desugar_jdk_libs:2.1.5` plus `androidx.core:core:1.7.0` for the API 23 build. No new network permission or backend is introduced. Source changes and documentation are offered under the upstream MIT licence; the Outfit font remains under SIL Open Font License 1.1.
