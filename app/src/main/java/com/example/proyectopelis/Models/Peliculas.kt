package com.example.proyectopelis.Models

import android.os.Parcelable
import com.google.firebase.firestore.DocumentId
import kotlinx.android.parcel.Parcelize



@Parcelize
data class Peliculas(
    @DocumentId
    var id: String? = null,
    var titulo: String = "",
    var descripcion: String = "",
    var anio: Int = 0, // Ya tienes el año, pero la fecha de lanzamiento es más específica
    var director: String = "",
    var duracionMinutos: Int = 0,
    var generoId: String? = null,
    var imageUrl: String? = null,
    var trailerUrl: String? = null,
    var fechaLanzamiento: String? = null, // ¡NUEVO CAMPO! Formato: YYYY-MM-DD
    var favoriteCount: Long = 0,
    var favoriteUsers: List<String>? = null


): Parcelable


