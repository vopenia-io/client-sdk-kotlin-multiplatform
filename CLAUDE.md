# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Vopenia's Kotlin Multiplatform SDK wrapping LiveKit for Android, JVM, and iOS. Group `eu.codlab`, current version in `version.properties` (see `vopenia/src/commonMain/.../Room.kt` which logs an alpha suffix independently — keep both in sync when bumping).

## Common commands

```bash
./gradlew publishToMavenLocal              # local publish, the typical build verification
./gradlew :vopenia:assemble                # build just the core SDK module across targets
./gradlew :appAndroid:installDebug         # install the Android sample
./gradlew :appJvm:run                      # run the JVM sample
./gradlew :vopenia:allTests                # run tests across all KMP targets for a module
./gradlew :vopenia:jvmTest                 # single-target tests
./gradlew :vopenia:testDebugUnitTest --tests "com.vopenia.sdk.AndroidTest"   # single test class

./scripts/publish.sh                       # publishToMavenLocal + Sonatype release
./scripts/androidDistribution.sh           # assemble & upload via Firebase App Distribution
```

iOS builds go through CocoaPods in `appIos/` (open `appIos.xcworkspace`). Run `pod install` there after changing any `cocoapods {}` block in a Gradle module.

### Local configuration (per-machine, not committed)

In `local.properties` at the repo root, set both:
- `sdk.dir=/path/to/Android/sdk` — Android SDK location.
- `kotlin.apple.cocoapods.bin=/path/to/pod` — required for the iOS CocoaPods integration to resolve the `pod` binary (typical: `/opt/homebrew/bin/pod` or `/usr/local/bin/pod`).

In `~/.gradle/gradle.properties`, set the LiveKit test config and the signing/distribution secrets: `TEST_CONFIG_LIVEKIT_URL`, `TEST_CONFIG_LIVEKIT_API_KEY`, `TEST_CONFIG_LIVEKIT_API_SECRET`, `VOPENIA_SAMPLE_APP_TOKEN_ENDPOINT`, `VOPENIA_STORE_FILE`, `VOPENIA_STORE_PASSWORD`, `VOPENIA_KEY_ALIAS`, `VOPENIA_KEY_PASSWORD`, `VOPENIA_APP_DISTRIBUTION_APP_ID`.

Drop the following files into `appAndroid/` (all resolved relative to `appAndroid/` at build time — see `appAndroid/build.gradle.kts`):
- `google-services.json` — required by the `com.google.gms.google-services` plugin; without it the Android app won't build.
- The release **keystore (JKS/p12)** at the path given by `VOPENIA_STORE_FILE` (that property is interpreted as a path *relative to* `appAndroid/`, e.g. `VOPENIA_STORE_FILE=vopenia-release.jks` → file at `appAndroid/vopenia-release.jks`). If the keystore is missing, signing is silently skipped and only debug builds work.
- `vopenia-service-crendentials.json` — Firebase service-account JSON used by `firebaseAppDistribution` when uploading the release APK. If absent, the distribution block is skipped but the release build still succeeds.

## Architecture

**Published SDK modules** (group `eu.codlab`, Android + JVM + iOS targets):
- `vopenia-utils` — `Dispatchers`, `Log`, platform shims. Depended on by everything.
- `vopenia-participants` — `Participant`, `LocalParticipant`, `RemoteParticipant`, `Track`/`TrackPublication` hierarchy, `ParticipantPermissions`, `PermissionsController`. The participant/track model lives here, separate from `Room`.
- `vopenia` — `Room` / `InternalRoom`, connection lifecycle, `ConnectionState`. `api`-depends on `vopenia-participants` so consumers get both transitively.
- `vopenia-compose` — Compose Multiplatform UI helpers on top of `vopenia`.
- `vopenia-test-config` — test-only fixtures that read the `TEST_CONFIG_LIVEKIT_*` gradle props; only consumed via `commonTest`.

**Non-published:**
- `shared` — Compose UI for the sample apps (routing, screens, theme, HTTP token fetch). Not part of the SDK.
- `appAndroid`, `appJvm`, `appIos` — sample apps that exercise the SDK.
- `lambda/` — separate Node/Serverless project that issues LiveKit tokens to the sample apps (hit by `shared/.../http/BackendConnection.kt`, endpoint from `VOPENIA_SAMPLE_APP_TOKEN_ENDPOINT`).

**`expect`/`actual` pattern.** Each public class in `vopenia` and `vopenia-participants` has a thin common `expect class` and per-platform `actual` implementations. Example: `com.vopenia.livekit.InternalRoom` is declared `expect` in `commonMain` and implemented in `androidMain`/`iosMain`/`jvmMain`, each delegating to its native LiveKit SDK:
- Android: `io.livekit:livekit-android` (Maven).
- iOS: the `LiveKitClient` (Swift/ObjC) Pod plus `LiveKitClientKotlin`, a wrapper Pod from `https://github.com/vopenia-io/pod-repo` needed for Kotlin/Native ABI compatibility. Some modules consume it from a sibling checkout at `../LiveKitClientKotlin`; others pull it from the pod repo. See README about `spm4Kmp` as the intended future replacement.
- JVM: currently a stub — there is no JVM LiveKit backend, `InternalRoom` on JVM won't actually connect.

When adding SDK surface area: add `expect` in `commonMain`, `actual` in every target (including a JVM no-op if needed), and re-export from `Room`/`Participant` if it's part of the public API. Don't forget the iOS Pod's module header if the new API uses new LiveKit types — the `extraOpts = listOf("-compiler-option", "-fmodules")` is already set in the `cocoapods {}` blocks.

## Build plumbing

- Root `settings.gradle` applies `https://raw.githubusercontent.com/the-inkwell/gradle-tools/${GRADLE_EXTENDED_VERSION_USED}/extended.gradle`, pinned via `GRADLE_EXTENDED_VERSION_USED` in `gradle.properties`. This provides the `additionals` version catalog (separate from the standard `libs`) and the `jvmCompat`, `iosSimulatorConfiguration`, `publication` convention plugins applied by every SDK module. `rootProject.ext.namespace`, `group`, `version`, `javaVersionObject` are set there.
- `version.properties` is the single source of truth for the SDK version; keep the `Log.d` tag in `Room.kt` aligned when you bump it (it embeds a hand-written version string).
- `buildkonfig` generates a `VERSION` constant per SDK module from the root version.
- CocoaPods modules patch the synthetic `project.pbxproj` in `PodInstallSyntheticTask` to force `IPHONEOS_DEPLOYMENT_TARGET = 14.1`. If you see iOS deployment-target errors after a pod change, look there first.
- `publication` plugin publishes to Sonatype via `nexusPublishing` in the root `build.gradle`; `sonatypeUsername`/`sonatypePassword` come from `~/.gradle/gradle.properties`.
