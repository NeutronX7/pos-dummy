# Sunmi Setup Notes

This project starts with a dummy Sunmi integration so receipt flows can be built before hardware arrives.

## What is already wired

- `app/src/main/java/com/example/pos_dummy/domain/repository/PosPrinterRepository.kt`
  Defines the printer contract used by the app.
- `app/src/main/java/com/example/pos_dummy/domain/model/PrinterModels.kt`
  Stores printer, receipt, print-result, and UI-state models.
- `app/src/main/java/com/example/pos_dummy/data/repository/FakeSunmiPrinterRepository.kt`
  Simulates connection, ready/error states, and receipt rendering.
- `app/src/main/java/com/example/pos_dummy/presentation/viewmodel/SunmiDummyViewModel.kt`
  Owns the dummy screen state and user actions.
- `app/src/main/java/com/example/pos_dummy/presentation/screen/SunmiDummyScreen.kt`
  Renders the screen and delegates behavior to the view-model layer.
- `app/src/main/AndroidManifest.xml`
  Declares package visibility for `woyou.aidlservice.jiuiv5`.

## Sunmi doc points this project is based on

- Sunmi recommends the remote dependency library for Gradle projects:
  `com.sunmi:printerlibrary:1.0.15`
- The print service package is:
  `woyou.aidlservice.jiuiv5`
- The common print service action is:
  `woyou.aidlservice.jiuiv5.IWoyouService`
- AIDL resources are device-family specific, so handheld devices such as V2, V2s, and P2 should use the handheld package if you go the AIDL route.

## When the real device arrives

1. Add the real Sunmi dependency in `app/build.gradle.kts`.
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
