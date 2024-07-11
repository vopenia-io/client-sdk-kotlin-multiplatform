import com.codingfeline.buildkonfig.compiler.FieldSpec

plugins {
    alias(additionals.plugins.kotlin.multiplatform)
    alias(additionals.plugins.android.library)
    alias(additionals.plugins.jetbrains.compose)
    alias(additionals.plugins.compose.compiler)
    id("jvmCompat")
    id("iosSimulatorConfiguration")
}

kotlin {
    androidTarget {
        publishLibraryVariants("release", "debug")
    }
    jvm()
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)

            implementation(projects.vopenia)
            implementation(projects.vopeniaUtils)
        }
        androidMain.dependencies {
            implementation(libs.livekit.android)
        }
    }
}

android {
    namespace = "${rootProject.ext["namespace"]}.compose"
    compileSdk = additionals.versions.compileSdkVersion.get().toInt()
    defaultConfig {
        minSdk = additionals.versions.minSdkVersion.get().toInt()
    }
    compileOptions {
        sourceCompatibility = rootProject.ext["javaVersionObject"] as JavaVersion
        targetCompatibility = rootProject.ext["javaVersionObject"] as JavaVersion
    }
}
