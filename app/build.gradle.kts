plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")

}

android {
    namespace = "com.example.becomingfamily"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.becomingfamily"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        buildFeatures {
            buildConfig = true
        }

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String","GEMINI_API_KEY","\"AIzaSyCOCL7BC0VnET6Jd41TE9axrSOrj43unZA\"")

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
}

dependencies {
    implementation(libs.generativeai)
    implementation("com.google.guava:guava:33.0.0-jre")
    implementation(platform("com.google.firebase:firebase-bom:34.5.0"))
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}