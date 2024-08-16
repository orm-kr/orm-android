import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("com.google.gms.google-services")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    id("kotlin-parcelize")
    id("kotlin-kapt")
}

fun getApiKey(key: String): String = gradleLocalProperties(rootDir, providers).getProperty(key)

android {
    namespace = "com.orm"
    compileSdk = 34

    buildFeatures {
        viewBinding = true
        dataBinding = true
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.orm"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ndk { abiFilters += listOf("arm64-v8a", "armeabi-v7a") }

        buildConfigField("String", "BASE_URL", getApiKey("BASE_URL"))
        buildConfigField("String", "DATASTORE_NAME", getApiKey("DATASTORE_NAME"))
        buildConfigField("String", "KAKAO_APP_KEY", getApiKey("KAKAO_APP_KEY"))
        buildConfigField("String", "WEATHER_API_KEY", getApiKey("WEATHER_API_KEY"))
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.firebase.messaging.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.android)
    implementation(libs.play.services.location)
    implementation(libs.play.services.pal)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // retrofit2
    implementation(libs.retrofit)
    implementation(libs.converter.gson)

    // coroutines
    implementation(libs.kotlinx.coroutines.android)

    // room
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)

    // navigation
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // splash
    implementation(libs.androidx.core.splashscreen)

    // firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)

    // glide
    implementation(libs.glide)
    ksp(libs.compiler)

    // kakao login
    implementation(libs.v2.user)

    // datastore
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.datastore.preferences.core)

    // kakao map
    implementation("com.kakao.maps.open:android:2.9.5")

    // google map
    implementation(libs.play.services.maps)
    implementation (libs.play.services.location.v2101)
    implementation(libs.android.maps.utils)

    // okhttp
    implementation (libs.okhttp)
    implementation (libs.logging.interceptor)

    // graph
    implementation(libs.mpandroidchart)

    // worker
    implementation(libs.androidx.work.runtime.ktx)

    // AI model
    implementation(libs.tensorflow.lite)
    implementation(libs.tensorflow.lite.support)

    //board
    implementation(libs.wasabeef.richeditor.android)
    implementation(libs.androidx.swiperefreshlayout)

    // gesture view
    implementation(libs.gesture.views)
}