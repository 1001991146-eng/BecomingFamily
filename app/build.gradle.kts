import java.util.Properties // חובה להוסיף את הייבוא הזה בהתחלה!

plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.becomingfamily"
    compileSdk = 36 // מומלץ להישאר על 34 או 35 (36 עדיין נסיוני)

    defaultConfig {
        applicationId = "com.example.becomingfamily"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // --- קוד מתוקן ל-Kotlin DSL ---
        val properties = Properties()
        val localPropertiesFile = project.rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            properties.load(localPropertiesFile.inputStream())
        }

        // שליפת המפתח בצורה בטוחה
        val apiKey = properties.getProperty("apiKey") ?: ""
        buildConfigField("String", "GEMINI_API_KEY", "\"$apiKey\"")
        // ------------------------------
    }

    // הבלוק הזה יושב במקום הנכון עכשיו!
    buildFeatures {
        buildConfig = true
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