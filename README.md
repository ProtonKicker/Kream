# Kocoa Beam - Klipper for Android

**Read this in other languages: [English](README.md) · [简体中文](README.zh-Hans.md) · [繁體中文](README.zh-Hant.md)**

## What's in a Name?

**Kocoa Beam** is named after cocoa beans — the smooth, rich foundation of chocolate. Like cocoa beans transformed into something warm and delightful, Kocoa Beam takes the raw energy of [Beam Klipper](https://github.com/utkabobr/BeamKlipper) and refines it into a softer, sweeter experience.

The "K" honors our Kotlin roots and Klipper heritage. The "Beam" pays tribute to the original [Beam Klipper](https://github.com/utkabobr/BeamKlipper) by [ProtonKicker](https://github.com/ProtonKicker). Together, it's a name that's as warm and approachable as a cup of hot cocoa.

Kocoa Beam allows you to run [Klipper](https://github.com/KevinOConnor/klipper) or [Kalico](https://github.com/KalicoDTU/kalico) host software on any Android 5.0+ device with OTG support.

## Why Kocoa Beam?

Kocoa Beam is a complete overhaul of Beam Klipper with three major improvements:

### 1. Kotlin Rewrite
The entire application has been migrated from Java to Kotlin, bringing:
- **Null safety** — compile-time prevention of NullPointerExceptions
- **Coroutines** — automatic cleanup of background threads, no more leaks
- **Immutable data classes** — thread-safe event bus messages and database entities
- **Smart casts & exhaustiveness checks** — bugs caught at compile time, not runtime

### 2. Dramatically Smaller Size
Kocoa Beam is significantly smaller than the original Beam Klipper:

| Component | Beam Klipper | Kocoa Beam |
|-----------|-------------|------------|
| FFmpeg timelapse | Bundled binary (~40 MB) | Android MediaCodec API (built-in) |
| App size | ~138 MB (arm64) | ~64 MB (arm64 / armv7), ~71 MB (x86_64) |

The FFmpeg timelapse component was replaced with Android's native MediaCodec API, saving ~40 MB per architecture.

### 3. Brand New UI
Kocoa Beam features a complete UI redesign with:
- Brutalist bento-box aesthetic with "Paper/Honey/Ink" color palette
- Hard offset shadows and bold borders
- Modern Jetpack Compose implementation
- Improved layout and usability

### Additional Features
- **10 concurrent instances** — run up to 10 printer profiles simultaneously (vs. 4 in Beam Klipper)
- **Dual firmware support** — run Klipper or Kalico firmware engines
- **Native timelapse** — uses Android's hardware MediaCodec instead of bundled FFmpeg
- **Local-only operation** — no cloud connectivity; all data stays on your device (Beam Cloud support removed)

## Choosing the Right Package

Kocoa Beam provides three APK variants:

| Architecture | Package Name | Use Case |
|-------------|--------------|----------|
| arm64 | `KocoaBeam_*_arm64.apk` | Modern 64-bit devices (recommended) |
| armv7 | `KocoaBeam_*_armv7.apk` | Older 32-bit devices |
| x86_64 | `KocoaBeam_*_amd64.apk` | x86_64 tablets, Chromebooks, Android emulators |

**How to check your device architecture:**
- **Settings > About Phone > Architecture** or **Kernel Architecture**
- Or install a CPU info app like "CPU-Z" or "AIDA64"
- If unsure, try arm64 first — most devices released after 2015 support it

## What this project changes

This project keeps the bundled Klipper / Moonraker / Fluidd / Mainsail / Happy
Hare current and adds on-device diagnostics, opt-in Klipper add-ons and firmware
tooling. Details:

- [`docs/whats-new.md`](docs/whats-new.md) — full list of changes
- [`docs/build-firmware.md`](docs/build-firmware.md) — build MCU firmware for any board
- [`docs/mods/klipper-addons.md`](docs/mods/klipper-addons.md) — the bundled add-ons
- [`docs/mods/input-shaper-manual.md`](docs/mods/input-shaper-manual.md) — input shaper without an accelerometer
- [`docs/`](docs/index.md) — documentation index

# Quick Start

1. **MCU firmware** — flash the printer's mainboard, using either:
   - a pre-built image from the [Beam Klipper firmware list](https://github.com/utkabobr/klipper/releases)
     (the `prebuilt-v0.12.0` set covers many boards), **or**
   - a fresh Klipper 0.13 build — one command via
     [`docs/build-firmware.md`](docs/build-firmware.md) (Docker or a local script,
     for any supported board).

   Klipper 0.13 is recommended; older pre-built images also work.
2. Install the APK for your CPU from the [Releases tab](https://github.com/ProtonKicker/Cream/releases/latest).
3. Grant the requested permissions.
4. Add a printer instance (choose a `generic-*.cfg` if your printer is not listed).
5. Start the instance.
6. Open the web UI: Fluidd `http://IP:4408/` or Mainsail `http://IP:4409/` — the
   active URL is shown on the main screen. The serial port is auto-detected.

# Can I use device as regular after I install Kocoa Beam to it?

**Yes!** You definitely can!

Kocoa Beam does not do **anything** to your Android system, it runs in user-space as a regular Android app

# What's IP:port?

It's displayed on the main page when any instance is running. Each front end has
its own port, following the front-end toggle on the main screen:

- Fluidd => `http://IP:4408/`
- Mainsail => `http://IP:4409/`

Camera URLs:
- /webcam/?action=stream => `http://IP:8889/`
- /webcam/?action=snapshot => `http://IP:8889/snapshot`

Recommended camera config is mjpeg-**stream** (Not adaptive mjpeg) for Fluidd and UV4L-MJPEG for Mainsail

# What's inside?

Kocoa Beam bundles:
- [Klipper](https://github.com/KevinOConnor/klipper)
- [Kalico](https://github.com/KalicoDTU/kalico)
- [Moonraker](https://github.com/Arksine/moonraker)
- [Fluidd](https://github.com/fluidd-core/fluidd)
- [Mainsail](https://github.com/mainsail-crew/mainsail)
- [Happy Hare](https://github.com/moggieuk/Happy-Hare)
- [Klipper TMC Autotune](https://github.com/andrewmcgr/klipper_tmc_autotune)
- [Moonraker-timelapse](https://github.com/mainsail-crew/moonraker-timelapse)

## Updates

Bundled component versions in this project:

| Component | Version |
|---|---|
| Klipper / Kalico | current upstream (MCU firmware target: 0.13) |
| Moonraker | 0.11.0 |
| Fluidd | 1.37.5 |
| Mainsail | 2.19.0 |
| Happy Hare | v4.0.0 |

Opt-in Klipper add-ons are also bundled (KAMP, LED Effect, Z Calibration, Auto Speed, TMC Autotune) — see [`docs/mods/klipper-addons.md`](docs/mods/klipper-addons.md). Full change list: [`docs/whats-new.md`](docs/whats-new.md).

# Android Extensions

Kocoa Beam provides additional extensions to control some built-in features.

### Camera

Include `[kocoa_camera]` into your printer.cfg

`SET_CAMERA_FLASHLIGHT ENABLED=true/false` - Toggles flashlight

`SET_CAMERA_FOCUS AUTOFOCUS=true/false FOCUS_DISTANCE=0...?` - Sets camera autofocus state and focus distance if autofocus is disabled. `FOCUS_DISTANCE` is expressed in dioptres, it may vary from device to device

### Beeper

Include `[include kocoa_beeper.cfg]` into your printer.cfg

Use `M300` macro [as defined in docs](https://marlinfw.org/docs/gcode/M300.html)

# Autostart

You can put the app to autostart by setting needed printers to autostart **AND** setting app as default launcher.

You **must** remove lockscreen pincode if your device is encrypted (Enabled by default on most devices)

# Background Activity Notice

Some manufacturers may restrict app's performance or background process.
You can circumvent this by setting app as default launcher and allowing all the background tasks

# Android TV Support?

Yup. Should be working just fine. But please note that some cheap TV boxes does not support setting Kocoa Beam as launcher without disabling system one first, use ADB or root to disable it.

# What USB Hub to Use?

I'm using UGREEN Type-c hub (Not affiliated, but I'm waiting for your request UGREEN :D), but any should be fine if it works with your device and provides charging at the same time

# Restrictions

- Web server can't run on default port because Android/linux doesn't allow user-space apps to bind to ports less than 1024 and we want 80 for default `http://IP`
- Some devices may reset device path on firmware restart, you should use VID/PID naming in that case
- No SSH (You won't be able to build firmware or run additional autorun services anyway)
- Some devices doesn't support OTG and charging at the same time, you must solder directly to the battery pins in that case (Or use different device, it's up to you)
- Only 250000 baud rate is supported (I don't want to forward this setting into Android USB driver, almost all configurations use 250000 anyway)

# Building

One-shot setup (installs the pinned SDK / NDK / CMake, a Python 3.10 for Chaquopy, and writes `local.properties`):

- Linux / macOS: `./scripts/setup.sh`
- Windows: `.\scripts\setup.ps1`

Then `./gradlew :app:assembleArm64Debug`, or open the project in Android Studio and Run. Details, manual steps and signing: [`docs/build-app.md`](docs/build-app.md).

# Contributing

Pull requests are welcome!