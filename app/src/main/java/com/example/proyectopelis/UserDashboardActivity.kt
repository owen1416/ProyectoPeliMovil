package com.example.proyectopelis

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.auth.FirebaseAuth

class UserDashboardActivity  : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_userdashboard)

        var auth = FirebaseAuth.getInstance()
        // 📍 1. Barra de búsqueda
        val searchBar = findViewById<EditText>(R.id.searchBar)

        // 📍 2. Carrusel de banners
        val bannerViewPager = findViewById<ViewPager2>(R.id.bannerViewPager)

        // Asocia el botón de cerrar sesión
        val btnLogout: Button = findViewById(R.id.btnLogout)

        // *** LA LÓGICA DE CERRAR SESIÓN DEBE ESTAR DENTRO DEL LISTENER DEL BOTÓN ***
        btnLogout.setOnClickListener {
            // 1. Cerrar sesión de Firebase Authentication
            auth.signOut()

            // 📍 3. Pelis recientes
            val recyclerNuevasPelis = findViewById<RecyclerView>(R.id.recyclerNuevasPelis)
            recyclerNuevasPelis.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)


// 📍 4. Películas por categoría
            val actionRecycler = findViewById<RecyclerView>(R.id.actionRecycler)
            val comediaRecycler = findViewById<RecyclerView>(R.id.comediaRecycler)
            val dramaRecycler = findViewById<RecyclerView>(R.id.dramaRecycler)
            val terrorRecycler = findViewById<RecyclerView>(R.id.terrorRecycler)


            actionRecycler.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            comediaRecycler.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            dramaRecycler.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            terrorRecycler.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)


            // 📍 5. Botones de Ver más
            findViewById<Button>(R.id.btnVerMasAccion).setOnClickListener {
                val intent = Intent(this, ListaPeliculasActivity::class.java)
                intent.putExtra("genero", "Acción")
                startActivity(intent)
            }

            findViewById<Button>(R.id.btnVerMasComedia).setOnClickListener {
                val intent = Intent(this, ListaPeliculasActivity::class.java)
                intent.putExtra("genero", "Comedia")
                startActivity(intent)
            }

            findViewById<Button>(R.id.btnVerMasDrama).setOnClickListener {
                val intent = Intent(this, ListaPeliculasActivity::class.java)
                intent.putExtra("genero", "Drama")
                startActivity(intent)
            }

            findViewById<Button>(R.id.btnVerMasTerror).setOnClickListener {
                val intent = Intent(this, ListaPeliculasActivity::class.java)
                intent.putExtra("genero", "Terror")
                startActivity(intent)
            }
        }
    }}
