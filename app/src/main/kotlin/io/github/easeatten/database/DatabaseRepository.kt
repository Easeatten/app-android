package io.github.easeatten.database

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

// The class has been written as per the requirements of the branch augustus/syllabus-update.
// It may be modified later if required.
@Suppress("Unchecked_Cast")
class DatabaseRepository<T>(private val path: String) {
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(ROOT_URL)
    // getInstance() always returns the same singleton instance of FirebaseDatabase.
    // This is not clearly mentioned in the docs.
    // It's vaguely written as "Gets the default FirebaseDatabase instance".

    // To get a reference to the root node of JSON tree.
    private val reference: DatabaseReference = database.reference

    companion object {
        private const val LOGGER = "RealtimeDatabase"
        private const val ROOT_URL =
            "https://easeatten-default-rtdb.asia-southeast1.firebasedatabase.app/"
    }

    suspend fun read(key: String): T? {
        val snapshot = reference.child(path).child(key).get().await()

        return if (snapshot.exists()) snapshot.value as T? else null
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
