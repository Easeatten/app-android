package io.github.easeatten.ui.nav.attendance

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import io.github.easeatten.data.repos.SettingsRepository
import io.github.easeatten.data.repos.UserRepository
import io.github.easeatten.data.sources.AttendanceRecord
import io.github.easeatten.data.sources.AttendanceSummary
import io.github.easeatten.ui.icons.iconArrowBack
import io.github.easeatten.ui.icons.iconFilterList
import io.github.easeatten.ui.icons.iconFilterOff
import io.github.easeatten.ui.icons.iconMore
import io.github.easeatten.ui.viewmodels.nav.AttendanceLogsState
import io.github.easeatten.ui.viewmodels.nav.AttendanceLogsViewModel
import io.github.easeatten.ui.viewmodels.nav.AttendanceLogsViewModelFactory
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private const val BOUNDS_DURATION_MS = 400 // Expand/collapse speed.
private val animationTransitionSpec =
    fadeIn(
        animationSpec = tween(durationMillis = BOUNDS_DURATION_MS, easing = FastOutLinearInEasing)
    ) togetherWith
        fadeOut(
            animationSpec = tween(durationMillis = BOUNDS_DURATION_MS, easing = FastOutSlowInEasing)
        )

@Composable
fun AttendanceLogs(navController: NavController) {
    val snackbarHostState = remember { SnackbarHostState() }
    // The coroutine scope is used for the snackbar.
    // The scope gets cancelled when the parent composable disappears,
    // so defining it here is recommended.
    val scope = rememberCoroutineScope()

    val context = LocalContext.current

    // Data Repositories
    val settingsRepository = remember { SettingsRepository(context) }
    val userRepository = remember { UserRepository(context) }
    // `ViewModel` Synthesis
    val vmFactory = remember { AttendanceLogsViewModelFactory(settingsRepository, userRepository) }
    val vm: AttendanceLogsViewModel = viewModel(factory = vmFactory)

    val state by vm.state.collectAsStateWithLifecycle()
    val settings by vm.settings.collectAsStateWithLifecycle()
    val logs by vm.logs.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { AppBar(navController, vm, state, logs.summary.isNotEmpty()) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { padding ->
        if (logs.summary.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding).padding(50.dp),
                contentAlignment = Alignment.TopCenter,
            ) {
                Text(
                    text = "No records to show yet.",
                    color =
                        MaterialTheme.colorScheme.onSurface
                            // Make the text 40% opaque.
                            .copy(alpha = 0.4f),
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(10.dp)) {
                fun predicament(date: String): Boolean =
                    if (state.filterByDay != null) {
                        val day = LocalDate.parse(date).dayOfWeek
                        day == state.filterByDay
                    } else true

                items(logs.summary.toList().filter { predicament(it.first) }, key = { it.first }) {
                    (key, value) ->
                    LogCard(
                        modifier = Modifier.fillMaxWidth().animateItem(),
                        date = key,
                        summary = value,
                        state = state,
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            if (state.showConfirmationDialog) ConfirmationDialog(vm, snackbarHostState, scope)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppBar(
    navController: NavController,
    vm: AttendanceLogsViewModel,
    state: AttendanceLogsState,
    show3Dots: Boolean,
) {
    TopAppBar(
        title = { Text(text = "Attendance History") },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(imageVector = iconArrowBack, contentDescription = "Back")
            }
        },
        actions = { if (show3Dots) MoreOptionsMenu(vm, state) },
    )
}

@Composable
private fun MoreOptionsMenu(vm: AttendanceLogsViewModel, state: AttendanceLogsState) {
    var showMenu by remember { mutableStateOf(false) }
    var showFilterItems by remember { mutableStateOf(false) }

    IconButton(onClick = { showMenu = true }) {
        Icon(imageVector = iconMore, contentDescription = "More Options")
    }

    AnimatedContent(targetState = showMenu, transitionSpec = { animationTransitionSpec }) {
        menuVisible ->
        DropdownMenu(
            modifier = Modifier.width(180.dp),
            expanded = menuVisible,
            onDismissRequest = { showMenu = false },
        ) {
            MainMenuItems(vm, state) { showFilterItems = it }
            FilterMenuItems(showFilterItems, vm) { showFilterItems = it }
        }
    }
}

@Composable
private fun MainMenuItems(
    vm: AttendanceLogsViewModel,
    state: AttendanceLogsState,
    setShowFilterItems: (Boolean) -> Unit,
) {
    if (state.filterByDay == null) {
        DropdownMenuItem(
            onClick = { setShowFilterItems(true) },
            text = { Text("Filter") },
            trailingIcon = { Icon(imageVector = iconFilterOff, contentDescription = "Filter") },
        )
    } else {
        DropdownMenuItem(
            modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer),
            onClick = { vm.filterLogs(null) },
            text = { Text(state.filterByDay.getDisplayName(TextStyle.FULL, Locale.ENGLISH)) },
            trailingIcon = { Icon(imageVector = iconFilterList, contentDescription = "Filter") },
        )
    }

    Spacer(Modifier.height(2.dp))
    HorizontalDivider(modifier = Modifier.fillMaxWidth(), thickness = 1.dp)
    Spacer(Modifier.height(2.dp))

    DropdownMenuItem(
        onClick = { vm.showConfirmationDialog(true) },
        text = { Text("Clear History") },
    )
}

@Composable
private fun FilterMenuItems(
    showFilterItems: Boolean,
    vm: AttendanceLogsViewModel,
    setShowFilterItems: (Boolean) -> Unit,
) {

    DropdownMenu(
        modifier = Modifier.width(180.dp),
        expanded = showFilterItems,
        onDismissRequest = { setShowFilterItems(false) },
    ) {
        DayOfWeek.entries
            .filter { it != DayOfWeek.SUNDAY }
            .forEach { day ->
                DropdownMenuItem(
                    text = { Text(day.getDisplayName(TextStyle.FULL, Locale.ENGLISH)) },
                    onClick = {
                        vm.filterLogs(day)
                        setShowFilterItems(false)
                    },
                )
            }
        DropdownMenuItem(
            text = { Text("Back") },
            onClick = { setShowFilterItems(false) },
            leadingIcon = { Icon(imageVector = iconArrowBack, contentDescription = "Go Back") },
        )
    }
}

@Composable
private fun LogCard(
    modifier: Modifier,
    date: String,
    summary: AttendanceSummary,
    state: AttendanceLogsState,
) {
    val cardColor = MaterialTheme.colorScheme.surfaceContainer
    var detailsVisible by remember { mutableStateOf(false) }

    AnimatedContent(targetState = detailsVisible, transitionSpec = { animationTransitionSpec }) {
        isClicked ->
        ElevatedCard(
            onClick = { detailsVisible = !detailsVisible },
            modifier = modifier,
            colors =
                CardDefaults.cardColors(
                    containerColor =
                        if (summary.attendedAll) {
                            cardColor.copy(green = (cardColor.green + 0.04f))
                        } else if (summary.missedAll) {
                            cardColor.copy(red = (cardColor.red + 0.07f).coerceAtMost(1f))
                        } else {
                            cardColor
                        },
                    contentColor = MaterialTheme.colorScheme.onSurface,
                ),
        ) {
            Column(
                modifier = Modifier.padding(10.dp),
                // Using AbsoluteAlignment.Left puts the text on the physical left side of device.
                // Using Alignment.Start would put the text at the beginning of the reading
                // direction.
                horizontalAlignment = AbsoluteAlignment.Left,
            ) {
                val weekDay = LocalDate.parse(date).dayOfWeek
                val child1 = weekDay.getDisplayName(TextStyle.FULL, Locale.ENGLISH).uppercase()
                val child2 = date

                AnimatedContent(
                    targetState = state.filterByDay,
                    transitionSpec = { animationTransitionSpec },
                ) { filterOn ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        if (filterOn == null) {
                            Text(child1, style = MaterialTheme.typography.titleMedium)
                            Text(
                                child2,
                                style = androidx.compose.ui.text.TextStyle(fontSize = 11.sp),
                            )
                        } else {
                            Text(child2, style = MaterialTheme.typography.titleMedium)
                            Text(
                                child1,
                                style = androidx.compose.ui.text.TextStyle(fontSize = 11.sp),
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(5.dp))

                if (isClicked) DetailedContent(summary) else DefaultContent(summary)
            }
        }
    }
}

@Composable
private fun DefaultContent(summary: AttendanceSummary) {
    Text(
        // Display attendance status.
        when {
            summary.attendedAll -> "ATTENDED ALL CLASSES"

            summary.missedAll -> "MISSED ALL CLASSES"

            else -> {
                "ATTENDED: ${summary.attendedClasses.keys.count()}     " +
                    "MISSED: ${summary.missedClasses.keys.count()}"
            }
        }
        // Display added or removed subjects.
        +
            (if (summary.subjectsAdded.isNotEmpty())
                "\n${summary.subjectsAdded.count()} SUBJECT(S) ADDED"
            else "") +
            if (summary.subjectsRemoved.isNotEmpty())
                "\n${summary.subjectsRemoved.count()} SUBJECT(S) REMOVED"
            else "",
        style = MaterialTheme.typography.bodyMedium,
        letterSpacing = 0.5.sp,
    )
}

@Composable
private fun DetailedContent(summary: AttendanceSummary) {
    HorizontalDivider(thickness = 2.dp)

    @Composable
    fun listSubjects(map: Map<AttendanceRecord, UInt>) {
        Spacer(Modifier.height(8.dp))

        for ((record, count) in map) {
            Text(
                "• " +
                    record.subject.initCap() +
                    (if (record.subjectPractical) " (P)" else "") +
                    ": $count",
                maxLines = 1,
                overflow = TextOverflow.MiddleEllipsis,
                style = androidx.compose.ui.text.TextStyle(fontSize = 14.sp),
                letterSpacing = 0.6.sp,
            )
        }
    }
    ProvideTextStyle(value = MaterialTheme.typography.titleSmall.copy(letterSpacing = 0.5.sp)) {
        if (summary.attendedClasses.keys.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(text = "ATTENDED")
            listSubjects(summary.attendedClasses)
        }
        if (summary.missedClasses.keys.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(text = "MISSED")
            listSubjects(summary.missedClasses)
        }

        if (summary.subjectsAdded.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text("SUBJECT(S) ADDED")
            listSubjects(summary.subjectsAdded)
        }
        if (summary.subjectsRemoved.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text("SUBJECT(S) REMOVED")
            listSubjects(summary.subjectsRemoved)
        }
    }
}

@Composable
private fun ConfirmationDialog(
    vm: AttendanceLogsViewModel,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope,
) {

    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = { vm.showConfirmationDialog(false) },
        title = { Text("Clear Attendance History") },
        text = { Text("Are you sure you want to delete all attendance records?") },
        confirmButton = {
            TextButton(
                onClick = {
                    scope.launch {
                        val result =
                            snackbarHostState.showSnackbar(
                                message = "Attendance records have been deleted",
                                actionLabel = "UNDO",
                                duration = SnackbarDuration.Indefinite,
                            )
                        when (result) {
                            SnackbarResult.ActionPerformed -> {}
                            SnackbarResult.Dismissed -> {
                                Log.d("delete", "Dismissed")
                                vm.deleteAttendanceHistory(context)
                            }
                        }
                    }
                    vm.showConfirmationDialog(false)
                }
            ) {
                Text("DELETE")
            }
        },
        dismissButton = {
            TextButton(onClick = { vm.showConfirmationDialog(false) }) { Text("CANCEL") }
        },
    )
}

private fun String.initCap(): String {
    return this.lowercase().split(" ").joinToString(separator = " ") { word ->
        word.replaceFirstChar { it.uppercase() }
    }
}
