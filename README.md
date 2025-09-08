# Vopenia's SDK

## Installation

### LiveKitClientKotlin

iOS requires a specific wrapper around the Objc/Swift implementation due to ABI compatibility.

The project is available at https://github.com/vopenia-io/pod-LiveKitClientKotlin and is published to to https://github.com/vopenia-io/pod-repo

Future version should be able to get rid of this using directly the SPM support from projects such as [https://github.com/frankois944/spm4Kmp](https://github.com/frankois944/spm4Kmp)

### LiveKit configurations

Set the given keys to your ~/.gradle/gradle.properties file (or create it) :

```
TEST_CONFIG_LIVEKIT_URL
TEST_CONFIG_LIVEKIT_API_KEY
TEST_CONFIG_LIVEKIT_API_SECRET

VOPENIA_SAMPLE_APP_TOKEN_ENDPOINT
```

### Google Services

Install the google-services.json file directly inside appAndroid/

### App signature

Update your gradle.properties file with the following :

```
VOPENIA_STORE_FILE=
VOPENIA_STORE_PASSWORD=
VOPENIA_KEY_ALIAS=
VOPENIA_KEY_PASSWORD=
```

### AppTester

Just like previous keys, use `VOPENIA_APP_DISTRIBUTION_APP_ID` & set the credentials file to
`androidApp/vopenia-service-crendentials.json`

## Usage

```bash
./gradlew publishToMavenLocal
```

## App's previews

- Install the HotPreview plugin into IntelliJ/Android
- restart
- done