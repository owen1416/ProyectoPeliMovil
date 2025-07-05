    package com.example.proyectopelis // Cambia a tu paquete

    import android.os.Bundle
    import android.util.Log
    import android.widget.Toast
    import androidx.appcompat.app.AppCompatActivity
    import androidx.recyclerview.widget.GridLayoutManager
    import com.example.proyectopelis.Models.Generos
    import com.example.proyectopelis.Models.Peliculas
    import com.example.proyectopelis.databinding.ActivityCategoryMoviesBinding
    import com.google.firebase.firestore.FirebaseFirestore

    class CategoryMoviesActivity : AppCompatActivity() {

        private lateinit var binding: ActivityCategoryMoviesBinding
        private lateinit var db: FirebaseFirestore
        private lateinit var peliculasAdapter: MovieAdapter

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding = ActivityCategoryMoviesBinding.inflate(layoutInflater)
            setContentView(binding.root)

            db = FirebaseFirestore.getInstance()

            val generoNombre = intent.getStringExtra("genre") // Obtiene el nombre del género
            if (generoNombre.isNullOrEmpty()) {
                Toast.makeText(this, "Género no especificado", Toast.LENGTH_SHORT).show()
                finish()
                return
            }

            binding.categoryTitle.text = "Género: $generoNombre"

            peliculasAdapter = MovieAdapter(mutableListOf())
            binding.categoryRecyclerView.apply {
                layoutManager = GridLayoutManager(this@CategoryMoviesActivity, 2)
                adapter = peliculasAdapter
            }

            // Ahora obtenemos el ID del género antes de buscar películas
            loadCategoryMovies(generoNombre)
        }

        // Nueva función para obtener el ID del género y luego las películas
        private fun loadCategoryMovies(genreName: String) {
            db.collection("generos") // Asumiendo que la colección se llama 'generos'
                .whereEqualTo("nombre", genreName) // Busca el documento de género por su nombre
                .limit(1)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val genero = querySnapshot.documents[0].toObject(Generos::class.java) // ¡Aquí se usa 'Generos'!
                        genero?.let {
                            fetchPeliculasByGeneroId(it.id) // Una vez que tenemos el ID, buscamos las películas
                        }
                    } else {
                        Log.w("CategoryMoviesActivity", "No se encontró el género: $genreName")
                        Toast.makeText(this, "No se encontraron películas para este género.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("CategoryMoviesActivity", "Error al obtener ID del género $genreName: $exception")
                    Toast.makeText(this, "Error al cargar género $genreName: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // Función que ahora usa el ID del género
        private fun fetchPeliculasByGeneroId(generoId: String?) { // generoId puede ser nulo
            if (generoId == null) {
                Log.e("CategoryMoviesActivity", "ID de género es nulo, no se pueden cargar películas.")
                return
            }
            db.collection("peliculas")
                .whereEqualTo("generoId", generoId) // ¡Ahora usamos el ID real del género!
                .get()
                .addOnSuccessListener { result ->
                    val peliculas = result.documents.mapNotNull { document ->
                        document.toObject(Peliculas::class.java)?.apply { id = document.id }
                    }
                    peliculasAdapter.updatePeliculas(peliculas)
                }
                .addOnFailureListener { exception ->
                    Log.e("CategoryMoviesActivity", "Error al obtener películas por generoId $generoId: $exception")
                    Toast.makeText(this, "Error al cargar películas: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }


