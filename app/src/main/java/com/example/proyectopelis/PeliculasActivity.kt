    package com.example.proyectopelis
    import android.widget.Toast
    import android.app.AlertDialog
    import android.app.DatePickerDialog // Para el selector de fecha
    import android.content.Intent
    import android.net.Uri
    import android.os.Bundle
    import android.text.Editable
    import android.text.TextWatcher
    import android.util.Log
    import android.view.LayoutInflater
    import android.view.View
    import android.widget.ArrayAdapter
    import android.widget.AutoCompleteTextView // Para el género en el diálogo
    import android.widget.Button
    import android.widget.EditText
    import android.widget.ImageView
    import android.widget.TextView

    import androidx.appcompat.app.AppCompatActivity
    import androidx.recyclerview.widget.LinearLayoutManager
    import androidx.recyclerview.widget.RecyclerView
    import com.bumptech.glide.Glide
    import com.example.proyectopelis.Models.Generos
    import com.example.proyectopelis.Models.Peliculas
    import com.google.android.material.dialog.MaterialAlertDialogBuilder
    import com.google.android.material.textfield.TextInputEditText
    import com.google.android.material.textfield.TextInputLayout
    import com.google.firebase.firestore.FirebaseFirestore
    import com.google.firebase.firestore.ktx.firestore
    import com.google.firebase.ktx.Firebase
    import com.example.proyectopelis.Adapters.OnPeliculaActionListener
    import com.example.proyectopelis.Adapters.PeliculasAdapter // Asegúrate de esta ruta
    import java.util.Calendar // Para el selector de fecha

    class PeliculasActivity : AppCompatActivity(), OnPeliculaActionListener {

        private lateinit var db: FirebaseFirestore
        private lateinit var peliculasAdapter: PeliculasAdapter
        private lateinit var rvPeliculas: RecyclerView
        private lateinit var btnCrearPelicula: Button
        private lateinit var btnRecargarLista: Button
        private lateinit var etSearchPelicula: TextInputEditText

        private var allPeliculasList: List<Peliculas> = emptyList() // Lista completa de películas
        private var allGenerosList: List<Generos> = emptyList() // Lista completa de géneros (para el Spinner y Adapter)
        private var generosMap: Map<String, String> = emptyMap() // Mapa de generoId a nombre (para el Adapter)


        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_peliculas)

            db = Firebase.firestore

            // Inicializar vistas del layout de la actividad
            rvPeliculas = findViewById(R.id.rv_peliculas)
            btnCrearPelicula = findViewById(R.id.btn_crear_pelicula)
            btnRecargarLista = findViewById(R.id.btn_leer_peliculas)
            etSearchPelicula = findViewById(R.id.et_search_pelicula)

            // Inicializar el adaptador de películas con una lista vacía y un mapa de géneros vacío
            peliculasAdapter = PeliculasAdapter(emptyList(), this, emptyMap())
            rvPeliculas.layoutManager = LinearLayoutManager(this)
            rvPeliculas.adapter = peliculasAdapter

            // Listeners para los botones
            btnCrearPelicula.setOnClickListener {
                mostrarDialogoFormularioPelicula(null) // null para indicar que es una nueva película
            }

            btnRecargarLista.setOnClickListener {
                leerPeliculas() // Recargar la lista completa
            }

            // Listener para el campo de búsqueda
            etSearchPelicula.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    filterPeliculas(s.toString())
                }
            })

            // Cargar datos al iniciar la actividad
            leerGenerosParaPeliculas { // Primero carga los géneros
                leerPeliculas() // Luego carga las películas
            }
        }

        // --- FUNCIONES CRUD DE PELÍCULAS ---

        private fun crearPelicula(pelicula: Peliculas) {
            db.collection("peliculas")
                .add(pelicula)
                .addOnSuccessListener { documentReference ->
                    Log.d("PeliculasActivity", "Película añadida con ID: ${documentReference.id}")
                    Toast.makeText(this, "Película '${pelicula.titulo}' creada.", Toast.LENGTH_SHORT).show()
                    leerPeliculas() // Recargar la lista después de crear
                }
                .addOnFailureListener { e ->
                    Log.w("PeliculasActivity", "Error al añadir película", e)
                    Toast.makeText(this, "Error al crear película: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }

        private fun leerPeliculas() {
            db.collection("peliculas")
                .get()
                .addOnSuccessListener { queryDocumentSnapshots ->
                    val peliculasList = mutableListOf<Peliculas>()
                    for (document in queryDocumentSnapshots.documents) {
                        val pelicula = document.toObject(Peliculas::class.java)
                        pelicula?.let {
                            it.id = document.id // Asegura que el ID esté en el objeto
                            peliculasList.add(it)
                        }
                    }
                    allPeliculasList = peliculasList // Guarda la lista completa sin filtrar
                    filterPeliculas(etSearchPelicula.text.toString()) // Aplica el filtro si hay texto en búsqueda
                    Log.d("PeliculasActivity", "Películas cargadas: ${allPeliculasList.size}")
                }
                .addOnFailureListener { e ->
                    Log.w("PeliculasActivity", "Error al obtener películas", e)
                    Toast.makeText(this, "Error al cargar películas: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }

        private fun actualizarPelicula(peliculaId: String, pelicula: Peliculas) {
            db.collection("peliculas").document(peliculaId)
                .set(pelicula) // set(pelicula) reemplaza el documento
                .addOnSuccessListener {
                    Log.d("PeliculasActivity", "Película $peliculaId actualizada.")
                    Toast.makeText(this, "Película actualizada.", Toast.LENGTH_SHORT).show()
                    leerPeliculas() // Recargar la lista después de actualizar
                }
                .addOnFailureListener { e ->
                    Log.w("PeliculasActivity", "Error al actualizar película", e)
                    Toast.makeText(this, "Error al actualizar película: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        private fun eliminarPelicula(peliculaId: String, tituloPelicula: String) {
            db.collection("peliculas").document(peliculaId)
                .delete()
                .addOnSuccessListener {
                    Log.d("PeliculasActivity", "Película $peliculaId eliminada.")
                    Toast.makeText(this, "Película '$tituloPelicula' eliminada.", Toast.LENGTH_SHORT).show()
                    leerPeliculas() // Recargar la lista después de eliminar
                }
                .addOnFailureListener { e ->
                    Log.w("PeliculasActivity", "Error al eliminar película", e)
                    Toast.makeText(this, "Error al eliminar película: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        // --- CARGA DE GÉNEROS (Necesaria para el Spinner y el Adaptador) ---

        private fun leerGenerosParaPeliculas(onComplete: () -> Unit) {
            db.collection("generos")
                .get()
                .addOnSuccessListener { queryDocumentSnapshots ->
                    val generosList = mutableListOf<Generos>()
                    val tempGenerosMap = mutableMapOf<String, String>()
                    for (document in queryDocumentSnapshots.documents) {
                        val genero = document.toObject(Generos::class.java)
                        genero?.let {
                            it.id = document.id
                            generosList.add(it)
                            it.id?.let { id ->
                                tempGenerosMap[id] = it.nombre // Rellenar el mapa
                            }
                        }
                    }
                    allGenerosList = generosList
                    generosMap = tempGenerosMap // Actualiza el mapa global
                    Log.d("PeliculasActivity", "Géneros cargados para películas: ${allGenerosList.size}")
                    onComplete() // Llama al callback cuando los géneros estén listos
                }
                .addOnFailureListener { e ->
                    Log.w("PeliculasActivity", "Error al obtener géneros para películas", e)
                    Toast.makeText(this, "Error al cargar géneros: ${e.message}", Toast.LENGTH_LONG).show()
                    onComplete() // Llama al callback incluso si falla, para no bloquear
                }
        }

        // --- FILTRADO DE PELÍCULAS ---

        private fun filterPeliculas(query: String) {
            val filteredList = if (query.isEmpty()) {
                allPeliculasList // Si la búsqueda está vacía, muestra toda la lista
            } else {
                allPeliculasList.filter {
                    it.titulo.contains(query, ignoreCase = true) ||
                            it.director.contains(query, ignoreCase = true) ||
                            (it.generoId?.let { generosMap[it]?.contains(query, ignoreCase = true) } == true)
                    // Puedes añadir más campos a buscar
                }
            }
            // Actualiza el adaptador con la lista filtrada y el mapa de géneros
            peliculasAdapter.updatePeliculas(filteredList, generosMap)
        }

        // --- IMPLEMENTACIÓN DE LA INTERFAZ OnPeliculaActionListener (para clics en ítems) ---

        override fun onEditClick(pelicula: Peliculas) {
            mostrarDialogoFormularioPelicula(pelicula) // Pasa la película para editar
        }

        override fun onDeleteClick(pelicula: Peliculas) {
            mostrarDialogoConfirmarEliminacion(pelicula)
        }

        override fun onPeliculaClick(pelicula: Peliculas, generoNombre: String?) {
            // Por ahora, solo muestra un Toast, la pantalla de detalles se implementará después
            Toast.makeText(this, "Has clicado en: ${pelicula.titulo} (Género: $generoNombre)", Toast.LENGTH_SHORT).show()
            // Aquí es donde en el futuro lanzarías el Intent a PeliculaDetalleActivity
            /*
            val intent = Intent(this, PeliculaDetalleActivity::class.java).apply {
                putExtra("pelicula", pelicula)
                putExtra("generoNombre", generoNombre)
            }
            startActivity(intent)
            */
        }

        // --- MÉTODOS PARA MOSTRAR LOS DIÁLOGOS ---

        private fun mostrarDialogoFormularioPelicula(pelicula: Peliculas?) {
            val isEditing = pelicula != null
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_form_pelicula, null)

            // Referencias a los campos del diálogo
            val tilTitulo = dialogView.findViewById<TextInputLayout>(R.id.til_titulo_pelicula)
            val etTitulo = dialogView.findViewById<TextInputEditText>(R.id.et_titulo_pelicula)
            val etDescripcion = dialogView.findViewById<TextInputEditText>(R.id.et_descripcion_pelicula)
            val etAnio = dialogView.findViewById<TextInputEditText>(R.id.et_anio_pelicula)
            val etDuracion = dialogView.findViewById<TextInputEditText>(R.id.et_duracion_pelicula)
            val etDirector = dialogView.findViewById<TextInputEditText>(R.id.et_director_pelicula)
            val etImageUrl = dialogView.findViewById<TextInputEditText>(R.id.et_image_url_pelicula)
            val etTrailerUrl = dialogView.findViewById<TextInputEditText>(R.id.et_trailer_url_pelicula)
            val etFechaLanzamiento = dialogView.findViewById<TextInputEditText>(R.id.et_fecha_lanzamiento_pelicula)
            val actvGenero = dialogView.findViewById<AutoCompleteTextView>(R.id.actv_genero_pelicula)

            // Configurar Spinner/AutoCompleteTextView de Géneros
            val generoNombres = allGenerosList.map { it.nombre }
            val generoAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, generoNombres)
            actvGenero.setAdapter(generoAdapter)

            // Listener para abrir el DatePickerDialog al hacer clic en el campo de fecha
            etFechaLanzamiento.setOnClickListener {
                val calendar = Calendar.getInstance()
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)

                val datePickerDialog = DatePickerDialog(this,
                    { _, selectedYear, selectedMonth, selectedDay ->
                        val monthFormatted = String.format("%02d", selectedMonth + 1) // +1 porque el mes es base 0
                        val dayFormatted = String.format("%02d", selectedDay)
                        etFechaLanzamiento.setText("$selectedYear-$monthFormatted-$dayFormatted")
                    }, year, month, day)
                datePickerDialog.show()
            }


            // Si estamos editando, precargar los datos de la película
            if (isEditing) {
                etTitulo.setText(pelicula?.titulo)
                etDescripcion.setText(pelicula?.descripcion)
                etAnio.setText(pelicula?.anio.toString())
                etDuracion.setText(pelicula?.duracionMinutos.toString())
                etDirector.setText(pelicula?.director)
                etImageUrl.setText(pelicula?.imageUrl)
                etTrailerUrl.setText(pelicula?.trailerUrl)
                etFechaLanzamiento.setText(pelicula?.fechaLanzamiento)

                // Seleccionar el género actual en el AutoCompleteTextView
                pelicula?.generoId?.let { id ->
                    val selectedGenero = allGenerosList.find { it.id == id }
                    actvGenero.setText(selectedGenero?.nombre, false) // false para no ejecutar el filtro del ArrayAdapter
                }
            }

            // Configurar el diálogo MaterialAlertDialogBuilder
            MaterialAlertDialogBuilder(this)
                .setTitle(if (isEditing) "Editar Película" else "Crear Nueva Película")
                .setView(dialogView)
                .setPositiveButton(if (isEditing) "Guardar Cambios" else "Crear") { dialog, _ ->
                    // Validar y obtener los datos
                    val titulo = etTitulo.text.toString().trim()
                    val descripcion = etDescripcion.text.toString().trim()
                    val anioStr = etAnio.text.toString().trim()
                    val duracionStr = etDuracion.text.toString().trim()
                    val director = etDirector.text.toString().trim()
                    val imageUrl = etImageUrl.text.toString().trim()
                    val trailerUrl = etTrailerUrl.text.toString().trim()
                    val fechaLanzamiento = etFechaLanzamiento.text.toString().trim()

                    // Validaciones básicas
                    if (titulo.isEmpty() || anioStr.isEmpty() || duracionStr.isEmpty() || director.isEmpty() || actvGenero.text.toString().trim().isEmpty()) {
                        Toast.makeText(this, "Por favor, completa los campos obligatorios.", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton // No cierres el diálogo si la validación falla
                    }

                    val anio = anioStr.toIntOrNull()
                    val duracion = duracionStr.toIntOrNull()

                    if (anio == null || duracion == null) {
                        Toast.makeText(this, "Año y Duración deben ser números válidos.", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    // Obtener el ID del género seleccionado
                    val generoSeleccionadoNombre = actvGenero.text.toString().trim()
                    val generoSeleccionado = allGenerosList.find { it.nombre == generoSeleccionadoNombre }
                    val generoId = generoSeleccionado?.id

                    if (generoId == null) {
                        Toast.makeText(this, "Selecciona un género válido.", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }

                    val nuevaPelicula = Peliculas(
                        id = pelicula?.id, // Conserva el ID si estamos editando
                        titulo = titulo,
                        descripcion = descripcion,
                        anio = anio,
                        director = director,
                        duracionMinutos = duracion,
                        generoId = generoId,
                        imageUrl = imageUrl.ifEmpty { null }, // Guarda null si el campo está vacío
                        trailerUrl = trailerUrl.ifEmpty { null }, // Guarda null si el campo está vacío
                        fechaLanzamiento = fechaLanzamiento.ifEmpty { null }
                    )

                    if (isEditing && pelicula != null) {
                        actualizarPelicula(pelicula.id!!, nuevaPelicula)
                    } else {
                        crearPelicula(nuevaPelicula)
                    }
                    dialog.dismiss() // Cierra el diálogo solo si todo es válido
                }
                .setNegativeButton("Cancelar") { dialog, _ -> dialog.cancel() }
                .show()
        }

        private fun mostrarDialogoConfirmarEliminacion(pelicula: Peliculas) {
            MaterialAlertDialogBuilder(this)
                .setTitle("Eliminar Película")
                .setMessage("¿Estás seguro de que quieres eliminar la película '${pelicula.titulo}'?")
                .setPositiveButton("Eliminar") { dialog, _ ->
                    pelicula.id?.let { id ->
                        eliminarPelicula(id, pelicula.titulo)
                    } ?: Toast.makeText(this, "Error: ID de película no encontrado.", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                .setNegativeButton("Cancelar") { dialog, _ -> dialog.cancel() }
                .show()
        }



    }
