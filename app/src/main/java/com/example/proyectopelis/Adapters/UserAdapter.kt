package com.example.proyectopelis.Adapters // Asegúrate que el paquete sea el correcto

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectopelis.Models.UserRole
import com.example.proyectopelis.R
import java.util.Locale

class UserAdapter(
    private var users: MutableList<UserRole>,
    private val listener: OnUserActionListener
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    private var originalUsers: MutableList<UserRole> = mutableListOf()

    init {
        originalUsers.addAll(users)
        Log.d("UserAdapterDebug", "init: originalUsers inicializada con ${originalUsers.size} usuarios.")
    }

    interface OnUserActionListener {
        fun onEditRoleClick(user: UserRole)
        fun onDeleteUserClick(user: UserRole)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = users[position]
        // Accede a user.uid, que ahora es una propiedad 'var' asignada manualmente
        holder.tvUsername.text = user.username
        holder.tvUserEmail.text = user.email
        holder.tvUserRole.text = "Rol: ${user.role}"
        holder.tvUserId.text = "ID: ${user.uid}" // Usa la propiedad uid de la instancia de UserRole

        holder.btnEditRole.setOnClickListener { listener.onEditRoleClick(user) }
        holder.btnDeleteUser.setOnClickListener { listener.onDeleteUserClick(user) }
    }

    override fun getItemCount(): Int = users.size

    fun updateUsers(newUsers: List<UserRole>) {
        Log.d("UserAdapterDebug", "updateUsers: Recibiendo ${newUsers.size} usuarios para actualizar.")
        Log.d("UserAdapterDebug", "updateUsers: Lista interna del adaptador (antes de reemplazar): ${users.size} usuarios.")

        this.users = newUsers.toMutableList()
        this.originalUsers = newUsers.toMutableList()

        Log.d("UserAdapterDebug", "updateUsers: Lista interna del adaptador (después de reemplazar): ${users.size} usuarios.")
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        users.clear()
        if (query.isEmpty()) {
            users.addAll(originalUsers)
            Log.d("UserAdapterDebug", "filter: Consulta vacía, mostrando todos los ${originalUsers.size} usuarios.")
        } else {
            val lowerCaseQuery = query.toLowerCase(Locale.getDefault())
            for (user in originalUsers) {
                if (user.username.toLowerCase(Locale.getDefault()).contains(lowerCaseQuery) ||
                    user.email.toLowerCase(Locale.getDefault()).contains(lowerCaseQuery)) {
                    users.add(user)
                }
            }
            Log.d("UserAdapterDebug", "filter: Consulta '$query' filtrada, mostrando ${users.size} usuarios.")
        }
        notifyDataSetChanged()
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUsername: TextView = itemView.findViewById(R.id.tv_username_item)
        val tvUserEmail: TextView = itemView.findViewById(R.id.tv_user_email)
        val tvUserRole: TextView = itemView.findViewById(R.id.tv_user_role)
        val tvUserId: TextView = itemView.findViewById(R.id.tv_user_id)
        val btnEditRole: ImageButton = itemView.findViewById(R.id.btn_edit_user_role)
        val btnDeleteUser: ImageButton = itemView.findViewById(R.id.btn_delete_user)
    }
}
