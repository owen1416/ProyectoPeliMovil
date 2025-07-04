        // Top-level build file where you can add configuration options common to all sub-projects/modules.

        buildscript {
            repositories {
                google() // <--- MUY IMPORTANTE QUE ESTÉ AQUÍ TAMBIÉN
                mavenCentral()

            }
            dependencies {
                // Asegúrate de que tu versión de Android Gradle Plugin es la correcta
                // classpath("com.android.tools.build:gradle:8.2.0") // Ejemplo, usa tu versión
                classpath("com.android.tools.build:gradle:8.11.0")
            }
        }
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    id("com.google.gms.google-services") version "4.4.1" apply false // <<< Asegúrate que está aquí
}