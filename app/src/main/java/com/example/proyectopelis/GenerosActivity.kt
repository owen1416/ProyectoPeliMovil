package com.example.proyectopelis

import android.content.ContentValues.TAG
import android.os.Bundle
import android.text.Editable // Nueva importación
import android.text.TextWatcher // Nueva importación
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.example.proyectopelis.Adapters.GenerosAdapter
import com.example.proyectopelis.Adapters.OnGeneroActionListener
import com.example.proyectopelis.Models.Generos
import android.app.AlertDialog
import android.view.LayoutInflater

import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.TextInputEditText // Asegúrate de tener esta importación

class GenerosActivity : AppCompatActivity(), OnGeneroActionListener {

    private lateinit var db: FirebaseFirestore
    private lateinit var generosAdapter: GenerosAdapter
    private lateinit var rvGeneros: RecyclerView
    private lateinit var btnCrearGenero: Button
    private lateinit var btnRecargarLista: Button

    // Nuevas propiedades para la búsqueda
    private lateinit var etSearchGenero: TextInputEditText // Usa TextInputEditText
    private var allGenerosList: List<Generos> = emptyList() // Lista para almacenar todos los géneros

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generos)

        db = Firebase.firestore

        rvGeneros = findViewById(R.id.rv_generos)
        btnCrearGenero = findViewById(R.id.btn_crear_genero)
        btnRecargarLista = findViewById(R.id.btn_leer_generos)

        // Inicializar el campo de búsqueda
        etSearchGenero = findViewById(R.id.et_search_genero)

        generosAdapter = GenerosAdapter(emptyList(), this)
        rvGeneros.layoutManager = LinearLayoutManager(this)
        rvGeneros.adapter = generosAdapter

        btnCrearGenero.setOnClickListener {
            mostrarDialogoCrearGenero()
        }

        btnRecargarLista.setOnClickListener {
            leerGeneros() // Llama a leerGeneros para cargar la lista completa y luego aplicar filtro si hay texto
        }

        // Listener para el campo de búsqueda
        etSearchGenero.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No se necesita implementación aquí
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // No se necesita implementación aquí
            }

            override fun afterTextChanged(s: Editable?) {
                filterGeneros(s.toString()) // Llama a la función de filtrado
            }
        })

        leerGeneros() // Cargar los géneros al iniciar la actividad
    }

    // --- FUNCIONES CRUD ---

    private fun crearGenero(nombreGenero: String) {
        val nuevoGenero = Generos(nombre = nombreGenero)
        db.collection("generos")
            .add(nuevoGenero)
            .addOnSuccessListener { documentReference ->
                Log.d(TAG, "Género añadido con ID: ${documentReference.id}")
                Toast.makeText(this, "Género '${nombreGenero}' creado.", Toast.LENGTH_SHORT).show()
                leerGeneros() // Recargar la lista después de crear
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error al añadir género", e)
                Toast.makeText(this, "Error al crear género: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun leerGeneros() {
        db.collection("generos")
            .get()
            .addOnSuccessListener { queryDocumentSnapshots ->
                val generosList = mutableListOf<Generos>()
                for (document in queryDocumentSnapshots.documents) {
                    val genero = document.toObject(Generos::class.java)
                    genero?.let {
                        it.id = document.id
                        generosList.add(it)
                    }
                }
                allGenerosList = generosList // Almacena la lista completa
                filterGeneros(etSearchGenero.text.toString()) // Aplica el filtro si ya hay texto
                Log.d(TAG, "Géneros cargados: ${allGenerosList.size}")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error al obtener géneros", e)
                Toast.makeText(this, "Error al cargar géneros: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun actualizarNombreGenero(generoId: String, nuevoNombre: String) {
        db.collection("generos").document(generoId)
            .update("nombre", nuevoNombre)
            .addOnSuccessListener {
                Log.d(TAG, "Género $generoId actualizado a $nuevoNombre")
                Toast.makeText(this, "Género actualizado.", Toast.LENGTH_SHORT).show()
                leerGeneros() // Recargar la lista después de actualizar
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error al actualizar género", e)
                Toast.makeText(this, "Error al actualizar género: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun eliminarGenero(generoId: String) {
        db.collection("generos").document(generoId)
            .delete()
            .addOnSuccessListener {
                Log.d(TAG, "Género $generoId eliminado.")
                Toast.makeText(this, "Género eliminado.", Toast.LENGTH_SHORT).show()
                leerGeneros() // Recargar la lista después de eliminar
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error al eliminar género", e)
                Toast.makeText(this, "Error al eliminar género: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // --- IMPLEMENTACIÓN DE LA INTERFAZ OnGeneroActionListener ---

    override fun onEditClick(genero: Generos) {
        mostrarDialogoEditarGenero(genero)
    }

    override fun onDeleteClick(genero: Generos) {
        mostrarDialogoConfirmarEliminacion(genero)
    }

    // --- NUEVA FUNCIÓN DE FILTRADO ---
    private fun filterGeneros(query: String) {
        val filteredList = if (query.isEmpty()) {
            allGenerosList // Si la búsqueda está vacía, muestra toda la lista
        } else {
            allGenerosList.filter {
                it.nombre?.contains(query, ignoreCase = true) == true
            }
        }
        generosAdapter.updateGeneros(filteredList)
    }

    // --- MÉTODOS PARA MOSTRAR LOS DIÁLOGOS (sin cambios) ---
    // ... (Mantén aquí las implementaciones de mostrarDialogoCrearGenero, mostrarDialogoEditarGenero, mostrarDialogoConfirmarEliminacion)
    // No he incluido el código completo de los diálogos para mantener esta respuesta concisa, pero úsalos tal cual los tienes ahora.
    private fun mostrarDialogoCrearGenero() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_input_text, null)
        val tilGeneroNombre = dialogView.findViewById<TextInputLayout>(R.id.til_dialog_input)
        val etGeneroNombre = dialogView.findViewById<EditText>(R.id.et_dialog_input)

        MaterialAlertDialogBuilder(this)
            .setTitle("Crear Nuevo Género")
            .setView(dialogView)
            .setPositiveButton("Crear") { dialog, _ ->
                val nombre = etGeneroNombre.text.toString().trim()
                if (nombre.isNotEmpty()) {
                    crearGenero(nombre)
                } else {
                    Toast.makeText(this, "El nombre del género no puede estar vacío.", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ -> dialog.cancel() }
            .show()

        tilGeneroNombre.hint = "Nombre del Género"
        tilGeneroNombre.counterMaxLength = 30
        tilGeneroNombre.isCounterEnabled = true
    }

    private fun mostrarDialogoEditarGenero(genero: Generos) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_input_text, null)
        val tilGeneroNombre = dialogView.findViewById<TextInputLayout>(R.id.til_dialog_input)
        val etGeneroNombre = dialogView.findViewById<EditText>(R.id.et_dialog_input)

        etGeneroNombre.setText(genero.nombre)

        MaterialAlertDialogBuilder(this)
            .setTitle("Editar Género")
            .setView(dialogView)
            .setPositiveButton("Actualizar") { dialog, _ ->
                val nuevoNombre = etGeneroNombre.text.toString().trim()
                if (nuevoNombre.isNotEmpty()) {
                    genero.id?.let { id ->
                        actualizarNombreGenero(id, nuevoNombre)
                    } ?: Toast.makeText(this, "Error: ID de género no encontrado.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "El nombre del género no puede estar vacío.", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ -> dialog.cancel() }
            .show()

        tilGeneroNombre.hint = "Nombre del Género"
        tilGeneroNombre.counterMaxLength = 30
        tilGeneroNombre.isCounterEnabled = true
    }

    private fun mostrarDialogoConfirmarEliminacion(genero: Generos) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Eliminar Género")
            .setMessage("¿Estás seguro de que quieres eliminar el género '${genero.nombre}'?")
            .setPositiveButton("Eliminar") { dialog, _ ->
                genero.id?.let { id ->
                    eliminarGenero(id)
                } ?: Toast.makeText(this, "Error: ID de género no encontrado.", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ -> dialog.cancel() }
            .show()
    }
}