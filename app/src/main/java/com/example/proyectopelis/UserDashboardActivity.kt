package com.example.proyectopelis

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View // Importar View para View.VISIBLE/GONE
import android.widget.FrameLayout // Importar FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectopelis.Models.Generos
import com.example.proyectopelis.Models.Peliculas
import com.example.proyectopelis.databinding.ActivityUserdashboardBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class UserDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserdashboardBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    // Declarar los contenedores FrameLayout (se mantienen por si los usas para otra cosa)
    private lateinit var homeContainer: FrameLayout
    private lateinit var searchContainer: FrameLayout
    private lateinit var favoritesContainer: FrameLayout
    private lateinit var profileContainer: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserdashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // Inicializar los contenedores FrameLayouts usando el binding
        homeContainer = binding.homeContainer
        searchContainer = binding.searchContainer
        favoritesContainer = binding.favoritesContainer
        profileContainer = binding.profileContainer

        // Asegurarse de que solo el contenedor de inicio sea visible por defecto
        showContainer(homeContainer)

        setupRecyclerViews()
        fetchNewPeliculas() // Carga las películas "Nuevas Pelis ✨"

        // Configuración del banner (ViewPager2)
        val bannerImages = listOf(
            "https://www.themoviedb.org/t/p/original/9gCgP447v930R1n8N2C01j8GgNl.jpg", // URL de tu imagen de Friends o similar
            "https://www.themoviedb.org/t/p/original/kYg3rO3D77yV30lqJ6wz0KqLwKx.jpg", // Otra URL
            "https://www.themoviedb.org/t/p/original/yfuL9T8R9ySg32oKzM9Lg7SgYxV.jpg"  // Otra URL
        )
        val bannerAdapter = BannerAdapter(bannerImages)
        binding.bannerViewPager.adapter = bannerAdapter

        // Conectar el TabLayout (indicador) al ViewPager2
        TabLayoutMediator(binding.bannerIndicator, binding.bannerViewPager) { tab, position ->
            // No necesitas texto, solo los puntos como indicadores
        }.attach()

        // Usamos 'loadGenreMovies' que ahora buscará el ID del género
        loadGenreMovies("Acción", binding.actionRecycler)
        loadGenreMovies("Comedia", binding.comediaRecycler)
        loadGenreMovies("Drama", binding.dramaRecycler)
        loadGenreMovies("Terror", binding.terrorRecycler)

        // Asigna listeners a los Chips
        binding.chipAccion.setOnClickListener { navigateToCategory("Acción") }
        binding.chipComedia.setOnClickListener { navigateToCategory("Comedia") }
        binding.chipDrama.setOnClickListener { navigateToCategory("Drama") }
        binding.chipTerror.setOnClickListener { navigateToCategory("Terror") }

        // Botón de cerrar sesión
        binding.btnLogout.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

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
            }}

        // *** LA LÓGICA DE bottomNavigationView HA SIDO ELIMINADA DE AQUÍ ***
    }

    // Nueva función para navegar a la actividad de categoría desde los Chips
    private fun navigateToCategory(genreName: String) {
        val intent = Intent(this, CategoryMoviesActivity::class.java)
        intent.putExtra("genre", genreName)
        startActivity(intent)
    }

    private fun setupRecyclerViews() {
        binding.recyclerNuevasPelis.apply {
            layoutManager = LinearLayoutManager(this@UserDashboardActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = MovieAdapter(mutableListOf())
        }

        binding.actionRecycler.apply {
            layoutManager = LinearLayoutManager(this@UserDashboardActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = MovieAdapter(mutableListOf())
        }
        binding.comediaRecycler.apply {
            layoutManager = LinearLayoutManager(this@UserDashboardActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = MovieAdapter(mutableListOf())
        }
        binding.dramaRecycler.apply {
            layoutManager = LinearLayoutManager(this@UserDashboardActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = MovieAdapter(mutableListOf())
        }
        binding.terrorRecycler.apply {
            layoutManager = LinearLayoutManager(this@UserDashboardActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = MovieAdapter(mutableListOf())
        }
    }

    private fun fetchNewPeliculas() {
        db.collection("peliculas")
            .orderBy("fechaLanzamiento", Query.Direction.DESCENDING)
            .limit(5)
            .get()
            .addOnSuccessListener { result ->
                val newPeliculas = result.documents.mapNotNull { document ->
                    document.toObject(Peliculas::class.java)?.apply { id = document.id }
                }
                (binding.recyclerNuevasPelis.adapter as? MovieAdapter)?.updatePeliculas(newPeliculas)
            }
            .addOnFailureListener { exception ->
                Log.e("UserDashboardActivity", "Error al obtener películas nuevas: $exception")
                Toast.makeText(this, "Error al cargar nuevas películas: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadGenreMovies(genreName: String, recyclerView: RecyclerView) {
        db.collection("generos")
            .whereEqualTo("nombre", genreName)
            .limit(1)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val genero = querySnapshot.documents[0].toObject(Generos::class.java)
                    genero?.let {
                        fetchPeliculasByGeneroId(it.id, recyclerView)
                    }
                } else {
                    Log.w("UserDashboardActivity", "No se encontró el género: $genreName")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("UserDashboardActivity", "Error al obtener ID del género $genreName: $exception")
                Toast.makeText(this, "Error al cargar género $genreName: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchPeliculasByGeneroId(generoId: String?, recyclerView: RecyclerView) {
        if (generoId == null) {
            Log.e("UserDashboardActivity", "ID de género es nulo, no se pueden cargar películas.")
            return
        }
        db.collection("peliculas")
            .whereEqualTo("generoId", generoId)
            .limit(10)
            .get()
            .addOnSuccessListener { result ->
                val peliculas = result.documents.mapNotNull { document ->
                    document.toObject(Peliculas::class.java)?.apply { id = document.id }
                }
                (recyclerView.adapter as? MovieAdapter)?.updatePeliculas(peliculas)
            }
            .addOnFailureListener { exception ->
                Log.e("UserDashboardActivity", "Error al obtener películas por generoId $generoId: $exception")
                Toast.makeText(this, "Error al cargar películas: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Función para mostrar solo el contenedor FrameLayout deseado y ocultar los demás
    // Por ahora, solo homeContainer será visible.
    private fun showContainer(containerToShow: FrameLayout) {
        homeContainer.visibility = if (containerToShow == homeContainer) View.VISIBLE else View.GONE
        searchContainer.visibility = if (containerToShow == searchContainer) View.VISIBLE else View.GONE
        favoritesContainer.visibility = if (containerToShow == favoritesContainer) View.VISIBLE else View.GONE
        profileContainer.visibility = if (containerToShow == profileContainer) View.VISIBLE else View.GONE
    }


}
