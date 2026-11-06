plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.peoplememory.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.peoplememory.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 3
        versionName = "2.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // Coil for Android Contacts Provider Photo Loading
    implementation(libs.coil.compose)

    // Google LiteRT-LM for On-Device LLM Inference (Gemma 1B/2B)
    implementation(libs.litert.lm)

    // Firebase BOM, Authentication, and Firestore
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.play.services.auth)

    // Jetpack Compose & Material 3
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
}