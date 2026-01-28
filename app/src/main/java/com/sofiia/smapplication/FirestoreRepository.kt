package com.sofiia.smapplication

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class FirestoreRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val docRef = db.collection("messages").document("current")

    fun listenCurrentMessage(): Flow<Result<String>> = callbackFlow {
        val reg = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Result.failure(error))
                return@addSnapshotListener
            }
            val text = snapshot?.getString("text") ?: ""
            trySend(Result.success(text))
        }
        awaitClose { reg.remove() }
    }

    fun setMessage(text: String, onResult: (Result<Unit>) -> Unit) {
        docRef.set(mapOf("text" to text))
            .addOnSuccessListener { onResult(Result.success(Unit)) }
            .addOnFailureListener { e -> onResult(Result.failure(e)) }
    }
}