package io.github.easeatten.data.sources

import android.content.Context
import android.icu.util.Calendar
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStore
import java.io.InputStream
import java.io.OutputStream
import kotlin.math.ceil
import kotlin.math.floor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

@Serializable
data class AttendanceRecord(
    val subject: String = "",
    val subjectPractical: Boolean = false,
    val attended: UInt = 0u,
    val delivered: UInt = 0u,
    val professors: List<String>,
) {
    fun getPercentage(): Float {
        if (this.delivered == 0u) return 1.0f
        return this.attended.toFloat() / this.delivered.toFloat()
    }

    fun getScore(target: Float): Int {
        return if (this.getPercentage() >= target) {
                floor((this.attended.toInt() - target * this.delivered.toInt()) / target)
            } else {
                -ceil((target * this.delivered.toInt() - attended.toInt()) / (1.0f - target))
            }
            .toInt()
    }
}

@Serializable
data class AttendanceData(
    val valid: Boolean = false,
    val name: String = "",
    val lastUpdatedYear: UInt = 0u,
    val lastUpdatedMonth: UInt = 0u,
    val lastUpdatedDay: UInt = 0u,
    val records: List<AttendanceRecord> = listOf(),
) {
    fun getLastUpdatedDate(): Calendar {
        val calendar = Calendar.getInstance()

        calendar.set(
            this.lastUpdatedYear.toInt(),
            this.lastUpdatedMonth.toInt() - 1, // Months indexed `0..11`.
            this.lastUpdatedDay.toInt(),
        )
        return calendar
    }

    fun getAggregatePercentage(): Float {
        if (this.records.isEmpty()) return 1.0f
        return records.fold(0.0f) { acc, record -> acc + record.getPercentage() } /
            this.records.size
    }
}

fun sxcapi.AttendanceData.toAttendanceData(): AttendanceData =
    AttendanceData(
        valid = true,
        name = this.name,
        lastUpdatedYear = this.lastUpdatedYear,
        lastUpdatedMonth = this.lastUpdatedMonth,
        lastUpdatedDay = this.lastUpdatedDay,
        records =
            this.subjects.map {
                AttendanceRecord(
                    subject = it.name ?: it.code ?: "Unknown",
                    subjectPractical = it.code?.endsWith("P") ?: false,
                    attended = it.records.sumOf { record -> record.attended },
                    delivered = it.records.sumOf { record -> record.delivered },
                    professors = it.records.mapNotNull { record -> record.professor },
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
