package com.example.proyectopelis

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore // Importar Firestore si lo vas a usar

import java.util.concurrent.TimeUnit

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore // Instancia de Firestore (si lo usas para guardar datos adicionales)

    // Vistas necesarias para el registro
    private lateinit var etRegisterUsername: EditText
    private lateinit var etPhoneNumber: EditText
    private lateinit var etRegisterPassword: EditText // Contraseña
    private lateinit var etRegisterConfirmPassword: EditText // Confirmar contraseña
    private lateinit var etVerificationCode: EditText
    private lateinit var btnSendCode: Button
    private lateinit var btnVerifyCode: Button
    private lateinit var progressBarRegister: ProgressBar

    // Variables para la autenticación de teléfono
    private var verificationId: String? = null
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance() // Inicializar Firestore

        // Inicialización de vistas
        etRegisterUsername = findViewById(R.id.etRegisterUsername)
        etPhoneNumber = findViewById(R.id.etPhoneNumber)
        etRegisterPassword = findViewById(R.id.etRegisterPassword)
        etRegisterConfirmPassword = findViewById(R.id.etRegisterConfirmPassword)
        etVerificationCode = findViewById(R.id.etVerificationCode)
        btnSendCode = findViewById(R.id.btnSendCode)
        btnVerifyCode = findViewById(R.id.btnVerifyCode)
        progressBarRegister = findViewById(R.id.progressBarRegister)

        // Listeners para los botones
        btnSendCode.setOnClickListener {
            sendVerificationCode()
        }

        btnVerifyCode.setOnClickListener {
            verifyCode()
        }
    }

    // Método para enviar el código de verificación al número de teléfono y validar contraseña
    private fun sendVerificationCode() {
        val username = etRegisterUsername.text.toString().trim()
        val phoneNumber = etPhoneNumber.text.toString().trim()
        val password = etRegisterPassword.text.toString()
        val confirmPassword = etRegisterConfirmPassword.text.toString()

        // 1. Validar campos (incluyendo contraseña)
        if (username.isEmpty()) {
            etRegisterUsername.error = "El nombre de usuario es requerido."
            etRegisterUsername.requestFocus()
            return
        }
        if (phoneNumber.isEmpty() || !isValidPhoneNumber(phoneNumber)) {
            etPhoneNumber.error = "Ingresa un número de teléfono válido (ej. +519XXXXXXXX)."
            etPhoneNumber.requestFocus()
            return
        }
        if (password.isEmpty() || password.length < 6) { // Mínimo 6 caracteres
            etRegisterPassword.error = "La contraseña debe tener al menos 6 caracteres."
            etRegisterPassword.requestFocus()
            return
        }
        if (password != confirmPassword) {
            etRegisterConfirmPassword.error = "Las contraseñas no coinciden."
            etRegisterConfirmPassword.requestFocus()
            return
        }

        // 2. Mostrar progreso y deshabilitar botón
        progressBarRegister.visibility = View.VISIBLE
        btnSendCode.isEnabled = false

        // 3. Iniciar verificación de teléfono con Firebase
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(phoneCallbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    // Método para verificar el código OTP ingresado por el usuario
    private fun verifyCode() {
        val code = etVerificationCode.text.toString().trim()

        if (code.isEmpty()) {
            etVerificationCode.error = "Ingresa el código de verificación."
            etVerificationCode.requestFocus()
            return
        }

        progressBarRegister.visibility = View.VISIBLE
        btnVerifyCode.isEnabled = false // Deshabilita el botón mientras se verifica

        if (verificationId != null) {
            val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
            signInWithPhoneAuthCredential(credential)
        } else {
            Toast.makeText(this, "Error: No se ha enviado el código de verificación.", Toast.LENGTH_SHORT).show()
            progressBarRegister.visibility = View.GONE
            btnVerifyCode.isEnabled = true
        }
    }

    // Método para autenticar con Firebase usando la credencial del teléfono
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                progressBarRegister.visibility = View.GONE
                btnVerifyCode.isEnabled = true // Habilitar de nuevo el botón de verificar

                if (task.isSuccessful) {
                    // Autenticación de teléfono exitosa
                    val user = auth.currentUser
                    val username = etRegisterUsername.text.toString().trim()
                    val phoneNumber = etPhoneNumber.text.toString().trim()
                    val password = etRegisterPassword.text.toString() // La contraseña ingresada

                    if (user != null) {
                        // AQUÍ ES DONDE GUARDAS LOS DATOS ADICIONALES (nombre, contraseña) EN TU BASE DE DATOS
                        val userData = hashMapOf(
                            "username" to username,
                            "phoneNumber" to phoneNumber,
                            "password" to password // Considera encriptar o hashear la contraseña antes de guardar
                            // Añade otros datos que necesites
                        )

                        // Ejemplo: Guardar en Firestore usando el UID de Firebase como ID del documento
                        db.collection("users")
                            .document(user.uid) // Usa el UID de Firebase como ID del documento
                            .set(userData)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Registro exitoso y datos guardados.", Toast.LENGTH_SHORT).show()
                                navigateToAdminPeliculas()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error al guardar datos adicionales: ${e.message}", Toast.LENGTH_LONG).show()
                                // Aquí podrías considerar borrar el usuario de Firebase Auth si el guardado de datos falla
                                // user.delete()
                                navigateToAdminPeliculas() // Navega de todas formas o maneja el error
                            }
                    } else {
                        Toast.makeText(this, "Error: Usuario autenticado nulo.", Toast.LENGTH_SHORT).show()
                    }

                } else {
                    // Falló la autenticación con el código
                    val errorMessage = task.exception?.message ?: "Error de autenticación."
                    Toast.makeText(this, "Error de verificación: $errorMessage", Toast.LENGTH_LONG).show()
                }
            }
    }

    // Callbacks para PhoneAuthProvider para manejar los estados de la verificación
    private val phoneCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // Este método se llama automáticamente si la verificación es instantánea (ej. Auto-retrieval)
            signInWithPhoneAuthCredential(credential)
            progressBarRegister.visibility = View.GONE
        }

        override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
            // Se llama cuando la verificación falla (ej. número inválido, cuota excedida)
            progressBarRegister.visibility = View.GONE
            btnSendCode.isEnabled = true // Habilitar el botón de enviar código
            Toast.makeText(this@RegisterActivity, "Verificación fallida: ${e.message}", Toast.LENGTH_LONG).show()
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            // Se llama cuando el código de verificación ha sido enviado al teléfono del usuario
            this@RegisterActivity.verificationId = verificationId
            this@RegisterActivity.resendToken = token // Guarda el token para posibles reenvíos

            progressBarRegister.visibility = View.GONE
            btnSendCode.isEnabled = true // El botón de enviar código se puede presionar de nuevo para reenviar

            // Deshabilitar los campos de número y contraseña, y mostrar los campos del código
            etPhoneNumber.isEnabled = false
            etRegisterPassword.isEnabled = false
            etRegisterConfirmPassword.isEnabled = false
            btnSendCode.text = "Reenviar Código" // Cambiar texto para indicar reenvío

            etVerificationCode.visibility = View.VISIBLE
            btnVerifyCode.visibility = View.VISIBLE

            Toast.makeText(this@RegisterActivity, "Código enviado al número.", Toast.LENGTH_SHORT).show()
        }
    }

    // Función auxiliar para validar el formato básico del número de teléfono
    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        return phoneNumber.startsWith("+") && phoneNumber.length >= 10
    }

    // Navega a la actividad principal después de un registro/login exitoso
    private fun navigateToAdminPeliculas() {
        val intent = Intent(this, AdminPeliculasActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}