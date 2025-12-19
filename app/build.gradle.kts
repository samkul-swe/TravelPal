plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
}

android {
    namespace = "org.kulkarni_sampada.travelpal"
    compileSdk = 36

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    defaultConfig {
        applicationId = "org.kulkarni_sampada.travelpal"
        minSdk = 27
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    packaging {
        resources {
            excludes += "META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/LICENSE"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/license.txt"
            excludes += "META-INF/NOTICE"
            excludes += "META-INF/NOTICE.txt"
            excludes += "META-INF/notice.txt"
            excludes += "META-INF/ASL2.0"
            excludes += "META-INF/*.kotlin_module"
            excludes += "META-INF/versions/9/OSGI-INF/MANIFEST.MF"
        }
    }
}

dependencies {
    // AndroidX Core
    implementation(libs.core)
    implementation(libs.coordinatorlayout)
    implementation(libs.cardview)
    implementation(libs.recyclerview)

    // Lifecycle Components (ViewModel, LiveData)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)
    implementation(libs.lifecycle.runtime)
    implementation(libs.lifecycle.extensions)

    // Firebase BOM (Bill of Materials)
    implementation(platform(libs.firebase.bom.v3270))
    implementation(libs.google.firebase.auth)
    implementation(libs.google.firebase.database)
    implementation(libs.firebase.analytics)

    // Gemini AI (Google Generative AI)
    implementation(libs.generativeai.v012)
    implementation(libs.guava.v3350android)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)

    // Networking - Volley for HTTP requests
    implementation(libs.volley)

    // Google Play Services - Maps and Location
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location.v2110)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.junit.v115)
    androidTestImplementation(libs.espresso.core.v351)



    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.espresso.web)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.retrofit2.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
    implementation(libs.play.services.location)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.database)
    implementation(libs.picasso)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.auth)
    implementation(libs.generativeai)
    implementation(libs.material.v130alpha02)
    implementation(libs.guava)
    implementation(libs.play.services.auth)
    implementation(libs.timelineview)
}