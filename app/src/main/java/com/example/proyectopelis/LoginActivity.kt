package com.example.proyectopelis

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    private lateinit var etUsuario: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var tvRegistrarse: TextView
    private lateinit var tvOlvidarContrasena: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnGoogleSignIn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        // Asocia las vistas con sus IDs del XML
        etUsuario = findViewById(R.id.etUsuario)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegistrarse = findViewById(R.id.tvRegistrarse)
        tvOlvidarContrasena = findViewById(R.id.tvOlvidarContrasena)
        progressBar = findViewById(R.id.progressBar)
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn)

        // 1. Configurar Google Sign-In Options (GSO)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // 2. Inicializar ActivityResultLauncher para manejar el resultado de la ventana de Google
        googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                progressBar.visibility = View.GONE
                btnLogin.isEnabled = true
                btnGoogleSignIn.isEnabled = true
                Toast.makeText(this, "Fallo de inicio de sesión con Google: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        // --- Listeners para los botones ---

        btnLogin.setOnClickListener {
            val email = etUsuario.text.toString().trim()
            val password = etPassword.text.toString()

            if (email.isEmpty()) {
                etUsuario.error = "El correo electrónico es requerido."
                etUsuario.requestFocus()
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                etPassword.error = "La contraseña es requerida."
                etPassword.requestFocus()
                return@setOnClickListener
            }

            progressBar.visibility = View.VISIBLE
            btnLogin.isEnabled = false
            btnGoogleSignIn.isEnabled = false

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    progressBar.visibility = View.GONE
                    btnLogin.isEnabled = true
                    btnGoogleSignIn.isEnabled = true

                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        Toast.makeText(this, "Bienvenido, ${user?.email}", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, AdminPeliculasActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        val errorMessage = when (task.exception) {
                            is FirebaseAuthInvalidUserException -> "Usuario no registrado o deshabilitado."
                            is FirebaseAuthInvalidCredentialsException -> "Contraseña incorrecta."
                            else -> "Error de inicio de sesión: ${task.exception?.message}"
                        }
                        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                    }
                }
        }

        tvRegistrarse.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        tvOlvidarContrasena.setOnClickListener {
            Toast.makeText(this, "Funcionalidad de recuperación de contraseña (próximamente)", Toast.LENGTH_SHORT).show()
        }

        btnGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun signInWithGoogle() {
        progressBar.visibility = View.VISIBLE
        btnLogin.isEnabled = false
        btnGoogleSignIn.isEnabled = false

        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                progressBar.visibility = View.GONE
                btnLogin.isEnabled = true
                btnGoogleSignIn.isEnabled = true

                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Toast.makeText(this, "Bienvenido con Google, ${user?.displayName ?: user?.email}", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, AdminPeliculasActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Error de autenticación con Firebase (Google): ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    // ESTA LÓGICA ES LA QUE PERMITE EL AUTO-LOGIN SI YA HAY UNA SESIÓN ACTIVA.
    // Es deseable para una buena UX, pero debes entender que hace que no veas el Login
    // si ya hay un usuario logueado.
    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(this, AdminPeliculasActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}