import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}


android {
    namespace = "com.example.app_usage_limit"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.app_usage_limit"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        // 🔹 OPENAI_API_KEY
        val localProperties = Properties().apply {
            val file = rootProject.file("local.properties")
            if (file.exists()) {
                load(file.inputStream())
            }
        }
        val openAiKey = localProperties.getProperty("OPENAI_API_KEY") ?: ""
        println("✅ Loaded OPENAI_API_KEY: $openAiKey")

        buildConfigField("String", "OPENAI_API_KEY", "\"$openAiKey\"")

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

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        buildConfig = true // 꼭 있어야 BuildConfig.OPENAI_API_KEY 사용 가능
    }
}

dependencies {

    // AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation (libs.androidx.appcompat.v171)
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation(libs.androidx.lifecycle.runtime.ktx.v261)
    implementation(libs.androidx.activity.ktx)
    implementation("androidx.core:core-ktx:1.12.0")

    // Jetpack Compose
    implementation(platform(libs.androidx.compose.bom.v20230501))
    implementation(libs.androidx.activity.compose.v172)
    implementation("androidx.activity:activity-ktx:1.9.0")
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material3)
    implementation("io.coil-kt:coil-compose:2.2.2")

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // 네트워크 및 JSON 처리
    implementation(libs.okhttp.v4120)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.gson)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.recyclerview)


    // 테스트
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit.v115)
    androidTestImplementation(libs.androidx.espresso.core.v351)
    androidTestImplementation(platform(libs.androidx.compose.bom.v20250501))
    androidTestImplementation(libs.ui.test.junit4)

    // 디버깅
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
}
