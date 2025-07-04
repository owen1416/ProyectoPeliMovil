package com.example.proyectopelis // Asegúrate que el paquete sea el correcto

import android.app.AlertDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectopelis.Adapters.UserAdapter
import com.example.proyectopelis.Models.UserRole
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class UserManagementActivity : AppCompatActivity(), UserAdapter.OnUserActionListener {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var rvUsers: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private lateinit var toolbar: Toolbar
    private lateinit var btnCrearUsuario: Button
    private lateinit var btnRecargarUsuarios: Button
    private lateinit var etSearchUser: TextInputEditText

    private var allUsersList: MutableList<UserRole> = mutableListOf()
    private var currentUserRole: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        Log.d("UserManagementDebug", "onCreate: Actividad de Gestión de Usuarios iniciada.")

        db = Firebase.firestore
        auth = FirebaseAuth.getInstance()

        rvUsers = findViewById(R.id.rv_users)
        toolbar = findViewById(R.id.toolbar_user_management)
        btnCrearUsuario = findViewById(R.id.btn_crear_usuario)
        btnRecargarUsuarios = findViewById(R.id.btn_recargar_usuarios)
        etSearchUser = findViewById(R.id.et_search_user)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Gestión de Usuarios"

        userAdapter = UserAdapter(allUsersList, this)
        rvUsers.layoutManager = LinearLayoutManager(this)
        rvUsers.adapter = userAdapter
        Log.d("UserManagementDebug", "onCreate: RecyclerView y Adapter configurados.")

        btnCrearUsuario.setOnClickListener {
            showUserFormDialog(null)
        }

        btnRecargarUsuarios.setOnClickListener {
            loadUsers()
            Toast.makeText(this, "Usuarios recargados", Toast.LENGTH_SHORT).show()
            Log.d("UserManagementDebug", "onCreate: Botón 'Recargar usuarios' presionado.")
        }

        etSearchUser.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                userAdapter.filter(s.toString())
                Log.d("UserManagementDebug", "onCreate: Texto de búsqueda cambiado a: '${s.toString()}'")
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        checkCurrentUserRoleAndLoadUsers()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            Log.d("UserManagementDebug", "onOptionsItemSelected: Botón de volver presionado.")
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun checkCurrentUserRoleAndLoadUsers() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            db.collection("userRoles").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    currentUserRole = document.getString("role") ?: "user"
                    Log.d("UserManagementDebug", "checkCurrentUserRoleAndLoadUsers: Rol del usuario actual: $currentUserRole, UID: $userId")
                    if (currentUserRole == "admin") {
                        loadUsers()
                        btnCrearUsuario.visibility = View.VISIBLE
                        btnRecargarUsuarios.visibility = View.VISIBLE
                        etSearchUser.visibility = View.VISIBLE
                    } else {
                        Toast.makeText(this, "No tienes permisos de administrador para gestionar usuarios.", Toast.LENGTH_LONG).show()
                        Log.w("UserManagementDebug", "checkCurrentUserRoleAndLoadUsers: Usuario no es admin, acceso denegado a gestión de usuarios.")
                        btnCrearUsuario.visibility = View.GONE
                        btnRecargarUsuarios.visibility = View.GONE
                        etSearchUser.visibility = View.GONE
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("UserManagementDebug", "checkCurrentUserRoleAndLoadUsers: Error al obtener el rol del usuario: ${e.message}", e)
                    Toast.makeText(this, "Error al verificar permisos: ${e.message}", Toast.LENGTH_LONG).show()
                    btnCrearUsuario.visibility = View.GONE
                    btnRecargarUsuarios.visibility = View.GONE
                    etSearchUser.visibility = View.GONE
                }
        } else {
            currentUserRole = "guest"
            Log.d("UserManagementDebug", "checkCurrentUserRoleAndLoadUsers: No hay usuario logueado.")
            Toast.makeText(this, "Debes iniciar sesión para acceder a la gestión de usuarios.", Toast.LENGTH_LONG).show()
            btnCrearUsuario.visibility = View.GONE
            btnRecargarUsuarios.visibility = View.GONE
            etSearchUser.visibility = View.GONE
        }
    }

    private fun loadUsers() {
        if (currentUserRole != "admin") {
            Log.w("UserManagementDebug", "loadUsers: Intento de cargar usuarios sin ser admin. Operación abortada.")
            return
        }

        Log.d("UserManagementDebug", "loadUsers: Iniciando carga de usuarios desde Firestore.")
        val currentUserUid = auth.currentUser?.uid
        Log.d("UserManagementDebug", "loadUsers: UID del usuario actual logueado: $currentUserUid")

        db.collection("userRoles")
            .get()
            .addOnSuccessListener { result ->
                Log.d("UserManagementDebug", "loadUsers: Consulta a Firestore exitosa.")
                allUsersList.clear()
                var documentsProcessed = 0

                if (result.isEmpty) {
                    Log.d("UserManagementDebug", "loadUsers: No se encontraron documentos en la colección 'userRoles'.")
                }

                for (document in result) {
                    documentsProcessed++
                    val userRole = document.toObject(UserRole::class.java) // Deserializa el documento a UserRole
                    userRole.uid = document.id // Asigna manualmente el ID del documento al campo 'uid' de UserRole
                    allUsersList.add(userRole)
                    Log.d("UserManagementDebug", "loadUsers: Documento procesado: UID=${userRole.uid}, Email=${userRole.email}, Username=${userRole.username}, Rol=${userRole.role}")
                }

                Log.d("UserManagementDebug", "loadUsers: Total de documentos procesados EN EL BUCLE: $documentsProcessed")
                Log.d("UserManagementDebug", "loadUsers: Tamaño de allUsersList ANTES de pasar al adaptador: ${allUsersList.size}")

                userAdapter.updateUsers(allUsersList)

                Log.d("UserManagementDebug", "loadUsers: Adaptador actualizado. Tamaño de allUsersList DESPUÉS de updateUsers: ${allUsersList.size}")

                if (allUsersList.isEmpty()) {
                    Toast.makeText(this, "No se encontraron usuarios para mostrar.", Toast.LENGTH_SHORT).show()
                }

            }
            .addOnFailureListener { exception ->
                Log.e("UserManagementDebug", "loadUsers: Error al cargar usuarios desde Firestore: ${exception.message}", exception)
                Toast.makeText(this, "Error al cargar usuarios: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun showUserFormDialog(user: UserRole?) {
        val isEditing = user != null
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_user_role, null)

        val tvDialogTitle: TextView = dialogView.findViewById(R.id.tv_dialog_title)
        val tvUserId: TextView = dialogView.findViewById(R.id.tv_dialog_user_id)
        val tilEmail: TextInputLayout = dialogView.findViewById(R.id.til_email)
        val etEmail: TextInputEditText = dialogView.findViewById(R.id.et_dialog_email)
        val tilPassword: TextInputLayout = dialogView.findViewById(R.id.til_password)
        val etPassword: TextInputEditText = dialogView.findViewById(R.id.et_dialog_password)
        val tilUsername: TextInputLayout = dialogView.findViewById(R.id.til_username)
        val etUsername: TextInputEditText = dialogView.findViewById(R.id.et_dialog_username)
        val spinnerRole: Spinner = dialogView.findViewById(R.id.spinner_dialog_role)

        val roles = arrayOf("user", "admin")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRole.adapter = adapter

        if (isEditing) {
            Log.d("UserManagementDebug", "showUserFormDialog: Mostrando diálogo para EDITAR usuario: ${user?.email}")
            tvDialogTitle.text = "Editar Usuario"
            tvUserId.visibility = View.VISIBLE
            tvUserId.text = "ID: ${user?.uid}"
            etEmail.setText(user?.email)
            etUsername.setText(user?.username)
            tilPassword.visibility = View.GONE

            etEmail.isEnabled = false
            etUsername.isEnabled = false

            val currentRoleIndex = roles.indexOf(user?.role)
            if (currentRoleIndex != -1) {
                spinnerRole.setSelection(currentRoleIndex)
            }
        } else {
            Log.d("UserManagementDebug", "showUserFormDialog: Mostrando diálogo para AGREGAR nuevo usuario.")
            tvDialogTitle.text = "Agregar Nuevo Usuario"
            tvUserId.visibility = View.GONE
            tilPassword.visibility = View.VISIBLE
            etEmail.isEnabled = true
            etUsername.isEnabled = true
        }

        AlertDialog.Builder(this)
            .setTitle(tvDialogTitle.text)
            .setView(dialogView)
            .setPositiveButton(if (isEditing) "Guardar Cambios" else "Crear Usuario") { dialog, _ ->
                if (isEditing) {
                    val newRole = spinnerRole.selectedItem.toString()
                    Log.d("UserManagementDebug", "showUserFormDialog: Intentando actualizar rol de ${user?.email} a $newRole")
                    user?.uid?.let { uid ->
                        user?.email?.let { email ->
                            updateUserRole(uid, newRole, email)
                        }
                    }
                } else {
                    val email = etEmail.text.toString().trim()
                    val password = etPassword.text.toString()
                    val username = etUsername.text.toString().trim()
                    val role = spinnerRole.selectedItem.toString()

                    if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
                        Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    if (password.length < 6) {
                        Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres.", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    createNewUser(email, password, username, role)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                Log.d("UserManagementDebug", "showUserFormDialog: Operación cancelada.")
                dialog.cancel()
            }
            .show()
    }

    private fun createNewUser(email: String, password: String, username: String, role: String) {
        Log.d("UserManagementDebug", "createNewUser: Intentando crear usuario: $email con rol: $role")
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    firebaseUser?.let { user ->
                        val userUid = user.uid
                        Log.d("UserManagementDebug", "createNewUser: Usuario de Firebase Auth creado: ${user.email}, UID: $userUid")

                        // Creamos un mapa con los datos para Firestore, EXCLUYENDO el 'uid' como campo
                        val userRoleData = hashMapOf(
                            "email" to email,
                            "username" to username,
                            "role" to role
                        )
                        db.collection("userRoles").document(userUid)
                            .set(userRoleData) // Usamos un mapa en lugar del objeto UserRole
                            .addOnSuccessListener {
                                Toast.makeText(this, "Usuario ${username} creado y rol asignado: $role", Toast.LENGTH_LONG).show()
                                Log.d("UserManagementDebug", "createNewUser: Rol de usuario guardado en Firestore para UID: $userUid")
                                loadUsers()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error al guardar rol en Firestore: ${e.message}", Toast.LENGTH_LONG).show()
                                Log.e("UserManagementDebug", "createNewUser: Error al guardar rol en Firestore para UID: $userUid: ${e.message}", e)
                                user.delete().addOnCompleteListener { deleteTask ->
                                    if (deleteTask.isSuccessful) {
                                        Log.d("UserManagementDebug", "createNewUser: Usuario de Auth eliminado debido a error en Firestore.")
                                    } else {
                                        Log.e("UserManagementDebug", "createNewUser: Error al eliminar usuario de Auth: ${deleteTask.exception?.message}")
                                    }
                                }
                            }
                    }
                } else {
                    val errorMessage = when (task.exception) {
                        is FirebaseAuthUserCollisionException -> "El email ya está registrado."
                        else -> "Error al crear usuario: ${task.exception?.message}"
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                    Log.e("UserManagementDebug", "createNewUser: Fallo al crear usuario en Firebase Auth: ${task.exception?.message}", task.exception)
                }
            }
    }

    override fun onEditRoleClick(user: UserRole) {
        showUserFormDialog(user)
    }

    private fun updateUserRole(userId: String, newRole: String, userEmail: String) {
        if (userId == auth.currentUser?.uid && newRole != "admin") {
            Toast.makeText(this, "No puedes quitarte el rol de admin a ti mismo.", Toast.LENGTH_LONG).show()
            Log.w("UserManagementDebug", "updateUserRole: Intento de admin de quitarse el rol a sí mismo.")
            return
        }

        db.collection("userRoles").document(userId)
            .update("role", newRole)
            .addOnSuccessListener {
                Toast.makeText(this, "Rol de ${userEmail} actualizado a $newRole", Toast.LENGTH_SHORT).show()
                Log.d("UserManagementDebug", "updateUserRole: Rol de $userEmail actualizado con éxito a $newRole.")
                loadUsers()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al actualizar rol: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("UserManagementDebug", "updateUserRole: Error al actualizar rol de $userId: ${e.message}", e)
            }
    }

    override fun onDeleteUserClick(user: UserRole) {
        Log.d("UserManagementDebug", "onDeleteUserClick: Eliminando usuario: ${user.email}")
        if (user.uid == auth.currentUser?.uid) {
            Toast.makeText(this, "No puedes eliminarte a ti mismo.", Toast.LENGTH_LONG).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Confirmar Eliminación")
            .setMessage("¿Estás seguro de que quieres eliminar al usuario ${user.username} (${user.email})? Esta acción eliminará su rol de la base de datos y podría afectar sus permisos.")
            .setPositiveButton("Sí, Eliminar") { dialog, _ ->
                Log.d("UserManagementDebug", "onDeleteUserClick: Confirmación de eliminación para ${user.email}.")
                deleteUserRoleFromFirestore(user.uid, user.email)
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                Log.d("UserManagementDebug", "onDeleteUserClick: Eliminación cancelada para ${user.email}.")
                dialog.cancel()
            }
            .show()
    }

    private fun deleteUserRoleFromFirestore(userId: String, userEmail: String) {
        db.collection("userRoles").document(userId)
            .delete()
            .addOnSuccessListener {
                Log.d("UserManagementDebug", "deleteUserRoleFromFirestore: Documento de rol de usuario eliminado de Firestore para UID: $userId.")
                Toast.makeText(this, "Rol de usuario ${userEmail} eliminado.", Toast.LENGTH_SHORT).show()
                loadUsers()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al eliminar rol de usuario: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("UserManagementDebug", "deleteUserRoleFromFirestore: Error al eliminar rol de usuario $userId: ${e.message}", e)
            }
    }
}
