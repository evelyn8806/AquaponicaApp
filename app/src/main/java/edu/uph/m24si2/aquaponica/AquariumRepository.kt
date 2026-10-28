package edu.uph.m24si2.aquaponica

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AquariumRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun saveAquarium(
        aquarium: Aquarium,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            onError("Please log in first")
            return
        }

        val document = db.collection("users")
            .document(uid)
            .collection("aquariums")
            .document()

        val aquariumData = aquarium.copy(
            id = document.id,
            ownerUid = uid
        )

        document.set(aquariumData)
            .addOnSuccessListener {
                onSuccess(document.id)
            }
            .addOnFailureListener { error ->
                onError(
                    error.localizedMessage ?: "Failed to save aquarium"
                )
            }
    }

    fun getAquarium(
        onSuccess: (Aquarium?) -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            onError("User belum login")
            return
        }

        db.collection("users")
            .document(uid)
            .collection("aquariums")
            .limit(1)
            .get()
            .addOnSuccessListener { result ->
                val aquarium = result.documents
                    .firstOrNull()
                    ?.toObject(Aquarium::class.java)

                onSuccess(aquarium)
            }
            .addOnFailureListener { error ->
                onError(
                    error.localizedMessage
                        ?: "Gagal mengambil data aquarium"
                )
            }
    }

    fun updateAquarium(
        aquarium: Aquarium,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            onError("Please log in first")
            return
        }

        if (aquarium.id.isBlank()) {
            onError("Aquarium ID tidak ditemukan")
            return
        }

        if (aquarium.ownerUid != uid) {
            onError("Anda tidak memiliki akses ke aquarium ini")
            return
        }

        db.collection("users")
            .document(uid)
            .collection("aquariums")
            .document(aquarium.id)
            .set(aquarium)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { error ->
                onError(
                    error.localizedMessage
                        ?: "Gagal memperbarui aquarium"
                )
            }
    }

    fun deleteAquarium(
        aquarium: Aquarium,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid

        if (uid == null) {
            onError("Silakan login terlebih dahulu")
            return
        }

        if (aquarium.id.isBlank()) {
            onError("Aquarium ID tidak ditemukan")
            return
        }

        if (aquarium.ownerUid != uid) {
            onError("Anda tidak memiliki akses ke aquarium ini")
            return
        }

        db.collection("users")
            .document(uid)
            .collection("aquariums")
            .document(aquarium.id)
            .delete()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { error ->
                onError(
                    error.localizedMessage
                        ?: "Gagal menghapus aquarium"
                )
            }
    }

}
