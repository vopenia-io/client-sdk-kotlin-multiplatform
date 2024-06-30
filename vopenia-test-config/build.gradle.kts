import com.codingfeline.buildkonfig.compiler.FieldSpec

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(additionals.plugins.multiplatform.buildkonfig)
    id("jvmCompat")
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
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
    compileSdk = additionals.versions.compileSdkVersion.get().toInt()
    defaultConfig {
        minSdk = additionals.versions.minSdkVersion.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
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
