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
    val subjectCode: String = "",
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

    fun createSummary(penultimateData: AttendanceData): AttendanceSummary {
        // Map subject code to corresponding record.
        val oldData = penultimateData.records.associateBy { it.subjectCode }
        val newData = records.associateBy { it.subjectCode }

        // Check for change in subjects.
        val subjectsAdded: (Set<String>) -> Map<AttendanceRecord, UInt> = {
            it.associate { code -> newData[code] as AttendanceRecord to newData[code]!!.attended }
        }
        val subjectsRemoved: (Set<String>) -> Map<AttendanceRecord, UInt> = {
            it.associate { code -> oldData[code] as AttendanceRecord to newData[code]!!.attended }
        }

        val missedClassesCount = mutableMapOf<AttendanceRecord, UInt>()
        val attendedClassesCount = mutableMapOf<AttendanceRecord, UInt>()

        newData.keys.forEach { code ->
            val newRecord = newData[code]!!
            // oldData returns null when a new subject is added.
            // So we skip that iteration when it returns null.
            val oldRecord = oldData[code] ?: return@forEach

            val deliveredDiff = newRecord.delivered.toInt() - oldRecord.delivered.toInt()
            val attendedDiff = newRecord.attended.toInt() - oldRecord.attended.toInt()

            // Both delivered and attended class(es) changed by the same amount.
            if (deliveredDiff == attendedDiff) {
                // No changes made, continue to the next iteration.
                if (attendedDiff == 0) return@forEach
                // If no. of missed classes stored is found to be positive, it
                // concludes that all classes were attended.
                else if (attendedDiff > 0) attendedClassesCount[newRecord] = attendedDiff.toUInt()
                // If no. of missed classes stored is found to be negative, it
                // concludes that classes were taken back after uploading in the website.
                else missedClassesCount[newRecord] = attendedDiff.toUInt()
            }

            // No. of delivered classes was increased or stayed the same:
            else if (deliveredDiff >= 0) {
                // and no. of attended classes was decreased or stayed the same.
                if (attendedDiff <= 0)
                    missedClassesCount[newRecord] = (deliveredDiff - attendedDiff).toUInt()
                // and no. of class(es) attended was also increased.
                else
                    attendedClassesCount[newRecord] =
                        (attendedDiff -
                                (
                                // Special case where student was granted
                                // extra no. of attended class(es).
                                if (attendedDiff > deliveredDiff) deliveredDiff
                                else
                                    0.also {
                                        // Get no. of class(es) missed.
                                        // Unexpected extra no. of delivered class(es) also gets
                                        // added up.
                                        missedClassesCount[newRecord] =
                                            (deliveredDiff - attendedDiff).toUInt()
                                    }))
                            .toUInt()
            }

            // No. of delivered classes was decreased:
            else {
                // and no. of attended classes was increased or stayed the same.
                if (attendedDiff >= 0)
                    attendedClassesCount[newRecord] = (attendedDiff - deliveredDiff).toUInt()
                // and no. of attended classes was also decreased.
                else
                    missedClassesCount[newRecord] =
                        (-attendedDiff +
                                (if (attendedDiff < deliveredDiff) deliveredDiff
                                else
                                    0.also {
                                        // Get no. of class(es) attended.
                                        // Unexpected extra no. of attended class(es) also gets
                                        // added up.
                                        attendedClassesCount[newRecord] =
                                            (attendedDiff - deliveredDiff).toUInt()
                                    }))
                            .toUInt()
            }
        }

        return AttendanceSummary(
            subjectsAdded = subjectsAdded(newData.keys - oldData.keys),
            subjectsRemoved = subjectsRemoved(oldData.keys - newData.keys),
            missedClasses = missedClassesCount,
            attendedClasses = attendedClassesCount,
            attendedAll = missedClassesCount.isEmpty(),
            missedAll = attendedClassesCount.isEmpty(),
        )
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
                    subjectCode = it.code ?: "Unknown",
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
