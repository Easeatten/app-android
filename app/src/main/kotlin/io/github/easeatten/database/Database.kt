package io.github.easeatten.database

import android.util.Log
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

@Suppress("TooGenericExceptionCaught")
class Database(private val path: String, dataPersistenceEnabled: Boolean = false) {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(ROOT_URL)
    //
    //    init {
    //        database.setPersistenceEnabled(dataPersistenceEnabled)
    //    }

    private val reference: DatabaseReference = database.reference

    companion object {
        private const val LOGGER = "Firebase Database"
        private const val ROOT_URL =
            "https://easeatten-default-rtdb.asia-southeast1.firebasedatabase.app/"
    }

    suspend fun read(key: String): String? {
        return try {
            val snapshot = reference.child(path).child(key).get().await()

            if (!snapshot.exists()) null else snapshot.value as String
        } catch (e: Exception) {
            Log.e(LOGGER, "Failed to read data at path=${path}", e)
            throw e
        }
    }

    fun write(
        data: Map<String, String>,
        addOnSuccessListener: () -> Unit,
        addOnFailureListener: () -> Unit,
    ) {
        reference
            .child(path)
            .push()
            .setValue(data)
            .addOnSuccessListener { addOnSuccessListener() }
            .addOnFailureListener { addOnFailureListener() }
    }
}
