package com.example.proyectopelis

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.proyectopelis.Adapters.OnPeliculaActionListener
import com.example.proyectopelis.Adapters.PeliculasAdapter
import com.example.proyectopelis.Models.Generos
import com.example.proyectopelis.Models.Peliculas
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth // Importar FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.Timestamp // Importar Timestamp
import com.google.firebase.firestore.FieldValue
import java.util.Calendar

class PeliculasActivity : AppCompatActivity(), OnPeliculaActionListener {

    private lateinit var db: FirebaseFirestore
    private lateinit var peliculasAdapter: PeliculasAdapter
    private lateinit var rvPeliculas: RecyclerView
    private lateinit var btnCrearPelicula: Button
    private lateinit var btnRecargarLista: Button
    private lateinit var etSearchPelicula: TextInputEditText

    private var allPeliculasList: List<Peliculas> = emptyList()
    private var allGenerosList: List<Generos> = emptyList()
    private var generosMap: Map<String, String> = emptyMap()

    private lateinit var auth: FirebaseAuth // Declarar FirebaseAuth
    private var currentUserId: String? = null // Para almacenar el ID del usuario actual

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_peliculas)

        db = Firebase.firestore
        auth = FirebaseAuth.getInstance() // Inicializar FirebaseAuth
        currentUserId = auth.currentUser?.uid // Obtener el ID del usuario logueado

        rvPeliculas = findViewById(R.id.rv_peliculas)
        btnCrearPelicula = findViewById(R.id.btn_crear_pelicula)
        btnRecargarLista = findViewById(R.id.btn_leer_peliculas)
        etSearchPelicula = findViewById(R.id.et_search_pelicula)

        // IMPORTANTE: Pasar el currentUserId al adaptador
        peliculasAdapter = PeliculasAdapter(emptyList(), this, emptyMap(), currentUserId)
        rvPeliculas.layoutManager = LinearLayoutManager(this)
        rvPeliculas.adapter = peliculasAdapter

        btnCrearPelicula.setOnClickListener {
            mostrarDialogoFormularioPelicula(null)
        }

        btnRecargarLista.setOnClickListener {
            leerPeliculas()
        }

        etSearchPelicula.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                filterPeliculas(s.toString())
            }
        })

        leerGenerosParaPeliculas {
            leerPeliculas()
        }
    }

    // --- FUNCIONES CRUD DE PELÍCULAS ---

    private fun crearPelicula(pelicula: Peliculas) {
        db.collection("peliculas")
            .add(pelicula)
            .addOnSuccessListener { documentReference ->
                Log.d("PeliculasActivity", "Película añadida con ID: ${documentReference.id}")
                Toast.makeText(this, "Película '${pelicula.titulo}' creada.", Toast.LENGTH_SHORT).show()
                leerPeliculas()
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
                        it.id = document.id
                        peliculasList.add(it)
                    }
                }
                allPeliculasList = peliculasList
                filterPeliculas(etSearchPelicula.text.toString())
                Log.d("PeliculasActivity", "Películas cargadas: ${allPeliculasList.size}")
            }
            .addOnFailureListener { e ->
                Log.w("PeliculasActivity", "Error al obtener películas", e)
                Toast.makeText(this, "Error al cargar películas: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    // MODIFICAR actualizarPelicula para usar los campos de tu Peliculas data class
    private fun actualizarPelicula(peliculaId: String, pelicula: Peliculas) {
        // Crear un mapa solo con los campos que se deben actualizar desde este formulario
        val updates = mapOf(
            "titulo" to pelicula.titulo,
            "descripcion" to pelicula.descripcion,
            "anio" to pelicula.anio,
            "director" to pelicula.director,
            "duracionMinutos" to pelicula.duracionMinutos,
            "generoId" to pelicula.generoId,
            "imageUrl" to pelicula.imageUrl, // ¡Corregido a imageUrl!
            "trailerUrl" to pelicula.trailerUrl,
            "fechaLanzamiento" to pelicula.fechaLanzamiento
            // NO INCLUIR 'favoriteCount' AQUÍ, lo gestiona la Cloud Function
        )

        db.collection("peliculas").document(peliculaId)
            .update(updates) // Usar update() para actualizar solo los campos especificados
            .addOnSuccessListener {
                Log.d("PeliculasActivity", "Película $peliculaId actualizada.")
                Toast.makeText(this, "Película actualizada.", Toast.LENGTH_SHORT).show()
                leerPeliculas() // Recargar la lista para reflejar los cambios
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
                leerPeliculas()
            }
            .addOnFailureListener { e ->
                Log.w("PeliculasActivity", "Error al eliminar película", e)
                Toast.makeText(this, "Error al eliminar película: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // --- CARGA DE GÉNEROS ---

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
                            tempGenerosMap[id] = it.nombre
                        }
                    }
                }
                allGenerosList = generosList
                generosMap = tempGenerosMap
                Log.d("PeliculasActivity", "Géneros cargados para películas: ${allGenerosList.size}")
                onComplete()
            }
            .addOnFailureListener { e ->
                Log.w("PeliculasActivity", "Error al obtener géneros para películas", e)
                Toast.makeText(this, "Error al cargar géneros: ${e.message}", Toast.LENGTH_LONG).show()
                onComplete()
            }
    }

    // --- FILTRADO DE PELÍCULAS ---

    private fun filterPeliculas(query: String) {
        val filteredList = if (query.isEmpty()) {
            allPeliculasList
        } else {
            allPeliculasList.filter {
                it.titulo.contains(query, ignoreCase = true) || // .titulo ahora no es nullable
                        it.director.contains(query, ignoreCase = true) || // .director ahora no es nullable
                        (it.generoId?.let { generosMap[it]?.contains(query, ignoreCase = true) } == true)
            }
        }
        peliculasAdapter.updatePeliculas(filteredList, generosMap)
    }

    // --- IMPLEMENTACIÓN DE LA INTERFAZ OnPeliculaActionListener ---

    override fun onEditClick(pelicula: Peliculas) {
        mostrarDialogoFormularioPelicula(pelicula)
    }

    override fun onDeleteClick(pelicula: Peliculas) {
        mostrarDialogoConfirmarEliminacion(pelicula)
    }

    override fun onPeliculaClick(pelicula: Peliculas, generoNombre: String?) {
        Toast.makeText(this, "Has clicado en: ${pelicula.titulo} (Género: ${generoNombre ?: "N/A"})", Toast.LENGTH_SHORT).show()
    }

    override fun onFavoriteClick(peliculaId: String, isFavorite: Boolean, userId: String) {
        val peliculaDocRef = db.collection("peliculas").document(peliculaId)

        if (isFavorite) {
            // Incrementar favoriteCount y añadir subdocumento de favorito
            val favoriteRef = peliculaDocRef.collection("favorites").document(userId)
            db.runBatch { batch ->
                batch.update(peliculaDocRef, "favoriteCount", FieldValue.increment(1))
                batch.set(favoriteRef, mapOf("timestamp" to Timestamp.now()))
            }.addOnSuccessListener {
                Log.d("PeliculasActivity", "Favorito agregado y contador incrementado")
                Toast.makeText(this, "Añadida a favoritos", Toast.LENGTH_SHORT).show()
                peliculasAdapter.updatePeliculaFavoriteStatus(peliculaId, true)
            }.addOnFailureListener { e ->
                Log.e("PeliculasActivity", "Error marcando favorito o incrementando: ${e.message}", e)
                Toast.makeText(this, "Error al marcar favorito", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Decrementar favoriteCount y eliminar subdocumento de favorito
            val favoriteRef = peliculaDocRef.collection("favorites").document(userId)
            db.runBatch { batch ->
                batch.update(peliculaDocRef, "favoriteCount", FieldValue.increment(-1))
                batch.delete(favoriteRef)
            }.addOnSuccessListener {
                Log.d("PeliculasActivity", "Favorito eliminado y contador decrementado")
                Toast.makeText(this, "Eliminado de favoritos", Toast.LENGTH_SHORT).show()
                peliculasAdapter.updatePeliculaFavoriteStatus(peliculaId, false)
            }.addOnFailureListener { e ->
                Log.e("PeliculasActivity", "Error eliminando favorito o decrementando: ${e.message}", e)
                Toast.makeText(this, "Error al eliminar favorito", Toast.LENGTH_SHORT).show()
            }
        }
    }
    // --- MÉTODOS PARA MOSTRAR LOS DIÁLOGOS ---

    private fun mostrarDialogoFormularioPelicula(pelicula: Peliculas?) {
        val isEditing = pelicula != null
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_form_pelicula, null)

        val etTitulo = dialogView.findViewById<TextInputEditText>(R.id.et_titulo_pelicula)
        val etDescripcion = dialogView.findViewById<TextInputEditText>(R.id.et_descripcion_pelicula)
        val etAnio = dialogView.findViewById<TextInputEditText>(R.id.et_anio_pelicula)
        val etDuracion = dialogView.findViewById<TextInputEditText>(R.id.et_duracion_pelicula)
        val etDirector = dialogView.findViewById<TextInputEditText>(R.id.et_director_pelicula)
        val etImageUrl = dialogView.findViewById<TextInputEditText>(R.id.et_image_url_pelicula)
        val etTrailerUrl = dialogView.findViewById<TextInputEditText>(R.id.et_trailer_url_pelicula)
        val etFechaLanzamiento = dialogView.findViewById<TextInputEditText>(R.id.et_fecha_lanzamiento_pelicula)
        val actvGenero = dialogView.findViewById<AutoCompleteTextView>(R.id.actv_genero_pelicula)

        val generoNombres = allGenerosList.map { it.nombre }
        val generoAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, generoNombres)
        actvGenero.setAdapter(generoAdapter)

        etFechaLanzamiento.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val monthFormatted = String.format("%02d", selectedMonth + 1)
                    val dayFormatted = String.format("%02d", selectedDay)
                    etFechaLanzamiento.setText("$selectedYear-$monthFormatted-$dayFormatted")
                }, year, month, day)
            datePickerDialog.show()
        }

        if (isEditing) {
            etTitulo.setText(pelicula?.titulo)
            etDescripcion.setText(pelicula?.descripcion)
            etAnio.setText(pelicula?.anio.toString())
            etDuracion.setText(pelicula?.duracionMinutos.toString())
            etDirector.setText(pelicula?.director)
            etImageUrl.setText(pelicula?.imageUrl) // ¡Usar imageUrl!
            etTrailerUrl.setText(pelicula?.trailerUrl)
            etFechaLanzamiento.setText(pelicula?.fechaLanzamiento)

            pelicula?.generoId?.let { id ->
                val selectedGenero = allGenerosList.find { it.id == id }
                actvGenero.setText(selectedGenero?.nombre, false)
            }
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(if (isEditing) "Editar Película" else "Crear Nueva Película")
            .setView(dialogView)
            .setPositiveButton(if (isEditing) "Guardar Cambios" else "Crear") { dialog, _ ->
                val titulo = etTitulo.text.toString().trim()
                val descripcion = etDescripcion.text.toString().trim()
                val anioStr = etAnio.text.toString().trim()
                val duracionStr = etDuracion.text.toString().trim()
                val director = etDirector.text.toString().trim()
                val imageUrl = etImageUrl.text.toString().trim()
                val trailerUrl = etTrailerUrl.text.toString().trim()
                val fechaLanzamiento = etFechaLanzamiento.text.toString().trim()

                if (titulo.isEmpty() || anioStr.isEmpty() || duracionStr.isEmpty() || director.isEmpty() || actvGenero.text.toString().trim().isEmpty()) {
                    Toast.makeText(this, "Por favor, completa los campos obligatorios.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Usar toIntOrNull para anio y duracion
                val anio = anioStr.toIntOrNull()
                val duracion = duracionStr.toIntOrNull()

                if (anio == null || duracion == null) {
                    Toast.makeText(this, "Año y Duración deben ser números válidos.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val generoSeleccionadoNombre = actvGenero.text.toString().trim()
                val generoSeleccionado = allGenerosList.find { it.nombre == generoSeleccionadoNombre }
                val generoId = generoSeleccionado?.id

                if (generoId == null) {
                    Toast.makeText(this, "Selecciona un género válido.", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val nuevaPelicula = Peliculas(
                    id = pelicula?.id,
                    titulo = titulo,
                    descripcion = descripcion,
                    anio = anio,
                    director = director,
                    duracionMinutos = duracion,
                    generoId = generoId,
                    imageUrl = imageUrl.ifEmpty { null }, // ¡Usar imageUrl!
                    trailerUrl = trailerUrl.ifEmpty { null },
                    fechaLanzamiento = fechaLanzamiento.ifEmpty { null }
                    // NO ES NECESARIO favoriteCount aquí, se inicializa por defecto en el modelo a 0
                    // y es actualizado por Cloud Functions
                )

                if (isEditing && pelicula != null) {
                    actualizarPelicula(pelicula.id!!, nuevaPelicula)
                } else {
                    crearPelicula(nuevaPelicula)
                }
                dialog.dismiss()
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