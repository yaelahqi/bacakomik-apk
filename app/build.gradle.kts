plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "id.pina.bacakomik"
    compileSdk = 34

    defaultConfig {
        applicationId = "id.pina.bacakomik"
        minSdk = 24
        targetSdk = 33
        versionCode = 23
        versionName = "2.1.3"
    }

    signingConfigs {
        create("pina") {
            storeFile = file("pinakomik-debug.keystore")
            storePassword = "pinakomik123"
            keyAlias = "pinakomik"
            keyPassword = "pinakomik123"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("pina")
        }
        debug {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("pina")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.09.00")
    implementation(composeBom)

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.8.0")
    implementation("io.coil-kt:coil-compose:2.6.0")
}
