package com.example.proyectopelis // Cambia a tu paquete

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.proyectopelis.Models.Peliculas
import com.example.proyectopelis.databinding.ItemMovieBinding

class MovieAdapter(private val peliculas: MutableList<Peliculas>) : RecyclerView.Adapter<MovieAdapter.MovieViewHolder>() {

    class MovieViewHolder(private val binding: ItemMovieBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(pelicula: Peliculas) {
            binding.movieTitle.text = pelicula.titulo
            Glide.with(binding.movieImage.context)
                .load(pelicula.imageUrl)
                .placeholder(R.drawable.placeholder_movie)
                .into(binding.movieImage)

            binding.root.setOnClickListener {
                val context = binding.root.context
                val intent = Intent(context, MovieDetailActivity::class.java)
                intent.putExtra("movieId", pelicula.id) // Usa el id correcto
                context.startActivity(intent)
            }
        }

    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val binding = ItemMovieBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MovieViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        holder.bind(peliculas[position])
    }

    override fun getItemCount(): Int = peliculas.size

    // Método para actualizar la lista de películas y notificar al adaptador
    fun updatePeliculas(newPeliculas: List<Peliculas>) {
        peliculas.clear()
        peliculas.addAll(newPeliculas)
        notifyDataSetChanged()
    }


}