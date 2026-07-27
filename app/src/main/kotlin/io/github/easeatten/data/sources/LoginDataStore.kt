package io.github.easeatten.data.sources

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStore
import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

@Serializable
data class LoginData(
    val valid: Boolean = false,
    val loggedIn: Boolean = false,
    val departmentCode: String = "",
    val year: UInt = 0u,
    val roll: UInt = 0u,
    val semester: UInt = 0u,
)

object LoginSerializer : Serializer<LoginData> {
  override val defaultValue = LoginData(valid = true)

  override suspend fun readFrom(input: InputStream): LoginData {
    try {
      return Json.decodeFromString<LoginData>(input.readBytes().decodeToString())
    } catch (serialization: SerializationException) {
      throw CorruptionException("corrupted login data:", serialization)
    }
  }

  override suspend fun writeTo(t: LoginData, output: OutputStream) {
    withContext(Dispatchers.IO) {
      output.write(Json.encodeToString(t.copy(valid = true)).encodeToByteArray())
    }
  }
}

val Context.LoginDataStore: DataStore<LoginData> by
    dataStore(
        fileName = "login.json",
        serializer = LoginSerializer,
        corruptionHandler = ReplaceFileCorruptionHandler { LoginSerializer.defaultValue },
    )
