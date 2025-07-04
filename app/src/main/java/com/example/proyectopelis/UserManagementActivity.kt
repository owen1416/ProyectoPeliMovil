package com.example.proyectopelis // Asegúrate que el paquete sea el correcto

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log // Importar Log para depuración
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class UserManagementActivity : AppCompatActivity(), UserAdapter.OnUserActionListener {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var rvUsers: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private lateinit var toolbar: Toolbar

    private var allUsersList: MutableList<UserRole> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user) // Asegúrate que este sea el layout correcto con la Toolbar

        Log.d("UserManagementDebug", "onCreate: Actividad iniciada.")

        db = Firebase.firestore
        auth = FirebaseAuth.getInstance()

        rvUsers = findViewById(R.id.rv_users)

        toolbar = findViewById(R.id.toolbar_user_management) // ID de la Toolbar en tu XML
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Gestión de Usuarios"

        userAdapter = UserAdapter(allUsersList, this)
        rvUsers.layoutManager = LinearLayoutManager(this)
        rvUsers.adapter = userAdapter

        Log.d("UserManagementDebug", "onCreate: RecyclerView y Adapter configurados.")

        loadUsers() // Inicia la carga de usuarios
        Log.d("UserManagementDebug", "onCreate: Llamada a loadUsers().")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            Log.d("UserManagementDebug", "onOptionsItemSelected: Botón de volver presionado.")
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadUsers() {
        Log.d("UserManagementDebug", "loadUsers: Iniciando carga de usuarios desde Firestore.")
        val currentUserUid = auth.currentUser?.uid
        Log.d("UserManagementDebug", "loadUsers: UID del usuario actual logueado: $currentUserUid")

        db.collection("userRoles")
            .get()
            .addOnSuccessListener { result ->
                Log.d("UserManagementDebug", "loadUsers: Consulta a Firestore exitosa.")
                allUsersList.clear() // Limpia la lista existente
                var documentsProcessed = 0

                if (result.isEmpty) {
                    Log.d("UserManagementDebug", "loadUsers: No se encontraron documentos en la colección 'userRoles'.")
                }

                for (document in result) {
                    documentsProcessed++
                    val userId = document.id
                    val role = document.getString("role") ?: "user"
                    val email = document.getString("email") ?: "N/A"
                    val username = document.getString("username") ?: "N/A"

                    val userRole = UserRole(userId, email, username, role)
                    allUsersList.add(userRole)
                    Log.d("UserManagementDebug", "loadUsers: Documento procesado: UID=$userId, Email=$email, Username=$username, Rol=$role")
                }
                Log.d("UserManagementDebug", "loadUsers: Total de documentos procesados: $documentsProcessed")

                userAdapter.updateUsers(allUsersList) // Actualiza el adaptador
                Log.d("UserManagementDebug", "loadUsers: Adaptador actualizado. Usuarios en lista: ${allUsersList.size}")

                if (allUsersList.isEmpty()) {
                    Toast.makeText(this, "No se encontraron usuarios para mostrar.", Toast.LENGTH_SHORT).show()
                }

            }
            .addOnFailureListener { exception ->
                // Este bloque se ejecuta si hay un error en la consulta (ej. PERMISSION_DENIED)
                Log.e("UserManagementDebug", "loadUsers: Error al cargar usuarios desde Firestore: ${exception.message}", exception)
                Toast.makeText(this, "Error al cargar usuarios: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }

    override fun onEditRoleClick(user: UserRole) {
        Log.d("UserManagementDebug", "onEditRoleClick: Editando rol para usuario: ${user.email}")
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_user_role, null)

        val tvUserId: TextView = dialogView.findViewById(R.id.tv_dialog_edit_user_id)
        val tvUserEmail: TextView = dialogView.findViewById(R.id.tv_dialog_edit_user_email)
        val tvUsername: TextView = dialogView.findViewById(R.id.tv_dialog_edit_username)
        val spinnerRole: Spinner = dialogView.findViewById(R.id.spinner_dialog_edit_role)

        tvUserId.text = "ID: ${user.uid}"
        tvUserEmail.text = user.email
        tvUsername.text = user.username

        val roles = arrayOf("user", "admin")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRole.adapter = adapter

        val currentRoleIndex = roles.indexOf(user.role)
        if (currentRoleIndex != -1) {
            spinnerRole.setSelection(currentRoleIndex)
        }

        AlertDialog.Builder(this)
            .setTitle("Editar Rol de Usuario")
            .setView(dialogView)
            .setPositiveButton("Guardar") { dialog, _ ->
                val newRole = spinnerRole.selectedItem.toString()
                Log.d("UserManagementDebug", "onEditRoleClick: Intentando actualizar rol de ${user.email} a $newRole")
                updateUserRole(user.uid, newRole, user.email)
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                Log.d("UserManagementDebug", "onEditRoleClick: Edición de rol cancelada.")
                dialog.cancel()
            }
            .show()
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
                loadUsers() // Recarga la lista para reflejar el cambio
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
            Log.w("UserManagementDebug", "onDeleteUserClick: Intento de admin de eliminarse a sí mismo.")
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
                loadUsers() // Recarga la lista
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al eliminar rol de usuario: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("UserManagementDebug", "deleteUserRoleFromFirestore: Error al eliminar rol de usuario $userId: ${e.message}", e)
            }
    }
}

