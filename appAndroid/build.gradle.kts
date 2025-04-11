import com.google.firebase.appdistribution.gradle.firebaseAppDistribution

plugins {
    alias(additionals.plugins.android.application)
    alias(additionals.plugins.kotlin.android)
    alias(additionals.plugins.compose.compiler)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.appDistribution)
    id("jvmCompat")
}

android {
    namespace = "io.vopenia.app"
    defaultConfig {
        versionCode = 1
        versionName = "1.0"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    signingConfigs {
        val keyFile = File(project.projectDir, "${rootProject.ext["storeFile"]}")
        if (keyFile.exists()) {
            create("release") {
                storeFile = keyFile
                storePassword = "${rootProject.ext["storePassword"]}"
                keyAlias = "${rootProject.ext["keyAlias"]}"
                keyPassword = "${rootProject.ext["keyPassword"]}"
            }
        } else {
            println("Skipping signing : ${keyFile.absolutePath} doesn't exist")
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            val firebaseDistrib = File(project.projectDir, "vopenia-service-crendentials.json")

            if (firebaseDistrib.exists()) {
                firebaseAppDistribution {
                    appId = "${rootProject.ext["appDistributionId"]}"
                    serviceCredentialsFile = firebaseDistrib.absolutePath

                    artifactPath = File(
                        project.buildDir,
                        "outputs/apk/release/appAndroid-release.apk"
                    ).absolutePath
                    artifactType = "APK"
                    releaseNotesFile = File(
                        rootProject.projectDir,
                        "changelogs/${rootProject.ext["originalVersion"]}.txt"
                    ).absolutePath
                    groups = "internal"
                }
            }
            signingConfig = signingConfigs.getByName("release")
            //proguardFiles.add(getDefaultProguardFile("proguard-android.txt"))
        }
    }
}

dependencies {
    implementation(projects.shared)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.androidx.activity.compose)
    debugImplementation(libs.compose.ui.tooling)

    // TODO : clean this
    implementation(platform("com.google.firebase:firebase-bom:33.1.1"))
    implementation("com.google.firebase:firebase-analytics")
}