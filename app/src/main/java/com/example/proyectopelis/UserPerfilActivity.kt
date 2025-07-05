package com.example.proyectopelis

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.proyectopelis.databinding.ActivityPerfilBinding
import com.google.firebase.auth.FirebaseAuth

class UserPerfilActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPerfilBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPerfilBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        val currentUser = auth.currentUser
        binding.textUserName.text = "Username: ${currentUser?.displayName ?: currentUser?.email}"

        // 🔽 Configura el menú inferior
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> {
                    startActivity(Intent(this, UserDashboardActivity::class.java))
                    true
                }
                R.id.nav_generos -> {
                    startActivity(Intent(this, UserCategoryActivity::class.java))
                    true
                }
                R.id.nav_lista -> {
                    startActivity(Intent(this, UserListPeliculasActivity::class.java))
                    true
                }
                R.id.nav_perfil -> true // ya estás aquí
                else -> false
            }
        }

        // 🔽 Botón Cerrar sesión
        binding.optionLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
