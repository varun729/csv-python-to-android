plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.chaquo.python") version "15.0.1"
}

android {
    namespace = "net.vagrawal.android.csv_python_to_android"
    // Use standard Kotlin DSL for compileSdk
    compileSdk = 36

    defaultConfig {
        applicationId = "net.vagrawal.android.csv_python_to_android"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Required by Chaquopy: specify the ABIs to build for
        ndk {
            abiFilters += listOf("arm64-v8a", "x86_64")
        }
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        viewBinding = true
    }
}

// Chaquopy configuration (Kotlin DSL)
chaquopy {
    defaultConfig {
        // REQUIRED: Path to your local Python 3.11 executable (project policy: no managed Python)
        // Adjust this path for your system if different.
        buildPython("/opt/homebrew/bin/python3.11")

        // Android embedded runtime must match the same minor version.
        version = "3.11"

        pip {
            install("numpy")
            install("pandas")
            install("matplotlib")
            install("pillow")
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}