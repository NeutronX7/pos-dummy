# Sunmi Setup Notes

This project starts with a dummy Sunmi integration so receipt flows can be built before hardware arrives.

The SUNMI integration now lives in the dedicated `sunmi` library module. The `app` module only hosts the demo UI.

## What is already wired

- `sunmi/src/main/java/com/example/sunmi/domain/repository/PosPrinterRepository.kt`
  Defines the printer contract used by the app.
- `sunmi/src/main/java/com/example/sunmi/domain/model/PrinterModels.kt`
  Stores printer, receipt, print-result, and UI-state models.
- `sunmi/src/main/java/com/example/sunmi/data/repository/FakeSunmiPrinterRepository.kt`
  Simulates connection, ready/error states, and receipt rendering.
- `sunmi/src/main/java/com/example/sunmi/data/repository/SunmiContactlessPaymentRepository.kt`
  Wraps the SUNMI Pay SDK contactless card-detection flow.
- `app/src/main/java/com/example/pos_dummy/presentation/viewmodel/HomeViewModel.kt`
  Owns the demo screen state and delegates business logic to the `sunmi` module.
- `app/src/main/java/com/example/pos_dummy/presentation/screen/HomeScreen.kt`
  Renders the demo UI and wires Android NFC reader mode for the optional native probe.
- `sunmi/src/main/AndroidManifest.xml`
  Declares the SUNMI-specific permissions, package visibility, and EMV activity needed by the SDK.

## Sunmi doc points this project is based on

- Sunmi recommends the remote dependency library for Gradle projects:
  `com.sunmi:printerlibrary:1.0.15`
- The print service package is:
  `woyou.aidlservice.jiuiv5`
- The common print service action is:
  `woyou.aidlservice.jiuiv5.IWoyouService`
- AIDL resources are device-family specific, so handheld devices such as V2, V2s, and P2 should use the handheld package if you go the AIDL route.

## When the real device arrives

1. Add or update the real Sunmi dependency in `sunmi/build.gradle.kts`.
2. Replace `FakeSunmiPrinterRepository` with a `SunmiPrinterLibraryRepository` that binds through Sunmi's printer library.
3. Keep `PosPrinterRepository` unchanged so the UI and business flow remain stable.
4. Test at least these cases on hardware:
   - service bind success
   - no paper
   - overheated printer
   - cover open
   - normal receipt print

## Important constraint

Without a Sunmi terminal or the vendor SDK downloaded in the build environment, this project can only simulate the printer path. That is intentional for now.
