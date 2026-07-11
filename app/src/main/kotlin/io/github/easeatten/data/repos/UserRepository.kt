package io.github.easeatten.data.repos

import android.content.Context
import android.util.Log
import io.github.easeatten.data.sources.AttendanceData
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
          loginFlow.mapNotNull { login ->
            var data: AttendanceData? = null

            try {
              try {
                data =
                    sxcapiMutex.withLock {
                      sxcapiSession
                          .fetchAttendance(
                              Student(
                                  enumValueOf<Department>(login.departmentCode),
                                  login.year,
                                  login.roll),
                              login.semester,
                          )
                          .toAttendanceData()
                    }
              } catch (e: SxcapiException.Validation) {
                // Exception during form validation suggests that the server is unable to find the
                // student as per the details table. So, explore the possibility that the user has
                // moved on to the next semester.
                Log.w(logTag, "Failed to fetch attendance details, trying with succeeding semester")

                data =
                    sxcapiMutex.withLock {
                      sxcapiSession
                          .fetchAttendance(
                              Student(
                                  enumValueOf<Department>(login.departmentCode),
                                  login.year,
                                  login.roll),
                              login.semester + 1u,
                          )
                          .toAttendanceData()
                    }
                context.LoginDataStore.updateData { it.copy(semester = login.semester + 1u) }
              }
            } catch (e: SxcapiException) {
              Log.e(logTag, "Failed to fetch attendance details: ${e.message ?: ""}")
            } catch (e: IllegalArgumentException) {
              // Thrown for `enumValueOf`. Swallow the exception.
            }

            if (data != null) {
              context.AttendanceDataStore.updateData { data }
            }

            data
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
