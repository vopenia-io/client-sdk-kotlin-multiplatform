@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import com.codingfeline.buildkonfig.compiler.FieldSpec
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    alias(additionals.plugins.kotlin.multiplatform)
    alias(additionals.plugins.kotlin.cocoapods)
    alias(additionals.plugins.android.library)
    alias(additionals.plugins.multiplatform.buildkonfig)
    alias(additionals.plugins.jetbrains.compose)
    alias(additionals.plugins.kotlin.serialization)
    alias(additionals.plugins.compose.compiler)
    id("jvmCompat")
}

val sampleNamespaceShared = rootProject.ext["sampleNamespaceShared"] as String

kotlin {
    androidTarget()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    jvm()

    cocoapods {
        summary = "Some description for the Shared Module"
        homepage = "Link to the Shared Module homepage"
        version = "1.0"
        specRepos {
            url("https://github.com/livekit/podspecs")
            url("https://github.com/vopenia-io/pod-repo")
        }
        ios.deploymentTarget = "16.0"
        podfile = project.file("../appIos/Podfile")
        framework {
            baseName = "shared"
            isStatic = false
            // transitiveExport = true
            //linkerOpts("-ld_classic")
        }

        // Transitive link-only dependencies: shared.framework inherits cinterop
        // bindings from :vopenia, so the LiveKit frameworks must be on the
        // linker search path at framework-link time. linkOnly skips binding
        // regeneration (requires a dynamic framework — see isStatic above).
        pod("LiveKitClient") {
            version = "2.6.0"
            linkOnly = true
            extraOpts += listOf("-compiler-option", "-fmodules")
        }

        // BBBACore must be declared everywhere LiveKitClientKotlin is —
        // its podspec depends on BBBACore but it lives outside any podspec
        // repo (local sibling checkout in `../BigBlueBetterAudio`).
        pod("BBBACore") {
            version = "1.0.0"
            source = path(rootProject.file("../BigBlueBetterAudio"))
            linkOnly = true
            extraOpts += listOf("-compiler-option", "-fmodules")
        }

        pod("LiveKitClientKotlin") {
            version = "2.6.0"
            linkOnly = true
            extraOpts += listOf("-compiler-option", "-fmodules")
        }

        // The kotlin-sentry klib on the classpath still embeds a cinterop
        // reference to Sentry.framework even though runtime calls are
        // commented out, so we need it on the linker search path.
        pod("Sentry") {
            linkOnly = true
            extraOpts += listOf("-compiler-option", "-fmodules")
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)
            implementation(compose.material)
            implementation(compose.material3)
            @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
            implementation(compose.components.resources)
            implementation(compose.materialIconsExtended)

            api(additionals.kotlinx.serialization.json)

            api("moe.tlaster:precompose:1.7.0-alpha03")
            api(additionals.multiplatform.safearea)
            api(additionals.multiplatform.widgets.compose)
            api(additionals.multiplatform.permissions)
            api(additionals.multiplatform.platform)
            api(additionals.multiplatform.http.client)
            api(additionals.multiplatform.viewmodel)
            api(additionals.multiplatform.file.access)
            api(additionals.multiplatform.sentry)

            api(additionals.hotpreview)

            api(projects.vopenia)
            api(projects.vopeniaCompose)
            implementation(projects.vopeniaUtils)
        }
        commonTest.dependencies {
            implementation(additionals.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
        }
    }
}

android {
    namespace = sampleNamespaceShared
}

compose.resources {
    publicResClass = true
    packageOfResClass = "$sampleNamespaceShared.res"
    generateResClass = always
}

buildkonfig {
    packageName = "$sampleNamespaceShared.config"

    defaultConfigs {
        listOf(
            "ENDPOINT_TOKEN" to rootProject.ext["sampleAppTokenEndpoint"] as String,
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

kotlin {
    // we disabled Sentry but this can still be used due to another cinterops issue
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().all {
        compilations["main"].cinterops.let { cinterops ->
            if (cinterops.any { it.name == "Sentry" }) {
                cinterops["Sentry"].extraOpts(
                    "-compiler-option",
                    "-DSentryMechanismMeta=SentryMechanismMetaUnavailable"
                )
            }
        }
    }
}
