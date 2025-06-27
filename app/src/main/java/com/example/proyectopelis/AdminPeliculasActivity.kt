package com.example.proyectopelis // Asegúrate de que este sea tu paquete correcto

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth

class AdminPeliculasActivity : AppCompatActivity() {

    // Declara las vistas aquí para que sean accesibles en toda la clase
    // Esto es útil si vas a acceder a ellas varias veces
    private lateinit var toolbar: MaterialToolbar
    private lateinit var cardTotalMovies: MaterialCardView
    private lateinit var tvTotalMovies: TextView
    private lateinit var cardTotalUsers: MaterialCardView
    private lateinit var tvTotalUsers: TextView
    private lateinit var btnAddNewMovie: MaterialCardView
    private lateinit var btnManageMovies: MaterialCardView
    private lateinit var btnManageUsers: MaterialCardView
    private lateinit var btnManageCategories: MaterialCardView
    private lateinit var btnReports: MaterialCardView
    // Puedes añadir más si incluyes el RecyclerView para actividad reciente
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient // Declara el cliente de Google Sign-In

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paneladmin) // Asegúrate de que este es el layout correcto

        auth = FirebaseAuth.getInstance()

        // Inicializa GoogleSignInClient AQUÍ en onCreate (esto está bien)
        // Pero NO llames a signOut() directamente aquí.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Asocia el botón de cerrar sesión
        val btnLogout: Button = findViewById(R.id.btnLogout)

        // *** LA LÓGICA DE CERRAR SESIÓN DEBE ESTAR DENTRO DEL LISTENER DEL BOTÓN ***
        btnLogout.setOnClickListener {
            // 1. Cerrar sesión de Firebase Authentication
            auth.signOut()

            // 2. Cerrar sesión de Google Sign-In
            // Esto es crucial para que Google muestre el selector de cuentas la próxima vez.
            // Añadimos un listener para saber cuándo ha terminado Google Sign-Out
            googleSignInClient.signOut().addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Error al cerrar sesión de Google: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }

                // 3. Redirigir al LoginActivity y limpiar el historial de actividades
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish() // Finaliza AdminPeliculasActivity para que no se pueda volver atrás
            }
        }




        // 1. Inicializar vistas
        // Asocia las variables Kotlin con los IDs de las vistas en tu XML
        toolbar = findViewById(R.id.toolbar)
        cardTotalMovies = findViewById(R.id.cardTotalMovies)
        tvTotalMovies = findViewById(R.id.tvTotalMovies)
        cardTotalUsers = findViewById(R.id.cardTotalUsers)
        tvTotalUsers = findViewById(R.id.tvTotalUsers)
        btnAddNewMovie = findViewById(R.id.btnAddNewMovie)
        btnManageMovies = findViewById(R.id.btnManageMovies)
        btnManageUsers = findViewById(R.id.btnManageUsers)
        btnManageCategories = findViewById(R.id.btnManageCategories)
        btnReports = findViewById(R.id.btnReports)

        // 2. Configurar la Toolbar (Opcional, pero recomendado para el Appbar)
        setSupportActionBar(toolbar) // Esto configura la MaterialToolbar como la ActionBar de la actividad
        supportActionBar?.title = "Panel de Administración" // Establece el título si no lo haces en el XML
        // Si tienes un Navigation Drawer, el listener para el icono de menú iría aquí:
        toolbar.setNavigationOnClickListener {
            // Lógica para abrir el Navigation Drawer
            // Por ejemplo: drawerLayout.openDrawer(GravityCompat.START)
        }

        // 3. Actualizar datos en las tarjetas de resumen
        // Aquí es donde obtendrías datos reales de Firebase o tu base de datos
        tvTotalMovies.text = "250" // Ejemplo: Simula datos
        tvTotalUsers.text = "870"  // Ejemplo: Simula datos

        // 4. Configurar Click Listeners para los botones/tarjetas de acción
        btnAddNewMovie.setOnClickListener {
            // Lógica para ir a la actividad de añadir nueva película
            val intent = Intent(this, AddNewMovieActivity::class.java) // Asume que tienes AddNewMovieActivity
            startActivity(intent)
        }

        btnManageMovies.setOnClickListener {
            // Lógica para ir a la actividad de gestionar películas
            val intent = Intent(this, ManageMoviesActivity::class.java) // Asume que tienes ManageMoviesActivity
            startActivity(intent)
        }

        btnManageUsers.setOnClickListener {
            // Lógica para ir a la actividad de gestionar usuarios
            val intent = Intent(this, ManageUsersActivity::class.java) // Asume que tienes ManageUsersActivity
            startActivity(intent)
        }

        btnManageCategories.setOnClickListener {
            // Lógica para ir a la actividad de gestionar categorías
            val intent = Intent(this, ManageCategoriesActivity::class.java) // Asume que tienes ManageCategoriesActivity
            startActivity(intent)
        }

        btnReports.setOnClickListener {
            // Lógica para ir a la actividad de reportes
            val intent = Intent(this, ReportsActivity::class.java) // Asume que tienes ReportsActivity
            startActivity(intent)
        }

        // Puedes añadir más lógica aquí, como cargar datos de Firebase
    }

    // Si necesitas sobreescribir métodos del ciclo de vida (onStart, onResume, etc.)
    // override fun onStart() {
    //     super.onStart()
    //     // Lógica aquí
    // }
}