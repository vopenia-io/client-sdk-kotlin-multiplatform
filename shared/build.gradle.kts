import com.codingfeline.buildkonfig.compiler.FieldSpec

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinCocoapods)
    alias(libs.plugins.androidLibrary)
    alias(additionals.plugins.multiplatform.buildkonfig)
    alias(additionals.plugins.jetbrains.compose)
    alias(additionals.plugins.kotlin.serialization)
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

            implementation(projects.vopenia)
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
    compileSdk = 34
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = rootProject.ext["javaVersionObject"] as JavaVersion
        targetCompatibility = rootProject.ext["javaVersionObject"] as JavaVersion
    }
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

//tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
//    kotlinOptions {
//        jvmTarget = rootProject.ext["javaVersion"] as String
//    }
//}
