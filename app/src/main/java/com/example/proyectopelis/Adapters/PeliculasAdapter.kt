package com.example.proyectopelis.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.proyectopelis.Adapters.OnPeliculaActionListener
import com.example.proyectopelis.Models.Peliculas
import com.example.proyectopelis.R

class PeliculasAdapter(
    private var peliculas: List<Peliculas>,
    private val listener: OnPeliculaActionListener,
    private var generosMap: Map<String, String>
) : RecyclerView.Adapter<PeliculasAdapter.PeliculaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeliculaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pelicula, parent, false)
        return PeliculaViewHolder(view)
    }

    override fun onBindViewHolder(holder: PeliculaViewHolder, position: Int) {
        val pelicula = peliculas[position]

        holder.tvTitulo.text = pelicula.titulo
        holder.tvAnio.text = "Año: ${pelicula.anio}"

        val generoNombre = generosMap[pelicula.generoId] ?: "Género desconocido"
        holder.tvGenero.text = "Género: $generoNombre"

        holder.tvDirector.text = "Director: ${pelicula.director}"
        holder.tvDescripcion.text = pelicula.descripcion

        if (!pelicula.imageUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(pelicula.imageUrl)
                .placeholder(R.drawable.placeholder_poster)
                .into(holder.imgPeliculaItem)
        } else {
            holder.imgPeliculaItem.setImageResource(R.drawable.placeholder_poster)
        }

        if (!pelicula.fechaLanzamiento.isNullOrEmpty()) {
            holder.tvFechaLanzamiento.text = "Lanzamiento: ${pelicula.fechaLanzamiento}"
            holder.tvFechaLanzamiento.visibility = View.VISIBLE
        } else {
            holder.tvFechaLanzamiento.visibility = View.GONE
        }

        // --- CAMBIO AQUÍ: Usar Glide para cargar el icono de favorito ---
        val favoriteIconRes = if (pelicula.isFavorite) R.drawable.heart_solid else R.drawable.heart_regular
        Glide.with(holder.itemView.context)
            .load(favoriteIconRes)
            .into(holder.btnFavorite)
        // --- FIN DEL CAMBIO ---

        // Configurar listeners de los botones y del item completo
        holder.btnEditar.setOnClickListener { listener.onEditClick(pelicula) }
        holder.btnEliminar.setOnClickListener { listener.onDeleteClick(pelicula) }
        holder.itemView.setOnClickListener { listener.onPeliculaClick(pelicula, generoNombre) }
        holder.btnFavorite.setOnClickListener {
            val newFavoriteState = !pelicula.isFavorite
            listener.onFavoriteClick(pelicula, newFavoriteState)
        }
    }

    override fun getItemCount(): Int = peliculas.size

    fun updatePeliculas(newList: List<Peliculas>, newGenerosMap: Map<String, String>) {
        peliculas = newList
        generosMap = newGenerosMap
        notifyDataSetChanged()
    }

    inner class PeliculaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgPeliculaItem: ImageView = itemView.findViewById(R.id.img_pelicula_item)
        val tvTitulo: TextView = itemView.findViewById(R.id.tv_titulo_pelicula_item)
        val tvGenero: TextView = itemView.findViewById(R.id.tv_genero_pelicula_item)
        val tvAnio: TextView = itemView.findViewById(R.id.tv_anio_pelicula_item)
        val tvFechaLanzamiento: TextView = itemView.findViewById(R.id.tv_fecha_lanzamiento_pelicula_item)
        val tvDirector: TextView = itemView.findViewById(R.id.tv_director_pelicula_item)
        val tvDescripcion: TextView = itemView.findViewById(R.id.tv_descripcion_pelicula_item)
        val btnEditar: ImageButton = itemView.findViewById(R.id.btn_editar_pelicula_item)
        val btnEliminar: ImageButton = itemView.findViewById(R.id.btn_eliminar_pelicula_item)
        val btnFavorite: ImageButton = itemView.findViewById(R.id.btn_favorite_pelicula_item)
    }
}