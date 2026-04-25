import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

android {
    namespace = libs.versions.appId.get()
    compileSdk = libs.versions.compileSdk.get().toInt()

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    defaultConfig {
        applicationId = libs.versions.appId.get()
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = libs.versions.versionCode.get().toInt()
        versionName = libs.versions.versionName.get()

        base.archivesName =  "ProjectivyPlugin-Overflight-$versionName-c$versionCode"
    }

    signingConfigs {
        create("genericRelease") {
            keyAlias = gradleLocalProperties(rootDir, providers).getProperty("GENERIC_KEY_ALIAS")
            keyPassword = gradleLocalProperties(rootDir, providers).getProperty("GENERIC_KEY_PASSWORD")
            storeFile = file(gradleLocalProperties(rootDir, providers).getProperty("GENERIC_STORE_FILE"))
            storePassword = gradleLocalProperties(rootDir, providers).getProperty("GENERIC_STORE_PASSWORD")
        }
        create("playstoreRelease") {
            keyAlias = gradleLocalProperties(rootDir, providers).getProperty("PLAYSTORE_KEY_ALIAS")
            keyPassword = gradleLocalProperties(rootDir, providers).getProperty("PLAYSTORE_KEY_PASSWORD")
            storeFile = file(gradleLocalProperties(rootDir, providers).getProperty("PLAYSTORE_STORE_FILE"))
            storePassword = gradleLocalProperties(rootDir, providers).getProperty("PLAYSTORE_STORE_PASSWORD")
        }
    }

    buildFeatures {
        buildConfig = true
    }

    flavorDimensions.add("store")
    productFlavors {
        create("playstore") {
            dimension = "store"
        }
        create("generic") {
            dimension = "store"
        }
    }
    productFlavors.configureEach {
        signingConfig = if (name.contains("playstore")) {
            signingConfigs.getByName("playstoreRelease")
        } else {
            signingConfigs.getByName("genericRelease")
        }
    }

    buildTypes {
        getByName("debug") {
            versionNameSuffix = "-dev"
        }
        getByName("release") {
            isShrinkResources = true
            isMinifyEnabled = true
            isDebuggable = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

}

dependencies {
    implementation(libs.androidx.core)
    implementation(libs.androidx.leanback)
    implementation(libs.androidx.appcompat)
    implementation(libs.google.material)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.kotlinx.serialization.json)

    implementation(platform(libs.okhttp.bom))
    implementation(libs.okhttp)

    implementation(libs.bundles.kotlinx.coroutines)

    implementation(project(":api"))
}