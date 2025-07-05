package com.example.proyectopelis

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.proyectopelis.Models.Generos
import com.example.proyectopelis.Models.Peliculas
import com.example.proyectopelis.databinding.ActivityCategoriaBinding
import com.google.firebase.firestore.FirebaseFirestore

class UserCategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoriaBinding
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoriaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()

        // Inicializa cada sección de género
        setupGeneroSection("Acción", binding.actionRecycler, binding.btnVerMasAccion)
        setupGeneroSection("Comedia", binding.comediaRecycler, binding.btnVerMasComedia)
        setupGeneroSection("Drama", binding.dramaRecycler, binding.btnVerMasDrama)
        setupGeneroSection("Terror", binding.terrorRecycler, binding.btnVerMasTerror)

        // ⬇️⬇️ Agrega aquí la navegación inferior
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
            }}}
    private fun setupGeneroSection(
        genreName: String,
        recyclerView: androidx.recyclerview.widget.RecyclerView,
        btnVerMas: View
    ) {
        val adapter = MovieAdapter(mutableListOf())
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = adapter

        // Cargar películas para el género
        db.collection("generos")
            .whereEqualTo("nombre", genreName)
            .limit(1)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val genero = querySnapshot.documents[0].toObject(Generos::class.java)
                    genero?.let {
                        fetchPeliculasByGeneroId(it.id, adapter)
                    }
                } else {
                    Log.w("GenerosActivity", "No se encontró el género: $genreName")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("GenerosActivity", "Error al obtener ID del género $genreName: $exception")
                Toast.makeText(this, "Error al cargar género $genreName: ${exception.message}", Toast.LENGTH_SHORT).show()
            }

        // Configurar botón "Ver más"
        btnVerMas.setOnClickListener {
            val intent = Intent(this, CategoryMoviesActivity::class.java)
            intent.putExtra("genre", genreName)
            startActivity(intent)
        }
    }

    private fun fetchPeliculasByGeneroId(generoId: String?, adapter: MovieAdapter) {
        if (generoId == null) {
            Log.e("GenerosActivity", "ID de género es nulo, no se pueden cargar películas.")
            return
        }

        db.collection("peliculas")
            .whereEqualTo("generoId", generoId)
            .limit(10) // Muestra solo 10 como preview
            .get()
            .addOnSuccessListener { result ->
                val peliculas = result.documents.mapNotNull { document ->
                    document.toObject(Peliculas::class.java)?.apply { id = document.id }
                }
                adapter.updatePeliculas(peliculas)
            }
            .addOnFailureListener { exception ->
                Log.e("GenerosActivity", "Error al obtener películas por generoId $generoId: $exception")
                Toast.makeText(this, "Error al cargar películas: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
