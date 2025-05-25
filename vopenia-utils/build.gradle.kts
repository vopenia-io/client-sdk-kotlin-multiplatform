import com.codingfeline.buildkonfig.compiler.FieldSpec

plugins {
    alias(additionals.plugins.kotlin.multiplatform)
    alias(additionals.plugins.android.library)
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

    sourceSets {
        commonMain.dependencies {
            implementation(additionals.kotlinx.coroutines)
        }
        commonTest.dependencies {
            implementation(additionals.kotlinx.coroutines.test)
        }
        jvmMain.dependencies {
            implementation(additionals.kotlinx.coroutines.jvm)
        }
    }
}

android {
    namespace = "${rootProject.ext["namespace"]}.utils"
}
