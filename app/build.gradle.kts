plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.NINA.ninasuawebnamofiscalesquentadinhabr"
    compileSdk = 36 // Atualizado para 36 conforme exigido pelas dependências do AndroidX

    defaultConfig {
        applicationId = "com.NINA.ninasuawebnamofiscalesquentadinhabr"
        minSdk = 26 // Aumentado para 26 para suportar Adaptive Icons e melhor performance de IA
        targetSdk = 36 // Sincronizado com o compileSdk
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

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        jniLibs {
            // Importante para MediaPipe GenAI: garantir que as libs nativas sejam carregadas
            useLegacyPackaging = true
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // ML Kit
    implementation("com.google.mlkit:face-detection:16.1.7")
    implementation("com.google.mlkit:image-labeling:17.0.9")

    // MediaPipe GenAI - O CORAÇÃO DA NINA (Gemma 3 Offline)
    // Versão 0.10.14 é a recomendada para Gemma 270M
    implementation("com.google.mediapipe:tasks-genai:0.10.14")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
