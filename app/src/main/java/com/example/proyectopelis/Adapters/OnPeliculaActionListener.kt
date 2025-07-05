package com.example.proyectopelis.Adapters

import com.example.proyectopelis.Models.Peliculas

interface OnPeliculaActionListener {
    fun onEditClick(pelicula: Peliculas)
    fun onDeleteClick(pelicula: Peliculas)
    fun onPeliculaClick(pelicula: Peliculas, generoNombre: String?)
    fun onFavoriteClick(pelicula: Peliculas, isFavorite: Boolean) // ¡NUEVO!
}