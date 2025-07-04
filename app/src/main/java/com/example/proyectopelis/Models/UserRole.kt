package com.example.proyectopelis.Models // Asegúrate que el paquete sea el correcto

// Importa FirestoreDocument si lo usas para deserialización automática
// import com.google.firebase.firestore.DocumentId // Si lo usas para el UID del documento

data class UserRole(
    // Eliminamos 'val uid: String' de aquí. El UID será el ID del documento en Firestore.
    val email: String = "",
    val username: String = "",
    val role: String = ""
) {
    // Constructor sin argumentos requerido por Firestore para deserialización
    constructor() : this("", "", "")

    // Puedes añadir una propiedad para el UID si la necesitas para pasarla en la app,
    // pero NO se guardará como un campo en Firestore automáticamente si usas .set(UserRole)
    // Se llenará manualmente al leer desde Firestore.
    var uid: String = "" // Esta propiedad NO se mapeará a un campo de Firestore directamente
}
