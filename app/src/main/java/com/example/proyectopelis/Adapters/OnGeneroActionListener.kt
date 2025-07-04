package com.example.proyectopelis.Adapters

import com.example.proyectopelis.Models.Generos

interface OnGeneroActionListener {
    fun onEditClick(genero: Generos)
    fun onDeleteClick(genero: Generos)
}