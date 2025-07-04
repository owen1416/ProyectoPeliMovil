package com.example.proyectopelis.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectopelis.Models.Generos
import com.example.proyectopelis.R


// Define una interfaz para manejar los clics de los ítems y acciones


class GenerosAdapter(
    private var generos: List<Generos>, // La lista de géneros a mostrar
    private val listener: OnGeneroActionListener // El listener para los clics
) : RecyclerView.Adapter<GenerosAdapter.GeneroViewHolder>() {

    // ViewHolder que mantiene las referencias a las vistas de un solo ítem
    class GeneroViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombre: TextView = itemView.findViewById(R.id.tv_genero_nombre)
        val ivEdit: ImageView = itemView.findViewById(R.id.iv_edit_genero)
        val ivDelete: ImageView = itemView.findViewById(R.id.iv_delete_genero)
    }

    // Crea nuevos ViewHolders (invocado por el layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GeneroViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_genero, parent, false)
        return GeneroViewHolder(view)
    }

    // Reemplaza el contenido de una vista (invocado por el layout manager)
    override fun onBindViewHolder(holder: GeneroViewHolder, position: Int) {
        val genero = generos[position]
        holder.tvNombre.text = genero.nombre

        holder.ivEdit.setOnClickListener { listener.onEditClick(genero) }
        holder.ivDelete.setOnClickListener { listener.onDeleteClick(genero) }
    }

    // Retorna el tamaño de tu dataset (invocado por el layout manager)
    override fun getItemCount(): Int = generos.size

    // Método para actualizar la lista de géneros y notificar al adaptador
    fun updateGeneros(newGeneros: List<Generos>) {
        this.generos = newGeneros
        notifyDataSetChanged() // Notifica al RecyclerView que los datos han cambiado
    }
}