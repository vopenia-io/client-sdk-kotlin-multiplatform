import com.google.firebase.appdistribution.gradle.firebaseAppDistribution

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.appDistribution)
}

android {
    namespace = "eu.codlab.vopenia"
    compileSdk = 34
    defaultConfig {
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = additionals.versions.androidx.compose.compiler.get()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    signingConfigs {
        create("release") {
            storeFile = File(project.projectDir, "${rootProject.ext["storeFile"]}")
            storePassword = "${rootProject.ext["storePassword"]}"
            keyAlias = "${rootProject.ext["keyAlias"]}"
            keyPassword = "${rootProject.ext["keyPassword"]}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            firebaseAppDistribution {
                appId = "${rootProject.ext["appDistributionId"]}"
                serviceCredentialsFile = File(
                    project.projectDir, "vopenia-service-crendentials.json"
                ).absolutePath

                artifactPath = File(
                    project.buildDir,
                    "outputs/apk/release/appAndroid-release.apk"
                ).absolutePath
                artifactType = "APK"
                releaseNotesFile = File(
                    rootProject.projectDir, "changelogs/${rootProject.ext["originalVersion"]}.txt"
                ).absolutePath
                groups = "internal"
            }
            signingConfig = signingConfigs.getByName("release")
            //proguardFiles.add(getDefaultProguardFile("proguard-android.txt"))
        }
    }
    compileOptions {
        sourceCompatibility = rootProject.ext["javaVersionObject"] as JavaVersion
        targetCompatibility = rootProject.ext["javaVersionObject"] as JavaVersion
    }
    kotlinOptions {
        jvmTarget = rootProject.ext["javaVersion"] as String
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