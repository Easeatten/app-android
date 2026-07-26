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
import sxcapi.AttendanceData as SxcapiAttendanceData

@Serializable
data class AttendanceRecord(
    val subject: String = "",
    val subjectPractical: Boolean = false,
    val attended: UInt = 0u,
    val delivered: UInt = 0u,
)

@Serializable
data class AttendanceData(
    val valid: Boolean = false,
    val name: String = "",
    val records: List<AttendanceRecord> = listOf(),
)

fun SxcapiAttendanceData.toAttendanceData(): AttendanceData =
    AttendanceData(
        valid = true,
        name = this.name,
        records =
            this.subjects.map {
              AttendanceRecord(
                  subject = it.name ?: it.code ?: "Unknown",
                  subjectPractical = it.code?.endsWith("P") ?: false,
                  attended = it.records.sumOf { record -> record.attended },
                  delivered = it.records.sumOf { record -> record.delivered },
              )
            },
    )

object AttendanceSerializer : Serializer<AttendanceData> {
  override val defaultValue = AttendanceData()

  override suspend fun readFrom(input: InputStream): AttendanceData {
    try {
      return Json.decodeFromString<AttendanceData>(input.readBytes().decodeToString())
    } catch (serialization: SerializationException) {
      throw CorruptionException("corrupted attendance data:", serialization)
    }
  }

  override suspend fun writeTo(t: AttendanceData, output: OutputStream) {
    withContext(Dispatchers.IO) {
      output.write(Json.encodeToString(t.copy(valid = true)).encodeToByteArray())
    }
  }
}

val Context.AttendanceDataStore: DataStore<AttendanceData> by
    dataStore(
        fileName = "attendance.json",
        serializer = AttendanceSerializer,
        corruptionHandler = ReplaceFileCorruptionHandler { AttendanceSerializer.defaultValue },
    )
