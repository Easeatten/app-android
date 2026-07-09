package io.github.easeatten.data.repos

import android.content.Context
import android.util.Log
import io.github.easeatten.data.sources.AttendanceDataStore
import io.github.easeatten.data.sources.AttendanceSerializer
import io.github.easeatten.data.sources.LoginDataStore
import io.github.easeatten.data.sources.LoginSerializer
import io.github.easeatten.data.sources.toAttendanceData
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import sxcapi.Department
import sxcapi.Exception as SxcapiException
import sxcapi.Session
import sxcapi.Student

class UserRepository(
    private val context: Context,
) {
  private val logTag = "EaseattenUserRepository"

  // Backend `Session` object. It can perform several actions pertaining to a `Student`, including
  // fetching attendance records, among other things. The backend is written in Rust and ported
  // over with Kotlin FFI bindings.
  //
  // Model loading is required for its in-built OCR engine.
  private val sxcapiSession =
      Session.newWithOcr(
          context.assets.open("models/text-detection.rten").use { it.readBytes() },
          context.assets.open("models/text-recognition.rten").use { it.readBytes() },
      )
  private val sxcapiMutex = Mutex()

  val loginFlow = context.LoginDataStore.data
  val attendanceFlow =
      merge(
          context.AttendanceDataStore.data,
          loginFlow.mapNotNull {
            try {
              val attendanceData =
                  sxcapiMutex.withLock {
                    sxcapiSession
                        .fetchAttendance(
                            Student(enumValueOf<Department>(it.departmentCode), it.year, it.roll),
                            it.semester,
                        )
                        .toAttendanceData()
                  }
              context.AttendanceDataStore.updateData { attendanceData }
              attendanceData
            } catch (e: Exception) {
              null
            }
          },
      )

  suspend fun registerUser(
      departmentCode: String,
      year: UInt,
      roll: UInt,
      semester: UInt,
  ): String? {
    val department = runCatching { enumValueOf<Department>(departmentCode) }.getOrNull()
    if (department == null) {
      return "Unknown Department"
    }

    try {
      val attendanceData =
          sxcapiMutex.withLock {
            sxcapiSession
                .fetchAttendance(Student(department, year, roll), semester)
                .toAttendanceData()
          }

      context.AttendanceDataStore.updateData { attendanceData }
      context.LoginDataStore.updateData {
        it.copy(
            loggedIn = true,
            departmentCode = departmentCode,
            year = year,
            roll = roll,
            semester = semester,
        )
      }

      return null
    } catch (e: SxcapiException.Parse) {
      Log.e(logTag, e.message!!)
      return "Student Not Found"
    } catch (e: SxcapiException.Validation) {
      Log.e(logTag, e.message!!)
      return "Invalid Details"
    } catch (e: SxcapiException) {
      Log.e(logTag, e.message!!)
      return "Connection Failure"
    }
  }

  suspend fun unregisterUser() {
    context.AttendanceDataStore.updateData { AttendanceSerializer.defaultValue }
    context.LoginDataStore.updateData { LoginSerializer.defaultValue }
  }
}
