package io.github.easeatten.ui.viewmodels.nav

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.easeatten.data.repos.SettingsRepository
import io.github.easeatten.data.repos.UserRepository
import io.github.easeatten.data.sources.AttendanceData
import io.github.easeatten.data.sources.LoginData
import io.github.easeatten.data.sources.SettingsData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

enum class AttendanceStateOrdering {
    NATURAL,
    SCORE_ASCENDING,
    SCORE_DESCENDING,
}

enum class AttendanceStateFilter {
    ALL,
    BEHIND,
    AHEAD,
    THEORY,
    PRACTICAL,
}

data class AttendanceState(
    /** How the subject entries are ordered. */
    val ordering: AttendanceStateOrdering = AttendanceStateOrdering.NATURAL,
    /** Which subject entries are displayed. */
    val filter: AttendanceStateFilter = AttendanceStateFilter.ALL,
    /** Whether the attendance target dialog is open or not. */
    val isTargetDialogOpen: Boolean = false,
    /** Whether the subject ordering dialog is open or not. */
    val isOrderingDialogOpen: Boolean = false,
    /** Whether the attendance data is on its way to be updated from remote. */
    val isAttendanceDataRefreshing: Boolean = false,
)

class AttendanceViewModel(
    private val settingsRepository: SettingsRepository,
    private val userRepository: UserRepository,
) : ViewModel() {
    private val statePrivate = MutableStateFlow(AttendanceState())
    val state = statePrivate.asStateFlow()

    val settings =
        settingsRepository.settingsFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = SettingsData(),
        )

    private val login =
        userRepository.loginFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = LoginData(),
        )

    val attendance =
        userRepository.attendanceFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = AttendanceData(),
        )

    private val mutexRefreshAttendanceData = Mutex()

    fun setOrdering(ordering: AttendanceStateOrdering) {
        statePrivate.update { it.copy(ordering = ordering) }
    }

    fun setFilter(filter: AttendanceStateFilter) {
        statePrivate.update { it.copy(filter = filter) }
    }

    fun showTargetDialog(show: Boolean) {
        statePrivate.update { it.copy(isTargetDialogOpen = show) }
    }

    fun showOrderingDialog(show: Boolean) {
        statePrivate.update { it.copy(isOrderingDialogOpen = show) }
    }

    fun updateAttendanceTargetPercentage(percentage: Float) {
        viewModelScope.launch { settingsRepository.updateAttendanceTargetPercentage(percentage) }
    }

    fun refreshAttendanceData() {
        viewModelScope.launch {
            mutexRefreshAttendanceData.withLock {
                statePrivate.update { it.copy(isAttendanceDataRefreshing = true) }
                userRepository.refreshAttendanceData()
                statePrivate.update { it.copy(isAttendanceDataRefreshing = false) }
            }
        }
    }
}

class AttendanceViewModelFactory(
    private val settingsRepository: SettingsRepository,
    private val userRepository: UserRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        when {
            modelClass.isAssignableFrom(AttendanceViewModel::class.java) -> {
                AttendanceViewModel(settingsRepository, userRepository) as T
            }

            else -> {
                throw IllegalArgumentException("Unknown ViewModel class $modelClass")
            }
        }
}
