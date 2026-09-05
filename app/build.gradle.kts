plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.kotlinkyber"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.kotlinkyber"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters.addAll(listOf("arm64-v8a", "x86_64"))
        }
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/mlkem-native/CMakeLists.txt")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
