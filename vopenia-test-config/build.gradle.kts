import com.codingfeline.buildkonfig.compiler.FieldSpec

plugins {
    alias(additionals.plugins.kotlin.multiplatform)
    alias(additionals.plugins.android.library)
    alias(additionals.plugins.multiplatform.buildkonfig)
    id("jvmCompat")
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = rootProject.ext["javaVersion"] as String
            }
        }
    }
    jvm()
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            // none at this moment
        }
    }
}

android {
    namespace = "com.vopenia.tests.config"
}

buildkonfig {
    packageName = "com.vopenia.tests.config"

    defaultConfigs {
        listOf(
            "LIVEKIT_URL" to rootProject.ext["testConfigliveKitUrl"] as String,
            "LIVEKIT_API_KEY" to rootProject.ext["testConfigliveKitApiKey"] as String,
        ).forEach { (name, value) ->
            buildConfigField(
                FieldSpec.Type.STRING,
                name,
                value,
                nullable = false,
                const = true
            )
        }
    }
}
