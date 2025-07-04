// com.yourpackage.adapters/PeliculasAdapter.kt
package com.example.proyectopelis.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide // Para cargar imágenes
import com.example.proyectopelis.Adapters.OnPeliculaActionListener
import com.example.proyectopelis.Models.Peliculas
import com.example.proyectopelis.R // Asegúrate deS que esta sea la R correcta de tu proyecto

class PeliculasAdapter(
    private var peliculas: List<Peliculas>,
    private val listener: OnPeliculaActionListener,
    private var generosMap: Map<String, String> // Mapa de generoId a nombre
) : RecyclerView.Adapter<PeliculasAdapter.PeliculaViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeliculaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pelicula, parent, false)
        return PeliculaViewHolder(view)
    }

    override fun onBindViewHolder(holder: PeliculaViewHolder, position: Int) {
        val pelicula = peliculas[position]

        holder.tvTitulo.text = pelicula.titulo
        holder.tvAnio.text = "Año: ${pelicula.anio}"

        // Obtener el nombre del género del mapa
        val generoNombre = generosMap[pelicula.generoId] ?: "Género desconocido"
        holder.tvGenero.text = "Género: $generoNombre"

        // *** AHORA LLENAR LOS NUEVOS CAMPOS ***
        holder.tvDirector.text = "Director: ${pelicula.director}"
        holder.tvDescripcion.text = pelicula.descripcion

        // Cargar la imagen
        if (!pelicula.imageUrl.isNullOrEmpty()) {
            Glide.with(holder.itemView.context)
                .load(pelicula.imageUrl)
                .placeholder(R.drawable.placeholder_poster) // Asegúrate de tener estos drawables

                .into(holder.imgPeliculaItem)
        } else {
            holder.imgPeliculaItem.setImageResource(R.drawable.placeholder_poster) // Usa un placeholder si no hay imagen
        }

        // Mostrar fecha de lanzamiento si existe
        if (!pelicula.fechaLanzamiento.isNullOrEmpty()) {
            holder.tvFechaLanzamiento.text = "Lanzamiento: ${pelicula.fechaLanzamiento}"
            holder.tvFechaLanzamiento.visibility = View.VISIBLE
        } else {
            holder.tvFechaLanzamiento.visibility = View.GONE
        }

        // Configurar listeners de los botones y del item completo
        holder.btnEditar.setOnClickListener { listener.onEditClick(pelicula) }
        holder.btnEliminar.setOnClickListener { listener.onDeleteClick(pelicula) }
        holder.itemView.setOnClickListener { listener.onPeliculaClick(pelicula, generoNombre) }
    }

    override fun getItemCount(): Int = peliculas.size

    fun updatePeliculas(newList: List<Peliculas>, newGenerosMap: Map<String, String>) {
        peliculas = newList
        generosMap = newGenerosMap
        notifyDataSetChanged()
    }

    // *** ACTUALIZA LA CLASE VIEWHOLDER CON LOS NUEVOS ID's ***
    inner class PeliculaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgPeliculaItem: ImageView = itemView.findViewById(R.id.img_pelicula_item)
        val tvTitulo: TextView = itemView.findViewById(R.id.tv_titulo_pelicula_item)
        val tvGenero: TextView = itemView.findViewById(R.id.tv_genero_pelicula_item)
        val tvAnio: TextView = itemView.findViewById(R.id.tv_anio_pelicula_item)
        val tvFechaLanzamiento: TextView = itemView.findViewById(R.id.tv_fecha_lanzamiento_pelicula_item)
        val tvDirector: TextView = itemView.findViewById(R.id.tv_director_pelicula_item)      // <--- NUEVO
        val tvDescripcion: TextView = itemView.findViewById(R.id.tv_descripcion_pelicula_item) // <--- NUEVO
        val btnEditar: ImageButton = itemView.findViewById(R.id.btn_editar_pelicula_item)
        val btnEliminar: ImageButton = itemView.findViewById(R.id.btn_eliminar_pelicula_item)
    }
}