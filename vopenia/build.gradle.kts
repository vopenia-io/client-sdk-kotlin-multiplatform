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
                transitiveExport = true
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
            transitiveExport = true
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
            implementation(additionals.multiplatform.permissions)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(project(":vopenia-test-config"))
        }
        androidMain.dependencies {
            implementation(libs.livekit.android)
            api(libs.androidx.fragment)
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

//tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
//    kotlinOptions {
//        jvmTarget = rootProject.ext["javaVersion"] as String
//    }
//}
