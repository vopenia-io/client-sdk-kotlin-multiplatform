# Vopenia SDK (client-sdk-kotlin-multiplatform)

**LiveKit Kotlin Multiplatform Wrapper**

Kotlin Multiplatform wrapper around [LiveKit 2.6.0](https://livekit.io/), providing a unified Kotlin API for real-time video and audio communication across Android, iOS, and JVM/Desktop. The SDK abstracts LiveKit's platform-specific SDKs behind Kotlin `expect`/`actual` patterns and exposes reactive `StateFlow`-based APIs for room state, participants, and tracks.

This is the foundation library consumed by [kotlin-meet-sdk](../kotlin-meet-sdk) and [kotlin-multiplatform-visio](../kotlin-multiplatform-visio) via composite builds.

## Architecture

The SDK wraps LiveKit's native SDKs into a common KMP API:

- **Room** -- Connection lifecycle and participant discovery. Wraps LiveKit Room and exposes room state, connection status, and participant lists as `StateFlow`.
- **Participants** -- Local and remote participant state, speaking detection, and transcription. Wraps LiveKit's participant model into a `LocalParticipant`/`RemoteParticipant` hierarchy.
- **Tracks** -- Audio and video track management and rendering via `VideoSink`. Wraps LiveKit Track into `AudioTrack`, `VideoTrack`, and `NoneTrack` types.
- **Platform bridging**:
  - **Android**: wraps `io.livekit.android` SDK directly
  - **iOS**: wraps `LiveKitClient` via the `LiveKitClientKotlin` pod, which bridges Obj-C/Swift LiveKit delegates to Kotlin/Native
  - **JVM/Desktop**: stub implementation (no native LiveKit SDK available)

## Modules

| Module | Description |
|---|---|
| `:vopenia` | Core module -- KMP wrapper around LiveKit Room, Participant, and Track APIs |
| `:vopenia-compose` | Compose UI components (`VideoView`, `CameraPreviewView`) for rendering LiveKit video tracks |
| `:vopenia-participants` | Participant and track abstractions wrapping LiveKit's participant model |
| `:vopenia-utils` | Shared utilities |
| `:vopenia-test-config` | Test configuration helpers |
| `:shared` | Shared KMP module |
| `:appAndroid` | Android sample/test app |
| `:appJvm` | JVM/Desktop sample app |

## Platform Targets

- Android (`androidTarget`)
- iOS (`iosArm64`, `iosSimulatorArm64`, `iosX64`)
- JVM/Desktop (`jvm()`)

## Prerequisites

- JDK (bundled with Android Studio recommended)
- Android SDK
- CocoaPods (`brew install cocoapods`)
- LiveKit 2.6.0

## Build Commands

| Command | Description |
|---|---|
| `./gradlew build` | Full build (all targets) |
| `./gradlew :appAndroid:installDebug` | Build and install Android sample app |
| `./gradlew :appJvm:run` | Run JVM/Desktop sample app |
| `./gradlew linkDebugFrameworkIosSimulatorArm64` | Build iOS framework for simulator |
| `./gradlew check` | Run all checks and tests |
| `./gradlew publishToMavenLocal` | Publish SDK to local Maven repository |

## Android Setup

Build and install the Android sample app:

```bash
./gradlew :appAndroid:installDebug
```

### Google Services

Install the `google-services.json` file directly inside `appAndroid/`.

### App Signature

Add the following to your `~/.gradle/gradle.properties`:

```properties
VOPENIA_STORE_FILE=
VOPENIA_STORE_PASSWORD=
VOPENIA_KEY_ALIAS=
VOPENIA_KEY_PASSWORD=
```

### AppTester

Set `VOPENIA_APP_DISTRIBUTION_APP_ID` in your gradle.properties and place the credentials file at `androidApp/vopenia-service-credentials.json`.

## iOS Setup

### LiveKitClientKotlin

iOS requires a specific wrapper around the Obj-C/Swift LiveKit implementation due to ABI compatibility.

The project is available at https://github.com/vopenia-io/pod-LiveKitClientKotlin and is published to https://github.com/vopenia-io/pod-repo

Future versions should be able to get rid of this by using SPM support directly from projects such as [spm4Kmp](https://github.com/frankois944/spm4Kmp).

### Building for iOS

1. Build the framework:
   ```bash
   ./gradlew linkDebugFrameworkIosSimulatorArm64
   ```
2. Install CocoaPods dependencies:
   ```bash
   cd appIos && pod install
   ```
3. Open the Xcode workspace:
   ```bash
   open appIos/appIos.xcworkspace
   ```
4. iOS deployment target: **16.0**

## Configuration

### LiveKit

Set the following keys in your `~/.gradle/gradle.properties` file:

```properties
# LiveKit server connection (used by :vopenia-test-config for integration tests)
TEST_CONFIG_LIVEKIT_URL=wss://your-project.livekit.cloud
TEST_CONFIG_LIVEKIT_API_KEY=APIxxxxxxxxxxxxxxx
TEST_CONFIG_LIVEKIT_API_SECRET=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx

# Backend token endpoint (used by sample apps to obtain a LiveKit access token)
# The sample app POSTs {"participant":"...", "room":"..."} and expects {"token":"...", "url":"..."}
VOPENIA_SAMPLE_APP_TOKEN_ENDPOINT=https://your-token-service.example.com
```

## Usage as Dependency

Other repos in this workspace consume this SDK via Gradle composite builds. The `settings.gradle` in consuming repos includes:

```groovy
includeBuild("../client-sdk-kotlin-multiplatform") {
    dependencySubstitution {
        substitute module("io.vopenia:vopenia") using project(":vopenia")
        // ...
    }
}
```

For standalone use, publish to local Maven first:

```bash
./gradlew publishToMavenLocal
```

## Development

### HotPreview

- Install the HotPreview plugin into IntelliJ / Android Studio
- Restart the IDE
- For iOS, add `kotlin.apple.cocoapods.bin=/location/to/pod` in `local.properties`
