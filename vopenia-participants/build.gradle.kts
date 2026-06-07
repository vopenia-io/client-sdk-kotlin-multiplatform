import com.codingfeline.buildkonfig.compiler.FieldSpec

plugins {
    alias(additionals.plugins.kotlin.multiplatform)
    alias(additionals.plugins.kotlin.cocoapods)
    alias(additionals.plugins.android.library)
    alias(libs.plugins.spm)
    alias(additionals.plugins.multiplatform.buildkonfig)
    id("jvmCompat")
    id("iosSimulatorConfiguration")
    id("publication")
}

kotlin {
    androidTarget {
        publishLibraryVariants("release", "debug")
    }
    jvm()
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries {
            framework {
                //transitiveExport = true
                baseName = "KotlinLibrary"
                isStatic = true
            }
        }
    }

    cocoapods {
        summary = "Some description for the Shared Module"
        homepage = "Link to the Shared Module homepage"
        version = "1.0"
        specRepos {
            url("https://github.com/livekit/podspecs")
        }
        ios.deploymentTarget = "16.0"
        osx.deploymentTarget = "16.0"
        framework {
            baseName = "vopenia-participants"
            // transitiveExport = true
        }

        pod("LiveKitClient") {
            version = "2.6.0"
            moduleName = "LiveKitClient"
            packageName = "LiveKitClient"
            extraOpts += listOf("-compiler-option", "-fmodules")
        }

        // BigBlueBetterAudio C/C++ core + iOS ObjC++ façade. Lives in its own
        // pod (rooted at the BigBlueBetterAudio repo top) because CocoaPods
        // rejects `../`-relative source paths. LiveKitClientKotlin depends on
        // it transitively via the podspec — we declare it here so the synthetic
        // Podfile picks the local sibling checkout instead of trying to fetch
        // BBBACore from a podspec repo (which doesn't exist).
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
            useInteropBindingFrom("LiveKitClient")
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(additionals.kotlinx.coroutines)
            implementation(additionals.multiplatform.permissions)
            implementation(additionals.multiplatform.permissions.bluetooth)
            implementation(additionals.multiplatform.permissions.camera)
            implementation(additionals.multiplatform.permissions.microphone)
            implementation(projects.vopeniaUtils)
        }
        commonTest.dependencies {
            implementation(additionals.kotlin.test)
            implementation(additionals.kotlinx.coroutines.test)
            implementation(projects.vopeniaTestConfig)
        }
        androidMain.dependencies {
            implementation(libs.livekit.android)
            api(additionals.androidx.fragment)
            // MediaPipe Tasks Vision for selfie segmentation (GPU delegate
            // via TFLite). The 244KB float16 model is bundled in androidMain/assets/.
            implementation("com.google.mediapipe:tasks-vision:0.10.14")
        }
        jvmMain.dependencies {
            implementation(additionals.kotlinx.coroutines.jvm)
        }
    }
}

android {
    namespace = "${rootProject.ext["namespace"]}.participants"
    compileSdk = additionals.versions.compileSdkVersion.get().toInt()
    // NDK build of the BigBlueBetterAudio noise-suppression core (RNNoise +
    // Faust chain) + JNI bridge -> libbbba.so. Sources live in the sibling
    // BigBlueBetterAudio repo (override the location with -DBBBA_DIR=).
    ndkVersion = "25.1.8937393"
    defaultConfig {
        minSdk = additionals.versions.minSdkVersion.get().toInt()
        externalNativeBuild {
            cmake {
                abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86_64")
            }
        }
    }
    externalNativeBuild {
        cmake {
            path = file("src/androidMain/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
    compileOptions {
        sourceCompatibility = rootProject.ext["javaVersionObject"] as JavaVersion
        targetCompatibility = rootProject.ext["javaVersionObject"] as JavaVersion
    }
}

buildkonfig {
    packageName = "${rootProject.ext["namespace"]}.participants"

    defaultConfigs {
        buildConfigField(
            FieldSpec.Type.STRING,
            "VERSION",
            rootProject.ext["version"].toString(),
            nullable = false,
            const = true
        )
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.targets.native.tasks.PodInstallSyntheticTask>()
    .configureEach {
        doLast {
            val xcodeprojFiles = listOf(
                "Pods/Pods.xcodeproj",
                "synthetic.xcodeproj",
            )

            for (xcodeprojFile in xcodeprojFiles) {
                val file =
                    project.buildDir.resolve("cocoapods/synthetic/ios/$xcodeprojFile/project.pbxproj")
                setIosDeploymentTarget(file)
            }
        }
    }

fun setIosDeploymentTarget(
    xcodeprojFile: File,
    target: String = "14.1",
) {
    if (!xcodeprojFile.exists()) {
        return
    }

    val lines = xcodeprojFile.readLines()
    val out = xcodeprojFile.bufferedWriter()
    out.use {
        for (line in lines) {
            out.write(
                line.replace(
                    "IPHONEOS_DEPLOYMENT_TARGET = ",
                    "IPHONEOS_DEPLOYMENT_TARGET = $target; // "
                )
            )
            out.write(("\n"))
        }
    }
}
