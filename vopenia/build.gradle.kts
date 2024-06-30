import com.codingfeline.buildkonfig.compiler.FieldSpec

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinCocoapods)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.spm)
    alias(additionals.plugins.multiplatform.buildkonfig)
    id("jvmCompat")
    id("iosSimulatorConfiguration")
}

kotlin {
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries {
            framework {
                baseName = "KotlinLibrary"
            }
        }
    }

    cocoapods {
        summary = "Some description for the Shared Module"
        homepage = "Link to the Shared Module homepage"
        version = "1.0"
        ios.deploymentTarget = "16.0"
        osx.deploymentTarget = "16.0"
        framework {
            baseName = "vopenia"
            isStatic = true
        }


        pod("LiveKitClient") {
            version = "2.0.9"
            source = path(rootProject.file("../LiveKitClient"))
            moduleName = "LiveKitClient"
            packageName = "LiveKitClient"
            extraOpts += listOf("-compiler-option", "-fmodules")
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        androidMain.dependencies {
            implementation(libs.livekit.android)
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
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