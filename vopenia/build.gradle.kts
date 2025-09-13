import com.codingfeline.buildkonfig.compiler.FieldSpec

plugins {
    alias(additionals.plugins.kotlin.multiplatform)
    alias(additionals.plugins.kotlin.cocoapods)
    alias(additionals.plugins.android.library)
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
            }
        }
    }

    cocoapods {
        summary = "Some description for the Shared Module"
        homepage = "Link to the Shared Module homepage"
        version = "1.0"
        specRepos {
            url("https://github.com/livekit/podspecs")
            url("https://github.com/vopenia-io/pod-repo")
        }
        ios.deploymentTarget = "16.0"
        osx.deploymentTarget = "16.0"
        framework {
            baseName = "vopenia"
            //isStatic = false
            //transitiveExport = true
        }

        pod("LiveKitClientKotlin") {
            version = "2.6.0"
            moduleName = "LiveKitClientKotlin"
            packageName = "LiveKitClientKotlin"
            extraOpts += listOf("-compiler-option", "-fmodules")
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(additionals.kotlinx.coroutines)
            implementation(additionals.multiplatform.permissions)
            implementation(additionals.multiplatform.permissions.bluetooth)
            implementation(additionals.multiplatform.permissions.camera)
            implementation(additionals.multiplatform.permissions.microphone)
            api(projects.vopeniaParticipants)
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
        }
        jvmMain.dependencies {
            implementation(additionals.kotlinx.coroutines.jvm)
        }
    }
}

android {
    namespace = rootProject.ext["namespace"] as String
    compileSdk = additionals.versions.compileSdkVersion.get().toInt()
    defaultConfig {
        minSdk = additionals.versions.minSdkVersion.get().toInt()
    }
    compileOptions {
        sourceCompatibility = rootProject.ext["javaVersionObject"] as JavaVersion
        targetCompatibility = rootProject.ext["javaVersionObject"] as JavaVersion
    }
}

buildkonfig {
    packageName = rootProject.ext["namespace"] as String

    defaultConfigs {
        buildConfigField(
            FieldSpec.Type.STRING,
            "VERSION",
            rootProject.ext["version"] as String,
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
