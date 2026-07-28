package io.github.easeatten.ui.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SplashScreenViewModel : ViewModel() {
    private val loadingDonePrivate = MutableStateFlow(false)
    val loadingDone = loadingDonePrivate.asStateFlow()

    fun setLoadingDone() {
        loadingDonePrivate.value = true
    }
}
