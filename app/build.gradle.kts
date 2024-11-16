plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.lockedin"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.lockedin"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.runtime.livedata)
    implementation(libs.firebase.storage.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Coil (latest version only)
    implementation("io.coil-kt.coil3:coil-compose:3.0.3")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.0.3")

    // Firebase BOM
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage")

    // Jetpack Compose 
    implementation("androidx.compose.material:material:1.7.4")
    implementation("androidx.compose.ui:ui:1.7.4")
    implementation(libs.androidx.navigation.compose.v240)
}

// dependencies {

//     implementation(libs.androidx.core.ktx)
//     implementation(libs.androidx.lifecycle.runtime.ktx)
//     implementation(libs.androidx.activity.compose)
//     implementation(platform(libs.androidx.compose.bom))
//     implementation(libs.androidx.ui)
//     implementation(libs.androidx.ui.graphics)
//     implementation(libs.androidx.ui.tooling.preview)
//     implementation(libs.androidx.material3)
//     implementation(libs.androidx.navigation.compose)
//     implementation(libs.androidx.runtime.livedata)
//     implementation(libs.firebase.storage.ktx)
//     testImplementation(libs.junit)
//     androidTestImplementation(libs.androidx.junit)
//     androidTestImplementation(libs.androidx.espresso.core)
//     androidTestImplementation(platform(libs.androidx.compose.bom))
//     androidTestImplementation(libs.androidx.ui.test.junit4)
//     debugImplementation(libs.androidx.ui.tooling)
//     debugImplementation(libs.androidx.ui.test.manifest)
//     implementation("io.coil-kt.coil3:coil-compose:3.0.2")
//     implementation("io.coil-kt.coil3:coil-network-okhttp:3.0.2")
//     implementation(platform("com.google.firebase:firebase-bom:33.4.0"))
//     implementation("com.google.firebase:firebase-analytics")
//     implementation("com.google.firebase:firebase-auth")
//     implementation ("com.google.firebase:firebase-firestore-ktx")


//     implementation("androidx.compose.material:material:1.7.4")
//     implementation("androidx.compose.ui:ui:1.7.4")
//     implementation(libs.androidx.navigation.compose.v240)

//     implementation("io.coil-kt.coil3:coil-compose:3.0.3")
//     implementation("io.coil-kt.coil3:coil-network-okhttp:3.0.3")

//     implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
//     implementation("com.google.firebase:firebase-storage")

// }