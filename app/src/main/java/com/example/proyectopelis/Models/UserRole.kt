package com.example.proyectopelis.Models

data class UserRole(
    val uid: String = "",
    val username: String = "",
    val email: String = "", // Útil para mostrar el email del usuario
    val role: String = "user"
)
