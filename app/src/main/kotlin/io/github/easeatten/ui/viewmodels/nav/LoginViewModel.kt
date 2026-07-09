package io.github.easeatten.ui.viewmodels.nav

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.easeatten.data.repos.UserRepository
import io.github.easeatten.data.sources.LoginData
import java.util.Calendar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginState(
    // The department code of the student.
    val department: String = "",
    // The roll number of the student.
    val roll: String = "",
    // The year in which the student joined the institution.
    val year: String = "",
    // The year in which the student is currently studying in.
    val semester: String = "",
    // Tri-states to determine whether a certain field is invalid. A `null` value indicates
    // evaluation hasn't happened yet.
    val isDepartmentInvalid: Boolean? = null,
    val isRollInvalid: Boolean? = null,
    val isYearInvalid: Boolean? = null,
    val isSemesterInvalid: Boolean? = null,
    // The UI is currently on hold as there's an attempt for login.
    val isLoginInProgress: Boolean = false,
    // Flag to determine if the login attempt has failed.
    val loginFailed: Boolean? = null,
    // Error message displayed when login fails.
    val loginErrorMessage: String? = null,
)

class LoginViewModel(
    private val userRepository: UserRepository,
) : ViewModel() {
  private val statePrivate = MutableStateFlow(LoginState())
  val state = statePrivate.asStateFlow()

  val login =
      userRepository.loginFlow.stateIn(
          scope = viewModelScope,
          started = SharingStarted.Eagerly,
          initialValue = LoginData(),
      )

  private val maxSemester = 10

  fun updateDepartment(value: String) {
    val valueFormatted =
        value
            .take(4) // First four characters
            .uppercase() // Convert to uppercase
            .replace(Regex("[^A-Z]"), "") // Remove all non-uppercase letters

    statePrivate.update {
      it.copy(
          department = valueFormatted,
          isDepartmentInvalid = valueFormatted.length < 4,
      )
    }
  }

  fun updateRoll(value: String) {
    val valueFormatted =
        value
            .take(4) // First four characters
            .replace(Regex("^0*"), "") // Replace leading zeros
            .replace(Regex("[^0-9]"), "") // Remove all non-numbers

    statePrivate.update {
      it.copy(
          roll = valueFormatted,
          isRollInvalid = valueFormatted.isEmpty(),
      )
    }
  }

  fun updateYear(value: String) {
    val valueFormatted =
        value
            .take(4) // First four characters
            .replace(Regex("^0*"), "") // Replace leading zeros
            .replace(Regex("[^0-9]"), "") // Remove all non-numbers
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)

    statePrivate.update {
      it.copy(
          year = valueFormatted,
          isYearInvalid =
              valueFormatted.isEmpty() ||
                  valueFormatted.toInt() > currentYear ||
                  valueFormatted.toInt() < currentYear - maxSemester / 2 - 1,
      )
    }
  }

  fun updateSemester(value: String) {
    val valueFormatted =
        value
            .take(4) // First four characters
            .replace(Regex("^0*"), "") // Replace leading zeros
            .replace(Regex("[^0-9]"), "") // Remove all non-numbers

    statePrivate.update {
      it.copy(
          semester = valueFormatted,
          isSemesterInvalid = valueFormatted.isEmpty() || valueFormatted.toInt() > maxSemester,
      )
    }
  }

  fun attemptLogin() {
    viewModelScope.launch {
      statePrivate.update {
        it.copy(isLoginInProgress = true, loginFailed = null, loginErrorMessage = null)
      }

      val department = state.value.department.uppercase()
      val year = state.value.year.toUInt()
      val roll = state.value.roll.toUInt()
      val semester = state.value.semester.toUInt()

      val error = userRepository.registerUser(department, year, roll, semester)

      statePrivate.update {
        it.copy(isLoginInProgress = false, loginFailed = error != null, loginErrorMessage = error)
      }
    }
  }

  fun clearLoginState() {
    statePrivate.update { it.copy(loginFailed = null, loginErrorMessage = null) }
  }
}

class LoginViewModelFactory(
    private val userRepository: UserRepository,
) : ViewModelProvider.Factory {
  @Suppress("UNCHECKED_CAST")
  override fun <T : ViewModel> create(modelClass: Class<T>): T =
      when {
        modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
          LoginViewModel(userRepository) as T
        }

        else -> {
          throw IllegalArgumentException("Unknown ViewModel class $modelClass")
        }
      }
}
