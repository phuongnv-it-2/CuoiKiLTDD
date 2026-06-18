plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.project24itb156.gglens"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.project24itb156.gglens"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        buildConfigField("String", "GEMINI_API_KEY", "\"AIzaSyCxT3TbKtgWtKYZspBNy6LrUCOORRrnwYA\"")
        buildConfigField("String", "CLOUD_VISION_API_KEY", "\"AIzaSyDiETcFmSzux8x6-R3gz_5Jj280uuc5Cms\"")
        buildConfigField("String", "SEARCH_CX", "\"20c8da01af36c538ae41d96e86ef5bebce6f1db10b37deaccc155179cb98d325\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    aaptOptions {
        noCompress += "tflite"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    
    // Use Compose BOM to manage versions
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    
    // Explicitly add these to ensure version 1.7.0+ is used for FlowRow
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.foundation:foundation-layout")
    implementation("androidx.compose.material:material-icons-extended")

    // Navigation and Lifecycle must be updated for 1.7.0 compatibility
    implementation("androidx.navigation:navigation-compose:2.8.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2")

    // Auth & Persistence
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // CameraX
    implementation("androidx.camera:camera-core:1.3.4")
    implementation("androidx.camera:camera-camera2:1.3.4")
    implementation("androidx.camera:camera-lifecycle:1.3.4")
    implementation("androidx.camera:camera-view:1.3.4")

    // ML Kit
    implementation("com.google.mlkit:text-recognition:16.0.1")
    implementation("com.google.mlkit:object-detection:17.0.2")
    implementation("com.google.mlkit:image-labeling:17.0.7")
    implementation("com.google.mlkit:translate:17.0.2")

    // Network
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")

    // Permissions & Coil
    implementation("com.google.accompanist:accompanist-permissions:0.36.0")
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Gemini
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
