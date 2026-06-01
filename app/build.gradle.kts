plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "id.pina.bacakomik"
    compileSdk = 34

    defaultConfig {
        applicationId = "id.pina.bacakomik"
        minSdk = 24
        targetSdk = 33
        versionCode = 20
        versionName = "2.1.0"
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
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
}
