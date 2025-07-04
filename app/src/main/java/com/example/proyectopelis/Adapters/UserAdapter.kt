package com.example.proyectopelis.Adapters // ¡¡IMPORTANTE: Asegúrate que este paquete sea el correcto para tu proyecto!!

import android.util.Log // Importar Log para depuración
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView // ¡Importación crucial para RecyclerView!
import com.example.proyectopelis.Models.UserRole // Importa tu modelo de datos UserRole
import com.example.proyectopelis.R // Importa R para acceder a los recursos (layouts, IDs)

class UserAdapter(
    // El constructor recibe una referencia a la lista mutable de usuarios
    // 'private var' es importante para que la lista pueda ser reasignada o modificada
    private var users: MutableList<UserRole>,
    private val listener: OnUserActionListener
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() { // <--- ¡Esta es la línea que te daba error, ahora está completa!

    // Interfaz para manejar las acciones de click en los botones de editar/eliminar
    interface OnUserActionListener {
        fun onEditRoleClick(user: UserRole)
        fun onDeleteUserClick(user: UserRole)
    }

    // Crea y devuelve un ViewHolder para cada elemento de la lista
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    // Asocia los datos de un usuario con las vistas del ViewHolder
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        holder.tvUsername.text = user.username
        holder.tvUserEmail.text = user.email
        holder.tvUserRole.text = "Rol: ${user.role}"
        holder.tvUserId.text = "ID: ${user.uid}"

        // Configura los listeners para los botones de editar y eliminar
        holder.btnEditRole.setOnClickListener { listener.onEditRoleClick(user) }
        holder.btnDeleteUser.setOnClickListener { listener.onDeleteUserClick(user) }
    }

    // Devuelve el número total de elementos en la lista
    override fun getItemCount(): Int = users.size

    // Método para actualizar la lista de usuarios del adaptador
    fun updateUsers(newUsers: List<UserRole>) {
        Log.d("UserAdapterDebug", "updateUsers: Recibiendo ${newUsers.size} usuarios para actualizar.")
        Log.d("UserAdapterDebug", "updateUsers: Lista interna del adaptador (antes de reemplazar): ${users.size} usuarios.")

        // **** CAMBIO CLAVE AQUÍ: Reemplazar la lista completa ****
        // Convertimos la lista recibida (que puede ser inmutable) a una MutableList
        // y la asignamos a la variable 'users' del adaptador.
        this.users = newUsers.toMutableList()

        Log.d("UserAdapterDebug", "updateUsers: Lista interna del adaptador (después de reemplazar): ${users.size} usuarios.")
        notifyDataSetChanged() // Notifica al RecyclerView que los datos han cambiado y debe redibujarse
    }

    // Clase interna ViewHolder que contiene las vistas de cada elemento de la lista
    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUsername: TextView = itemView.findViewById(R.id.tv_username_item)
        val tvUserEmail: TextView = itemView.findViewById(R.id.tv_user_email)
        val tvUserRole: TextView = itemView.findViewById(R.id.tv_user_role)
        val tvUserId: TextView = itemView.findViewById(R.id.tv_user_id)
        val btnEditRole: ImageButton = itemView.findViewById(R.id.btn_edit_user_role)
        val btnDeleteUser: ImageButton = itemView.findViewById(R.id.btn_delete_user)
    }
}