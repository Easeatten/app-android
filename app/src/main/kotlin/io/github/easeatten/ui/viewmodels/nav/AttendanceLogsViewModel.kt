package io.github.easeatten.ui.viewmodels.nav

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.easeatten.data.repos.SettingsRepository
import io.github.easeatten.data.repos.UserRepository
import io.github.easeatten.data.sources.AttendanceLog
import io.github.easeatten.data.sources.AttendanceLogStore
import io.github.easeatten.data.sources.AttendanceLogsSerializer
import io.github.easeatten.data.sources.SettingsData
import io.github.easeatten.data.sources.SettingsDataStore
import java.time.DayOfWeek
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AttendanceLogsState(
    /**
     * Whether filter is active for AttendanceLogs or not. 'null' indicates filter is not active.
     */
    val filterByDay: DayOfWeek? = null,
    /** Whether the confirmation alert dialog is shown or not */
    val showConfirmationDialog: Boolean = false,
)

class AttendanceLogsViewModel(
    settingsRepository: SettingsRepository,
    userRepository: UserRepository,
) : ViewModel() {
    private val statePrivate = MutableStateFlow(AttendanceLogsState())
    val state = statePrivate.asStateFlow()

    val settings =
        settingsRepository.settingsFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = SettingsData(),
        )

    val logs =
        userRepository.attendanceLogsFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = AttendanceLog(),
        )

    fun filterLogs(day: DayOfWeek?) {
        statePrivate.update { it.copy(filterByDay = day) }
    }

    fun showConfirmationDialog(show: Boolean) {
        statePrivate.update { it.copy(showConfirmationDialog = show) }
    }

    fun deleteAttendanceHistory(context: Context) {
        viewModelScope.launch {
            context.AttendanceLogStore.updateData { AttendanceLogsSerializer.defaultValue }
        }
    }

    fun pauseAttendanceHistory(context: Context) {
        viewModelScope.launch {
            context.SettingsDataStore.updateData { it.copy(historyPaused = true) }
        }
    }
}

class AttendanceLogsViewModelFactory(
    private val settingsRepository: SettingsRepository,
    private val userRepository: UserRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        when {
            modelClass.isAssignableFrom(AttendanceLogsViewModel::class.java) -> {
                AttendanceLogsViewModel(settingsRepository, userRepository) as T
            }
            else -> {
                throw IllegalArgumentException("Unknown ViewModel class $modelClass")
            }
        }
}
