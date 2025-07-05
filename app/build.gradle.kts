plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
    id("kotlin-parcelize") // Necesario para Parcelable

}

android {
    namespace = "com.example.proyectopelis"
    compileSdk = 35 // Manteniendo tu versión de compileSdk

    defaultConfig {
        applicationId = "com.example.proyectopelis"
        minSdk = 24
        targetSdk = 35 // Manteniendo tu versión de targetSdk
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
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
        sourceCompatibility = JavaVersion.VERSION_11 // Manteniendo tu versión de Java
        targetCompatibility = JavaVersion.VERSION_11 // Manteniendo tu versión de Java
    }
    kotlinOptions {
        jvmTarget = "11" // Manteniendo tu versión de JVM Target
    }
}

dependencies {

    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    // Core AndroidX Libraries (preferimos los aliases de libs.versions.toml si están definidos y actualizados)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material) // Asumiendo que libs.material es 1.11.0 o más reciente
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Firebase Platform (BOM) - ¡IMPORTANTE para versiones compatibles!
    // Usamos la BOM para gestionar las versiones de Firebase de forma consistente.
    implementation(platform("com.google.firebase:firebase-bom:33.1.0")) // Usamos la última BOM de la conversación anterior

    // Firebase Authentication (para login/registro)
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.android.gms:play-services-auth:21.2.0") // Para Google Sign-In

    // Firebase Firestore
    implementation("com.google.firebase:firebase-firestore-ktx") // Versión KTX para Kotlin

    // Firebase Storage (para subir imágenes, si lo usas)
    implementation("com.google.firebase:firebase-storage-ktx") // <--- AÑADIDO: Firebase Storage

    // Firebase Analytics (opcional, si lo necesitas para seguimiento)
    implementation("com.google.firebase:firebase-analytics-ktx")

    // Glide para cargar imágenes
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // MPAndroidChart para gráficas
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0") // <--- AÑADIDO: MPAndroidChart

    // Dependencias para Retrofit (para llamadas a API externas)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // Dependencias para Corrutinas (útil para manejar las llamadas de red asíncronas)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2") // Para ViewModelScope si usas ViewModels

    // Testing Libraries
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
