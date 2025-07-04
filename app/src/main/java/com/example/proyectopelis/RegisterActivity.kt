package com.example.proyectopelis

import android.content.Intent
import android.os.Bundle
import android.util.Log // Importar Log para depuración
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.proyectopelis.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth // Este es el SDK de cliente
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
// NO NECESITAS ESTAS IMPORTACIONES DEL ADMIN SDK SI LO VAMOS A QUITAR
// import com.google.firebase.FirebaseApp
// import com.google.firebase.FirebaseOptions
// import java.io.InputStream
// import com.google.firebase.auth.FirebaseAuth as FirebaseAuthAdmin

import com.google.firebase.firestore.FirebaseFirestore // PARA ESCRIBIR EN FIRESTORE CON EL SDK CLIENTE

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth // FirebaseAuth del cliente SDK
    private lateinit var db: FirebaseFirestore // Firestore del cliente SDK

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance() // Inicializa Auth del cliente
        db = FirebaseFirestore.getInstance() // Inicializa Firestore del cliente

        // **HE ELIMINADO TODA LA INICIALIZACIÓN DEL FIREBASE ADMIN SDK DE AQUÍ.**
        // Esto soluciona los errores que tenías con .setCredentials y FirebaseApp.getApps(this).isEmpty()
        // porque ya no intentaremos inicializar el Admin SDK en el cliente.

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
                                    Log.d("Register", "Registro exitoso y perfil actualizado.")
                                    Toast.makeText(this, "Registro exitoso y perfil actualizado.", Toast.LENGTH_LONG).show()

                                    // **NUEVO CÓDIGO: ASIGNAR ROL EN FIRESTORE (AHORA USANDO SOLO EL SDK CLIENTE)**
                                    // HEMOS ELIMINADO LA LLAMADA A setCustomUserClaims DEL ADMIN SDK
                                    // y la inicialización del Admin SDK, que daban los errores.
                                    val userRoleData = mapOf(
                                        "email" to user.email,
                                        "role" to "user", // Asignamos el rol "user" por defecto
                                        "username" to username
                                    )

                                    // Escribir en Firestore usando la instancia 'db' del SDK de cliente
                                    // Asegúrate de que el nombre de la colección ("userRoles") coincide con tu Firestore
                                    // O si tus colecciones son "users" y "admins", podrías querer escribir en "users"
                                    // Por ejemplo: db.collection("users").document(user.uid).set(...)
                                    db.collection("userRoles").document(user.uid).set(userRoleData)
                                        .addOnSuccessListener {
                                            Log.d("Register", "Rol 'user' guardado en Firestore para UID: ${user.uid}")
                                            // Ahora que todo está hecho, redirigir
                                            redirectToLogin()
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("Register", "Error guardando rol en Firestore: ${e.message}", e)
                                            Toast.makeText(this, "Error: No se pudo guardar el rol en Firestore.", Toast.LENGTH_LONG).show()
                                            // Redirigir de todos modos, pero el rol puede no estar disponible
                                            redirectToLogin()
                                        }

                                } else {
                                    Log.e("Register", "Registro exitoso, pero no se pudo actualizar el nombre de usuario.")
                                    Toast.makeText(this, "Registro exitoso, pero no se pudo actualizar el nombre de usuario.", Toast.LENGTH_LONG).show()
                                    redirectToLogin() // Redirigir de todos modos
                                }
                            }
                    } else {
                        Log.e("Register", "Registro exitoso, pero el usuario es nulo.")
                        Toast.makeText(this, "Registro exitoso, pero el usuario es nulo.", Toast.LENGTH_LONG).show()
                        redirectToLogin()
                    }
                } else {
                    val errorMessage = task.exception?.message ?: "Error desconocido al registrar."
                    Log.e("Register", "Error en el registro: $errorMessage")
                    Toast.makeText(this, "Error en el registro: $errorMessage", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}