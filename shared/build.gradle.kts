import com.codingfeline.buildkonfig.compiler.FieldSpec

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

val sampleAppNamespace = rootProject.ext["sampleAppNamespace"] as String

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
        ios.deploymentTarget = "16.0"
        podfile = project.file("../appIos/Podfile")
        framework {
            baseName = "shared"
            isStatic = true
            transitiveExport = true
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

            implementation(libs.moko.viewmodel)
            implementation(libs.moko.viewmodel.compose)

            api(additionals.multiplatform.precompose)
            api(additionals.multiplatform.safearea)
            api(additionals.multiplatform.widgets.compose)
            api(additionals.multiplatform.permissions)
            api(additionals.multiplatform.platform)
            api(additionals.multiplatform.http.client)
            api(additionals.multiplatform.viewmodel)
            api(additionals.multiplatform.file.access)

            api(additionals.hotpreview)

            api(projects.vopenia)
            api(projects.vopeniaCompose)
            implementation(projects.vopeniaUtils)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
        }
    }
}

android {
    namespace = sampleAppNamespace
}

compose.resources {
    publicResClass = true
    packageOfResClass = "$sampleAppNamespace.res"
    generateResClass = always
}

buildkonfig {
    packageName = "$sampleAppNamespace.config"

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
