package com.example.proyectopelis // ASEGÚRATE DE QUE ESTE SEA TU NOMBRE DE PAQUETE REAL

import android.content.Intent // Importa Intent para la redirección
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.proyectopelis.databinding.ActivityRegisterBinding // ASEGÚRATE DE QUE ESTE SEA TU ARCHIVO DE BINDING REAL
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnRegister.setOnClickListener {
            val username = binding.etRegisterUsername.text.toString().trim()
            val email = binding.etRegisterEmail.text.toString().trim()
            val password = binding.etRegisterPassword.text.toString().trim()

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.progressBarRegister.visibility = View.VISIBLE
            binding.btnRegister.isEnabled = false

            registerUserWithEmailAndPassword(username, email, password)
        }
    }

    private fun registerUserWithEmailAndPassword(username: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                binding.progressBarRegister.visibility = View.GONE
                binding.btnRegister.isEnabled = true

                if (task.isSuccessful) {
                    val user: FirebaseUser? = auth.currentUser
                    if (user != null) {
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(username)
                            .build()

                        user.updateProfile(profileUpdates)
                            .addOnCompleteListener { profileTask ->
                                if (profileTask.isSuccessful) {
                                    Toast.makeText(this, "Registro exitoso y perfil actualizado.", Toast.LENGTH_LONG).show()
                                    // REDIRECCIÓN A LOGIN ACTIVITY
                                    val intent = Intent(this, LoginActivity::class.java) // <--- ¡CAMBIO AQUÍ!
                                    startActivity(intent)
                                    finish() // Finaliza RegisterActivity para que el usuario no pueda volver con el botón atrás
                                } else {
                                    Toast.makeText(this, "Registro exitoso, pero no se pudo actualizar el nombre de usuario.", Toast.LENGTH_LONG).show()
                                    // Si no se puede actualizar el perfil, aún así puedes redirigir
                                    val intent = Intent(this, LoginActivity::class.java) // <--- ¡CAMBIO AQUÍ!
                                    startActivity(intent)
                                    finish()
                                }
                            }
                    } else {
                        Toast.makeText(this, "Registro exitoso, pero el usuario es nulo.", Toast.LENGTH_LONG).show()
                        val intent = Intent(this, LoginActivity::class.java) // <--- ¡CAMBIO AQUÍ!
                        startActivity(intent)
                        finish()
                    }
                } else {
                    val errorMessage = task.exception?.message ?: "Error desconocido al registrar."
                    Toast.makeText(this, "Error en el registro: $errorMessage", Toast.LENGTH_LONG).show()
                }
            }
    }

    // Opcional: Esto se ejecuta al inicio de la actividad.
    // Si ya hay un usuario logueado, podrías redirigirlo automáticamente,
    // pero si esta es una actividad de REGISTRO, normalmente no lo harías
    // a menos que sea una pantalla de inicio donde se maneje tanto el login como el registro.
    // Si la intención de esta actividad es *solo* el registro, esta parte no es estrictamente necesaria.
    /*
    override fun onStart() {
        super.onStart()
        val currentUser: FirebaseUser? = auth.currentUser
        if (currentUser != null) {
            // Usuario ya autenticado, redirigir a la pantalla principal (MainActivity)
            // o a la pantalla de login si el flujo lo requiere.
            // val intent = Intent(this, MainActivity::class.java)
            // startActivity(intent)
            // finish()
        }
    }
    */
}