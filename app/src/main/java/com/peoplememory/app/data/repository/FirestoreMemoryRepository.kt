package com.peoplememory.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.Query
import com.peoplememory.app.model.CloudMemory
import com.peoplememory.app.model.Contact
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreMemoryRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    fun observeMemories(userId: String): Flow<List<CloudMemory>> = callbackFlow {
        val collection = firestore.collection("users").document(userId).collection("memories")
        val registration: ListenerRegistration = collection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val memories = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(CloudMemory::class.java)?.copy(
                        isPendingSync = doc.metadata.hasPendingWrites()
                    )
                } ?: emptyList()
                trySend(memories)
            }
        awaitClose { registration.remove() }
    }

    suspend fun saveMemory(userId: String, memory: CloudMemory): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(userId)
                .collection("memories")
                .document(memory.memoryId)
                .set(memory)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}