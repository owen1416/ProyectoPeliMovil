
package com.example.proyectopelis
import com.example.proyectopelis.RegisterActivity



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
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>
    private lateinit var db: FirebaseFirestore

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
        db = FirebaseFirestore.getInstance()

        etUsuario = findViewById(R.id.etUsuario)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvRegistrarse = findViewById(R.id.tvRegistrarse)
        tvOlvidarContrasena = findViewById(R.id.tvOlvidarContrasena)
        progressBar = findViewById(R.id.progressBar)
        btnGoogleSignIn = findViewById(R.id.btnGoogleSignIn)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

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
                        user?.let { readUserRoleAndRedirect(it.uid) } ?: run {
                            Toast.makeText(this, "Error: No se pudo obtener el usuario.", Toast.LENGTH_SHORT).show()
                        }
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
                    user?.let { readUserRoleAndRedirect(it.uid) } ?: run {
                        Toast.makeText(this, "Error: No se pudo obtener el usuario de Google.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Error de autenticación con Firebase (Google): ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun readUserRoleAndRedirect(uid: String) {
        db.collection("userRoles").document(uid).get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val document = task.result
                    var userRole = "default"

                    if (document != null && document.exists()) {
                        val roleFromFirestore = document.getString("role")
                        if (roleFromFirestore != null) {
                            userRole = roleFromFirestore
                            println("Rol del usuario de Firestore: $userRole para UID: $uid")
                        } else {
                            println("Campo 'role' no encontrado o no es String en el documento para UID: $uid")
                        }
                    } else {
                        println("Documento de usuario no encontrado en 'userRoles' para UID: $uid. Asignando rol por defecto.")
                    }
                    redirectToScreenByRole(userRole)
                } else {
                    println("Error al obtener el rol del usuario de Firestore: ${task.exception?.message}")
                    Toast.makeText(this, "Error al obtener rol de usuario. Redirigiendo a pantalla por defecto.", Toast.LENGTH_LONG).show()
                    redirectToScreenByRole("default")
                }
            }
    }

    private fun redirectToScreenByRole(role: String) {
        val intent: Intent
        when (role) {
            "admin" -> {
                intent = Intent(this, AdminDashboard::class.java)
            }
            "user" -> {
                intent = Intent(this, UserDashboardActivity::class.java)
            }
            else -> {
                intent = Intent(this, UserDashboardActivity::class.java)
            }
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    // ESTA LÓGICA ES LA QUE CAUSA EL AUTO-LOGIN.
    // Coméntala o elimínala si no quieres que el usuario se loguee automáticamente.
    /*
    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            readUserRoleAndRedirect(currentUser.uid)
        }
    }
    */
}