package com.example.proyectopelis.Adapters

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.proyectopelis.Models.Peliculas
import com.example.proyectopelis.R // Asegúrate de que tu R esté correctamente importado
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore



class PeliculasAdapter(
    private var peliculas: List<Peliculas>,
    private val listener: OnPeliculaActionListener,
    private val generosMap: Map<String, String>,
    private val currentUserId: String?
) : RecyclerView.Adapter<PeliculasAdapter.PeliculaViewHolder>() {

    private val db = FirebaseFirestore.getInstance()

    class PeliculaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgPelicula: ImageView = itemView.findViewById(R.id.img_pelicula_item)
        val tvTitulo: TextView = itemView.findViewById(R.id.tv_titulo_pelicula_item)
        val tvGenero: TextView = itemView.findViewById(R.id.tv_genero_pelicula_item)
        val tvAnio: TextView = itemView.findViewById(R.id.tv_anio_pelicula_item)
        val tvFechaLanzamiento: TextView = itemView.findViewById(R.id.tv_fecha_lanzamiento_pelicula_item)
        val tvDirector: TextView = itemView.findViewById(R.id.tv_director_pelicula_item)
        val tvDescripcion: TextView = itemView.findViewById(R.id.tv_descripcion_pelicula_item)
        val btnFavorite: ImageButton = itemView.findViewById(R.id.btn_favorite_pelicula_item)
        val btnEditar: ImageButton = itemView.findViewById(R.id.btn_editar_pelicula_item)
        val btnEliminar: ImageButton = itemView.findViewById(R.id.btn_eliminar_pelicula_item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeliculaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pelicula, parent, false)
        return PeliculaViewHolder(view)
    }

    override fun onBindViewHolder(holder: PeliculaViewHolder, position: Int) {
        val pelicula = peliculas[position]
        val generoNombre = pelicula.generoId?.let { generosMap[it] } ?: "Género Desconocido"

        holder.tvTitulo.text = pelicula.titulo
        holder.tvGenero.text = "Género: $generoNombre"
        holder.tvAnio.text = "Año: ${pelicula.anio}"
        holder.tvFechaLanzamiento.text = "Lanzamiento: ${pelicula.fechaLanzamiento ?: "N/A"}" // Manejar posible null
        holder.tvDirector.text = "Director: ${pelicula.director}"
        holder.tvDescripcion.text = pelicula.descripcion

        // Cargar imagen con Glide
        Glide.with(holder.itemView.context)
            .load(pelicula.imageUrl) // ¡Usar imageUrl!

            .into(holder.imgPelicula)

        // --- LÓGICA DE FAVORITOS POR USUARIO ---
        val peliculaId = pelicula.id

        if (peliculaId == null) {
            Log.e("PeliculasAdapter", "Pelicula sin ID en posición $position. No se puede manejar favorito.")
            holder.btnFavorite.isEnabled = false
            updateFavoriteButtonState(holder.btnFavorite, false)
            return
        }

        if (currentUserId != null) {
            db.collection("peliculas").document(peliculaId)
                .collection("favorites").document(currentUserId)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    val isUserFavorite = documentSnapshot.exists()
                    updateFavoriteButtonState(holder.btnFavorite, isUserFavorite)
                }
                .addOnFailureListener { e ->
                    Log.e("PeliculasAdapter", "Error al verificar estado de favorito para $peliculaId: ${e.message}")
                    updateFavoriteButtonState(holder.btnFavorite, false)
                }
        } else {
            holder.btnFavorite.isEnabled = false
            updateFavoriteButtonState(holder.btnFavorite, false)
        }

        holder.btnFavorite.setOnClickListener {
            if (currentUserId == null) {
                Toast.makeText(holder.itemView.context, "Debes iniciar sesión para marcar como favorito.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Aquí necesitamos obtener el estado actual del corazón directamente de Firestore
            // para evitar inconsistencias si la UI no se ha refrescado.
            // Para simplificar y usar el estado actual del icono, lo hacemos como antes:
            val currentIconRes = if (holder.btnFavorite.drawable.constantState == ContextCompat.getDrawable(holder.itemView.context, R.drawable.heart_solid)?.constantState) {
                R.drawable.heart_solid
            } else {
                R.drawable.heart_regular
            }

            val isCurrentlyFavorite = (currentIconRes == R.drawable.heart_solid)

            listener.onFavoriteClick(peliculaId, !isCurrentlyFavorite, currentUserId)
        }
        // --- FIN LÓGICA DE FAVORITOS POR USUARIO ---

        holder.btnEditar.setOnClickListener { listener.onEditClick(pelicula) }
        holder.btnEliminar.setOnClickListener { listener.onDeleteClick(pelicula) }
        holder.itemView.setOnClickListener { listener.onPeliculaClick(pelicula, generoNombre) }
    }

    override fun getItemCount(): Int = peliculas.size

    fun updatePeliculas(newPeliculas: List<Peliculas>, newGenerosMap: Map<String, String>) {
        this.peliculas = newPeliculas
        // Si el mapa de géneros también se actualiza, deberías pasarlo y asignarlo aquí
        // this.generosMap = newGenerosMap // Descomentar si generosMap no es estático
        notifyDataSetChanged()
    }

    private fun updateFavoriteButtonState(button: ImageButton, isFavorite: Boolean) {
        val iconRes = if (isFavorite) R.drawable.heart_solid else R.drawable.heart_regular
        button.setImageResource(iconRes)
        val tintColor = if (isFavorite) {
            Color.parseColor("#E91E63") // Tu color rojo para favoritos
        } else {
            Color.GRAY // Un color neutro para "no favorito"
        }
        button.setColorFilter(tintColor, PorterDuff.Mode.SRC_IN)
    }

    fun updatePeliculaFavoriteStatus(peliculaId: String, newFavoriteState: Boolean) {
        val index = peliculas.indexOfFirst { it.id == peliculaId }
        if (index != -1) {
            notifyItemChanged(index)
        }
    }
}