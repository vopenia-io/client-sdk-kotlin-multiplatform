plugins {
    alias(additionals.plugins.kotlin.multiplatform)
    alias(additionals.plugins.android.library)
    alias(additionals.plugins.jetbrains.compose)
    alias(additionals.plugins.compose.compiler)
    alias(additionals.plugins.kotlin.cocoapods)
    id("jvmCompat")
    id("iosSimulatorConfiguration")
    id("publication")
}

kotlin {
    androidTarget {
        publishLibraryVariants("release", "debug")
    }
    jvm()
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    cocoapods {
        summary = "UI Compose library for vopanie"
        homepage = "Link to the Shared Module homepage"
        version = "1.0"
        specRepos {
            url("https://github.com/livekit/podspecs")
        }
        ios.deploymentTarget = "16.0"
        osx.deploymentTarget = "16.0"
        framework {
            baseName = "vopenia-compose"
            // isStatic = false
            // transitiveExport = true
        }

        // BBBACore must be declared everywhere LiveKitClientKotlin is —
        // LiveKitClientKotlin.podspec depends on it but it lives outside any
        // podspec repo (local sibling checkout).
        pod("BBBACore") {
            version = "1.0.0"
            source = path(rootProject.file("../BigBlueBetterAudio"))
            moduleName = "BBBACore"
            packageName = "BBBACore"
            extraOpts += listOf("-compiler-option", "-fmodules")
        }

        pod("LiveKitClientKotlin") {
            version = "2.6.0"
            source = path(rootProject.file("../LiveKitClientKotlin"))
            moduleName = "LiveKitClientKotlin"
            packageName = "LiveKitClientKotlin"
            extraOpts += listOf("-compiler-option", "-fmodules")
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)

            implementation(projects.vopenia)
            implementation(projects.vopeniaUtils)

            implementation(additionals.multiplatform.widgets.compose)
        }
        androidMain.dependencies {
            implementation(libs.livekit.android)
        }
    }
}

android {
    namespace = "${rootProject.ext["namespace"]}.compose"
}
