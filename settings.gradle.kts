pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") } // ¡Sintaxis correcta para Kotlin DSL!
    }
}

rootProject.name = "ProyectoPelis" // Asegúrate de que este sea el nombre de tu proyecto
include(":app")
