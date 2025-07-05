package com.example.proyectopelis

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.proyectopelis.databinding.ActivityMovieDetailBinding
import com.example.proyectopelis.Models.Peliculas
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class MovieDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMovieDetailBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private var isFavorite = false
    private var movieId: String? = null
    private var currentUserId: String? = null
    private var pelicula: Peliculas? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovieDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        currentUserId = auth.currentUser?.uid

        movieId = intent.getStringExtra("movieId")
        if (movieId != null) {
            loadMovieDetails(movieId!!)
        } else {
            Toast.makeText(this, "No se encontró la película.", Toast.LENGTH_SHORT).show()
            finish()
        }

        // Click listener del corazón
        binding.ivFavorite.setOnClickListener {
            if (movieId != null && currentUserId != null && pelicula != null) {
                if (isFavorite) {
                    removeFavorite(movieId!!, currentUserId!!)
                } else {
                    addFavorite(movieId!!, currentUserId!!)
                }
            }
        }
    }

    private fun loadMovieDetails(id: String) {
        db.collection("peliculas").document(id).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    pelicula = document.toObject(Peliculas::class.java)
                    pelicula?.let { peli ->
                        // Poster
                        Glide.with(this)
                            .load(peli.imageUrl)
                            .into(binding.ivPoster)

                        binding.tvTitle.text = peli.titulo
                        binding.tvDescription.text = peli.descripcion
                        binding.tvYear.text = "Año: ${peli.anio}"
                        binding.tvDirector.text = "Director: ${peli.director}"
                        binding.tvDuration.text = "Duración: ${peli.duracionMinutos} min"
                        binding.tvFechaLanzamiento.text = "Estreno: ${peli.fechaLanzamiento}"
                        binding.tvFavoriteCount.text = "Favoritos: ${peli.favoriteCount}"

                        // Trailer
                        val trailerId = peli.trailerUrl?.substringAfter("v=")
                        val trailerEmbed = "https://www.youtube.com/embed/$trailerId"
                        val html = """
                            <iframe width="100%" height="100%" src="$trailerEmbed" frameborder="0" allowfullscreen></iframe>
                        """.trimIndent()

                        binding.webTrailer.settings.javaScriptEnabled = true
                        binding.webTrailer.loadData(html, "text/html", "utf-8")

                        // Verificar si ya es favorito
                        if (peli.favoriteUsers?.contains(currentUserId) == true) {
                            binding.ivFavorite.setImageResource(R.drawable.heart_solid)
                            isFavorite = true
                        } else {
                            binding.ivFavorite.setImageResource(R.drawable.heart_regular)
                            isFavorite = false
                        }
                    }
                } else {
                    Toast.makeText(this, "Película no encontrada.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar detalles.", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    private fun addFavorite(movieId: String, userId: String) {
        val movieRef = db.collection("peliculas").document(movieId)
        movieRef.update("favoriteUsers", FieldValue.arrayUnion(userId))
        movieRef.update("favoriteCount", FieldValue.increment(1))
        binding.ivFavorite.setImageResource(R.drawable.heart_solid)
        isFavorite = true
        Toast.makeText(this, "Agregado a favoritos", Toast.LENGTH_SHORT).show()
    }

    private fun removeFavorite(movieId: String, userId: String) {
        val movieRef = db.collection("peliculas").document(movieId)
        movieRef.update("favoriteUsers", FieldValue.arrayRemove(userId))
        movieRef.update("favoriteCount", FieldValue.increment(-1))
        binding.ivFavorite.setImageResource(R.drawable.heart_regular)
        isFavorite = false
        Toast.makeText(this, "Eliminado de favoritos", Toast.LENGTH_SHORT).show()
    }
}
