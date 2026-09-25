plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "th.go.banlat.kiosk"
    compileSdk = 36

    defaultConfig {
        applicationId = "th.go.banlat.kiosk"
        // ตู้จริง Android 7.1.2 (rk3288) · เงาเบลอบน Android < 9 ใช้ SoftBlur (เรนเดอร์ลงบิตแมปแล้วแคช) ภาพจึงเหมือนกัน
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0-prototype"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    signingConfigs {
        getByName("debug") { enableV1Signing = true; enableV2Signing = true }   // v1 ให้ติดตั้งบนเครื่องเก่าได้แน่นอน
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true   // java.time (นาฬิกา / อายุ / วันที่บนบัตรคิว) บน Android 7
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2025.05.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.core:core-ktx:1.16.0")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
}
