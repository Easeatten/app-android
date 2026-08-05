package io.github.easeatten.data.repos

import android.content.Context
import android.util.Log
import io.github.easeatten.data.sources.AttendanceData
import io.github.easeatten.data.sources.AttendanceDataStore
import io.github.easeatten.data.sources.AttendanceSerializer
import io.github.easeatten.data.sources.LoginDataStore
import io.github.easeatten.data.sources.LoginSerializer
import io.github.easeatten.data.sources.toAttendanceData
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import sxcapi.Department
import sxcapi.Exception as SxcapiException
import sxcapi.Session
import sxcapi.Student

class UserRepository(private val context: Context) {
    private val logTag = "EaseattenUserRepository"

    // Backend `Session` object. It can perform several actions pertaining to a `Student`, including
    // fetching attendance records, among other things. The backend is written in Rust and ported
    // over with Kotlin FFI bindings.
    private val sxcapiSession = Session()
    private val sxcapiMutex = Mutex()

    val loginFlow = context.LoginDataStore.data
    val attendanceFlow = context.AttendanceDataStore.data

    private suspend fun fetchAttendanceDataFromSource(
        department: sxcapi.Department,
        year: UInt,
        roll: UInt,
        semester: UInt,
    ): AttendanceData = sxcapiMutex.withLock {
        sxcapiSession.fetchAttendance(Student(department, year, roll), semester).toAttendanceData()
    }

    suspend fun registerUser(
        departmentCode: String,
        year: UInt,
        roll: UInt,
        semester: UInt,
    ): String? {
        val department =
            runCatching { enumValueOf<Department>(departmentCode) }
                .getOrElse {
                    return "Unknown Department"
                }

        var message: String? = null
        try {
            context.AttendanceDataStore.updateData {
                fetchAttendanceDataFromSource(department, year, roll, semester)
            }

            context.LoginDataStore.updateData {
                it.copy(
                    loggedIn = true,
                    departmentCode = departmentCode,
                    year = year,
                    roll = roll,
                    semester = semester,
                )
            }
        } catch (e: SxcapiException.Parse) {
            message = "Student Not Found"
            Log.e(logTag, e.message!!)
        } catch (e: SxcapiException.Validation) {
            message = "Invalid Details"
            Log.e(logTag, e.message!!)
        } catch (e: SxcapiException) {
            message = "Connection Failure"
            Log.e(logTag, e.message!!)
        }

        return message
    }

    suspend fun unregisterUser() {
        context.AttendanceDataStore.updateData { AttendanceSerializer.defaultValue }
        context.LoginDataStore.updateData { LoginSerializer.defaultValue }
    }

    suspend fun refreshAttendanceData() {
        val login = loginFlow.stateIn(CoroutineScope(EmptyCoroutineContext)).value
        var data: AttendanceData? = null

        if (!login.valid) return

        val department =
            runCatching { enumValueOf<Department>(login.departmentCode) }
                .getOrElse {
                    Log.e(logTag, "Unexpected department value found: ${login.departmentCode}")
                    return
                }

        try {
            try {
                data =
                    fetchAttendanceDataFromSource(
                        department,
                        login.year,
                        login.roll,
                        login.semester,
                    )
            } catch (e: SxcapiException.Validation) {
                // Exception during form validation suggests that the server is unable to find the
                // student as per the details table. So, explore the possibility that the user has
                // moved on to the next semester.
                Log.w(
                    logTag,
                    "Failed to fetch attendance details, trying with succeeding semester: ${e.message!!}",
                )

                data =
                    fetchAttendanceDataFromSource(
                        department,
                        login.year,
                        login.roll,
                        login.semester + 1u,
                    )

                context.LoginDataStore.updateData { it.copy(semester = login.semester + 1u) }
            }
        } catch (e: SxcapiException) {
            Log.e(logTag, "Failed to fetch attendance details: ${e.message!!}")
        }

        if (data != null) context.AttendanceDataStore.updateData { data }
    }
}
