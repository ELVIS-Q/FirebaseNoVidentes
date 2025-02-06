plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.googleService)
}

android {
    namespace = "com.personal.firebaseapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.personal.firebaseapp"
        minSdk = 24
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation (libs.firebase.analytics)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebaseAuth)
    implementation(libs.firebase.database)
    implementation (libs.firebase.messaging)
    implementation (libs.play.services.maps)
}