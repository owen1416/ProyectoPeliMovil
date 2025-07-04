package com.example.proyectopelis.Adapters

import com.example.proyectopelis.Models.Peliculas

interface OnPeliculaActionListener {
    fun onEditClick(pelicula: Peliculas)
    fun onDeleteClick(pelicula: Peliculas)
    // Opcional: Si quieres un clic en toda la tarjeta para ver detalles, añade esto:
    fun onPeliculaClick(pelicula: Peliculas, generoNombre: String?) // Para ir a la pantalla de detalles (futuro)
}