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
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json

@Serializable
data class AttendanceLog(
    val penultimateData: AttendanceData = AttendanceData(),
    @Serializable(with = AttendanceSummarys::class)
    val summary: LinkedHashMap<String, AttendanceSummary> = LinkedHashMap(),
)

@Serializable
data class AttendanceSummary(
    val subjectsAdded: Map<AttendanceRecord, UInt> = emptyMap(),
    val subjectsRemoved: Map<AttendanceRecord, UInt> = emptyMap(),
    val missedClasses: Map<AttendanceRecord, UInt> = emptyMap(),
    val attendedClasses: Map<AttendanceRecord, UInt> = emptyMap(),
    val attendedAll: Boolean = false,
    val missedAll: Boolean = false,
)

object AttendanceLogsSerializer : Serializer<AttendanceLog> {
    val jsonBuilder = Json { allowStructuredMapKeys = true }
    override val defaultValue = AttendanceLog()

    override suspend fun readFrom(input: InputStream): AttendanceLog {
        try {
            return jsonBuilder.decodeFromString<AttendanceLog>(input.readBytes().decodeToString())
        } catch (serialization: SerializationException) {
            throw CorruptionException("Failed to deserialize AttendanceLog", serialization)
        }
    }

    override suspend fun writeTo(t: AttendanceLog, output: OutputStream) {
        withContext(Dispatchers.IO) {
            output.write(jsonBuilder.encodeToString(t).encodeToByteArray())
        }
    }
}

object AttendanceSummarys : KSerializer<LinkedHashMap<String, AttendanceSummary>> {
    val jsonBuilder = Json { allowStructuredMapKeys = true }

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("io.github.easeatten.AttendanceSummarys", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LinkedHashMap<String, AttendanceSummary>) {
        val json = jsonBuilder.encodeToString(value)

        encoder.encodeString(json)
    }

    override fun deserialize(decoder: Decoder): LinkedHashMap<String, AttendanceSummary> {
        val jsonString = decoder.decodeString()

        return jsonBuilder.decodeFromString<LinkedHashMap<String, AttendanceSummary>>(jsonString)
    }
}

val Context.AttendanceLogStore: DataStore<AttendanceLog> by
    dataStore(
        fileName = "logs.json",
        serializer = AttendanceLogsSerializer,
        corruptionHandler = ReplaceFileCorruptionHandler { AttendanceLogsSerializer.defaultValue },
    )
