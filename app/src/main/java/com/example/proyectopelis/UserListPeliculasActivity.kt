package com.example.proyectopelis

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.proyectopelis.Models.Peliculas
import com.example.proyectopelis.databinding.ActivityListaPeliculasBinding
import com.google.firebase.firestore.FirebaseFirestore

class UserListPeliculasActivity: AppCompatActivity() {

    private lateinit var binding: ActivityListaPeliculasBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var peliculasAdapter: MovieAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityListaPeliculasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        // Configura el RecyclerView
        peliculasAdapter = MovieAdapter(mutableListOf())
        binding.allMoviesRecycler.apply {
            binding.allMoviesRecycler.layoutManager = GridLayoutManager(this@UserListPeliculasActivity, 3)

            adapter = peliculasAdapter
        }

        // Carga todas las películas
        fetchAllPeliculas()

        // ✅ Navegación inferior
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> {
                    val intent = Intent(this, UserDashboardActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_generos -> {
                    val intent = Intent(this, UserCategoryActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_lista -> {
                    val intent = Intent(this, UserListPeliculasActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_perfil -> {
                    val intent = Intent(this, UserPerfilActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
    }

    private fun fetchAllPeliculas() {
        db.collection("peliculas")
            .get()
            .addOnSuccessListener { result ->
                val peliculas = result.documents.mapNotNull { document ->
                    document.toObject(Peliculas::class.java)?.apply { id = document.id }
                }
                peliculasAdapter.updatePeliculas(peliculas)
            }
            .addOnFailureListener { exception ->
                Log.e("AllMoviesActivity", "Error al obtener películas: $exception")
                Toast.makeText(this, "Error al cargar películas: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}