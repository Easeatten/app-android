package io.github.easeatten.ui.nav.attendance

import android.icu.text.DateFormat
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import io.github.easeatten.data.repos.SettingsRepository
import io.github.easeatten.data.repos.UserRepository
import io.github.easeatten.data.sources.AttendanceData
import io.github.easeatten.data.sources.SettingsData
import io.github.easeatten.ui.common.ContinuousSliderDialogCard
import io.github.easeatten.ui.common.RadioButtonDialogCard
import io.github.easeatten.ui.icons.iconArrowBack
import io.github.easeatten.ui.icons.iconCalendarMonth
import io.github.easeatten.ui.icons.iconSort
import io.github.easeatten.ui.icons.iconTarget
import io.github.easeatten.ui.viewmodels.nav.AttendanceState
import io.github.easeatten.ui.viewmodels.nav.AttendanceStateFilter
import io.github.easeatten.ui.viewmodels.nav.AttendanceStateOrdering
import io.github.easeatten.ui.viewmodels.nav.AttendanceViewModel
import io.github.easeatten.ui.viewmodels.nav.AttendanceViewModelFactory
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay

private const val ANIMATION_DURATION = 300

@Composable
fun AttendancePage(navController: NavController) {
    val context = LocalContext.current.applicationContext
    var isLoading by remember { mutableStateOf(true) }

    // Data Repositories
    val settingsRepository = remember { SettingsRepository(context) }
    val userRepository = remember { UserRepository(context) }
    // `ViewModel` Synthesis
    val vmFactory = remember { AttendanceViewModelFactory(settingsRepository, userRepository) }
    val vm: AttendanceViewModel = viewModel(factory = vmFactory)

    val state by vm.state.collectAsStateWithLifecycle()
    val settings by vm.settings.collectAsStateWithLifecycle()
    val attendance by vm.attendance.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        // Add delay to hide lag during navigation.
        delay(500.milliseconds)
        isLoading = false
    }

    Scaffold(topBar = { TopBar(navController) }) { padding ->
        AnimatedContent(
            targetState = isLoading,
            label = "LoadingTransition",
            transitionSpec = {
                fadeIn(animationSpec = tween(ANIMATION_DURATION)) togetherWith
                    fadeOut(animationSpec = tween(ANIMATION_DURATION))
            },
        ) { isLoaded ->
            if (isLoaded || !attendance.valid)
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            else {
                Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                    ChipRow(vm, state, settings, attendance)
                    SubjectList(vm, state, settings, attendance)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TopBar(navController: NavController) {
    TopAppBar(
        title = { Text(text = "Attendance") },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(imageVector = iconArrowBack, contentDescription = "Back")
            }
        },
    )
}

@Composable
internal fun ChipRow(
    vm: AttendanceViewModel,
    state: AttendanceState,
    settings: SettingsData,
    attendance: AttendanceData,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        item { Spacer(modifier = Modifier.padding(horizontal = 7.5.dp)) }

        item { DateAssistChip(attendance) }
        item { TargetAssistChip(vm, state, settings) }
        item { OrderingAssistChip(vm, state) }

        items(AttendanceStateFilter.entries) { filter ->
            val text =
                when (filter) {
                    AttendanceStateFilter.ALL -> "All"
                    AttendanceStateFilter.BEHIND -> "Behind"
                    AttendanceStateFilter.AHEAD -> "Ahead"
                    AttendanceStateFilter.THEORY -> "Theory"
                    AttendanceStateFilter.PRACTICAL -> "Practical"
                }

            FilterChip(
                label = { Text(text) },
                selected = state.filter == filter,
                onClick = { vm.setFilter(filter) },
            )
        }

        item { Spacer(modifier = Modifier.padding(horizontal = 7.5.dp)) }
    }
}

@Composable
internal fun DateAssistChip(attendance: AttendanceData) {
    val dateCal = attendance.getLastUpdatedDate()
    val dateDisplay = DateFormat.getDateInstance(DateFormat.SHORT).format(dateCal)

    AssistChip(
        label = { Text(dateDisplay) },
        leadingIcon = {
            Icon(
                modifier = Modifier.size(AssistChipDefaults.IconSize),
                imageVector = iconCalendarMonth,
                contentDescription = "Date",
            )
        },
        onClick = {},
    )
}

@Composable
internal fun TargetAssistChip(
    vm: AttendanceViewModel,
    state: AttendanceState,
    settings: SettingsData,
) {
    val percentage = settings.attendanceTargetPercentage
    val percentageDisplay = (percentage * 100).roundToInt()

    AssistChip(
        label = { Text("$percentageDisplay%") },
        leadingIcon = {
            Icon(
                modifier = Modifier.size(AssistChipDefaults.IconSize),
                imageVector = iconTarget,
                contentDescription = "Target",
            )
        },
        onClick = { vm.showTargetDialog(true) },
    )

    if (!state.isTargetDialogOpen) return

    Dialog(onDismissRequest = { vm.showTargetDialog(false) }) {
        ContinuousSliderDialogCard(
            modifier = Modifier.fillMaxWidth(),
            title = "Target",
            value = percentage,
            onValueChange = {
                val percentageRounded = (it * 100).roundToInt() / 100.0f
                vm.updateAttendanceTargetPercentage(percentageRounded)
            },
            valueToLabel = {
                val percentageDisplay = (it * 100).roundToInt()
                "$percentageDisplay%"
            },
        )
    }
}

@Composable
internal fun OrderingAssistChip(vm: AttendanceViewModel, state: AttendanceState) {
    AssistChip(
        label = { Text("Sort") },
        leadingIcon = {
            Icon(
                modifier = Modifier.size(AssistChipDefaults.IconSize),
                imageVector = iconSort,
                contentDescription = "Sort",
            )
        },
        onClick = { vm.showOrderingDialog(true) },
    )

    if (!state.isOrderingDialogOpen) return

    Dialog(onDismissRequest = { vm.showOrderingDialog(false) }) {
        RadioButtonDialogCard(
            modifier = Modifier.fillMaxWidth(),
            title = "Sort",
            options = AttendanceStateOrdering.entries,
            optionsToLabels = {
                when (it) {
                    AttendanceStateOrdering.NATURAL -> "Natural"
                    AttendanceStateOrdering.SCORE_ASCENDING -> "Score: Ascending"
                    AttendanceStateOrdering.SCORE_DESCENDING -> "Score: Descending"
                }
            },
            selected = state.ordering,
            onSelectedChange = { vm.setOrdering(it) },
        )
    }
}
