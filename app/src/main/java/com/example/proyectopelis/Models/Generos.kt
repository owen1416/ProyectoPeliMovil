package com.example.proyectopelis.Models

import com.google.firebase.firestore.DocumentId

data class Generos(
    @DocumentId
    var id: String? = null,
    var nombre: String = ""
)
