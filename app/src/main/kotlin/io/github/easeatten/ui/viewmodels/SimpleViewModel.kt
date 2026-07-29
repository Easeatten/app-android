package io.github.easeatten.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.easeatten.data.repos.SettingsRepository
import io.github.easeatten.data.repos.UserRepository
import io.github.easeatten.data.sources.AttendanceData
import io.github.easeatten.data.sources.LoginData
import io.github.easeatten.data.sources.SettingsData
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class SimpleViewModel(settingsRepository: SettingsRepository, userRepository: UserRepository) :
    ViewModel() {
    val settings =
        settingsRepository.settingsFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = SettingsData(),
        )

    val login =
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
}

class SimpleViewModelFactory(
    private val settingsRepository: SettingsRepository,
    private val userRepository: UserRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        when {
            modelClass.isAssignableFrom(SimpleViewModel::class.java) -> {
                SimpleViewModel(settingsRepository, userRepository) as T
            }

            else -> {
                throw IllegalArgumentException("Unknown ViewModel class $modelClass")
            }
        }
}
