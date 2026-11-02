@file:Suppress("TooManyFunctions")

package io.github.easeatten.ui.nav.home

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import io.github.easeatten.data.sources.AttendanceData
import io.github.easeatten.data.sources.LoginData
import io.github.easeatten.data.sources.SettingsData
import io.github.easeatten.ui.common.ConfirmDialogCard
import io.github.easeatten.ui.common.RadioButtonDialogCard
import io.github.easeatten.ui.nav.NavDestination
import io.github.easeatten.ui.theme.colorscheme.ColorScheme
import io.github.easeatten.ui.theme.colorscheme.isDynamicColorSupported
import io.github.easeatten.ui.theme.typography.Typography
import io.github.easeatten.ui.viewmodels.nav.HomeState
import io.github.easeatten.ui.viewmodels.nav.HomeViewModel
import io.github.easeatten.workers.AttendanceSync

@Composable
@Suppress("UnusedParameter") // Parameters are as per Composable in [`HomeDestination`]
fun HomePageSettings(
    navController: NavController,
    homeNavController: NavController,
    vm: HomeViewModel,
    state: HomeState,
    settings: SettingsData,
    login: LoginData,
    attendance: AttendanceData,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 15.dp),
            text = "Settings",
            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
        )

        LazyColumn {
            val sectionModifier = Modifier.padding(horizontal = 20.dp)
            val itemModifier = Modifier.padding(horizontal = 20.dp, vertical = 15.dp)

            // Appearance configurations.
            item { SectionText(sectionModifier, "Appearance") }
            item { ColorSchemeOption(vm, settings, itemModifier) }
            item { DarkModeOption(vm, settings, itemModifier) }
            item { DynamicColorSwitch(vm, settings, itemModifier) }
            item { TypographyOption(vm, settings, itemModifier) }
            // Behavioural configurations.
            item { SectionText(sectionModifier, "Background & sync") }
            item { AutoRefreshSwitch(vm, settings, itemModifier) }
            item { BatteryOptimizationRequest(vm, settings) }

            item { LogoutButton(navController, vm, itemModifier) }
        }
    }
}

@Composable
internal fun SettingsSwitch(
    modifier: Modifier = Modifier,
    text: String,
    subtext: String,
    enabled: Boolean = true,
    checked: Boolean,
    onCheckedToggle: () -> Unit,
) {
    Box(modifier = Modifier.clickable { onCheckedToggle() }) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = modifier.weight(1f)) {
                Text(text = text, style = MaterialTheme.typography.titleLarge)
                Text(text = subtext, style = MaterialTheme.typography.bodyMedium)
            }

            Switch(
                modifier = modifier,
                enabled = enabled,
                checked = checked,
                onCheckedChange = null,
            )
        }
    }
}

@Composable
internal fun <T> SettingsOption(
    modifier: Modifier = Modifier,
    text: String,
    disabledSubtext: String? = null,
    iterable: Iterable<T>,
    labelMap: (T) -> String,
    enabled: Boolean = true,
    selected: T,
    onSelectedChange: (T) -> Unit,
) {
    var dialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.clickable { if (enabled) dialog = true }) {
        val subtext =
            if (enabled || disabledSubtext == null) labelMap(selected) else disabledSubtext

        Box(modifier = modifier) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = text, style = MaterialTheme.typography.titleLarge)
                Text(text = subtext, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }

    if (!enabled || !dialog) return

    Dialog(onDismissRequest = { dialog = false }) {
        RadioButtonDialogCard(
            modifier = Modifier.fillMaxWidth(),
            title = text,
            options = iterable,
            optionsToLabels = labelMap,
            selected = selected,
            onSelectedChange = onSelectedChange,
        )
    }
}

@Composable
internal fun SettingsDangerButton(
    modifier: Modifier = Modifier,
    text: String,
    confirmText: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    var dialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Button(
            colors =
                ButtonDefaults.buttonColors()
                    .copy(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    ),
            onClick = { dialog = true },
        ) {
            Text(text = text)
        }
    }

    if (!enabled || !dialog) return

    Dialog(onDismissRequest = { dialog = false }) {
        ConfirmDialogCard(
            modifier = Modifier.fillMaxWidth(),
            title = text,
            message = confirmText,
            positiveButtonText = "Yes",
            negativeButtonText = "No",
            onPositiveButtonClick = onClick,
            onNegativeButtonClick = { dialog = false },
        )
    }
}

@Composable
internal fun SectionText(modifier: Modifier = Modifier, text: String) {
    Text(modifier = modifier, text = text, color = MaterialTheme.colorScheme.primary)
}

@Composable
internal fun DynamicColorSwitch(
    vm: HomeViewModel,
    settings: SettingsData,
    modifier: Modifier = Modifier,
) {
    if (isDynamicColorSupported) {
        SettingsSwitch(
            modifier = modifier,
            text = "Dynamic Colors",
            subtext = "Colors by Material You",
            checked = settings.themeDynamicColor,
        ) {
            vm.updateDynamicColor(!settings.themeDynamicColor)
        }
    }
}

@Composable
internal fun ColorSchemeOption(
    vm: HomeViewModel,
    settings: SettingsData,
    modifier: Modifier = Modifier,
) {
    SettingsOption(
        modifier = modifier,
        text = "Color Scheme",
        disabledSubtext = "Overridden by Dynamic Colors",
        iterable = ColorScheme.entries,
        labelMap = { it.get().name },
        enabled = !settings.themeDynamicColor,
        selected = settings.themeColorScheme,
    ) {
        vm.updateColorScheme(it)
    }
}

@Composable
internal fun DarkModeOption(
    vm: HomeViewModel,
    settings: SettingsData,
    modifier: Modifier = Modifier,
) {
    SettingsOption(
        modifier = modifier,
        text = "Dark Mode",
        iterable = listOf(null, false, true),
        labelMap = {
            if (it == true) {
                "Enabled"
            } else if (it == false) {
                "Disabled"
            } else {
                "Follows System"
            }
        },
        selected = settings.themeDarkMode,
    ) {
        vm.updateDarkMode(it)
    }
}

@Composable
internal fun TypographyOption(
    vm: HomeViewModel,
    settings: SettingsData,
    modifier: Modifier = Modifier,
) {
    SettingsOption(
        modifier = modifier,
        text = "Typography",
        iterable = Typography.entries,
        labelMap = { it.get().name },
        selected = settings.themeTypography,
    ) {
        vm.updateTypography(it)
    }
}

@Composable
internal fun AutoRefreshSwitch(
    vm: HomeViewModel,
    settings: SettingsData,
    modifier: Modifier = Modifier,
) {
    val applicationContext = LocalContext.current.applicationContext
    val workManager = WorkManager.getInstance(applicationContext)

    SettingsSwitch(
        modifier = modifier,
        text = "Auto Refresh",
        subtext = "Daily refresh at 00:00 hours",
        checked = settings.autoRefreshAttendance,
    ) {
        // If auto refresh was off, start the first work.
        if (!settings.autoRefreshAttendance) {
            val workReq = AttendanceSync.Work.request()

            workManager.enqueueUniqueWork(
                uniqueWorkName = AttendanceSync.WORK_TAG,
                existingWorkPolicy = ExistingWorkPolicy.KEEP,
                request = workReq,
            )
        }
        // Else cancel work(s), if any.
        else workManager.cancelAllWorkByTag(AttendanceSync.WORK_TAG)
        // Then update in the settings.
        // Doing this before the if-else block could give unexpected results,
        // as the update function does not block the thread.
        vm.updateAutoRefreshPreference(!settings.autoRefreshAttendance)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BatteryOptimizationRequest(vm: HomeViewModel, setting: SettingsData) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val state by vm.state.collectAsStateWithLifecycle()

    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(setting.autoRefreshAttendance) }
    var isExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(setting.autoRefreshAttendance) {
        showBottomSheet = setting.autoRefreshAttendance
    }

    // The purpose of using DisposableEffect is to avoid recreation of new lifecycle observer on
    // recomposition,
    // and removal of the observer on the disposal of composition.
    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            // Only show button after the user resumes activity.
            if (event == Lifecycle.Event.ON_RESUME) {
                vm.updateBatteryOptimizationsSettings(context)
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (state.isIgnoringBatteryOptimization)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Button(
                onClick = {
                    context.startActivity(
                        Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                    )
                }
            ) {
                Text("Turn on battery optimization")
            }
        }
    else if (showBottomSheet)
        ModalBottomSheet(onDismissRequest = { showBottomSheet = false }, sheetState = sheetState) {
            Column(modifier = Modifier.padding(10.dp)) {
                ModalSheetContent(context, isExpanded) { isExpanded = !isExpanded }
            }
        }
}

@Composable
private fun ModalSheetContent(context: Context, isExpanded: Boolean, toggle: () -> Unit) {
    val expandCollapseText = if (!isExpanded) "Understand why" else "Show less"

    Text("ACTION NEEDED", style = MaterialTheme.typography.titleLarge, letterSpacing = 0.5.sp)
    Spacer(Modifier.height(10.dp))

    Text(
        buildAnnotatedString {
            append("Allow Easeatten to ")
            pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
            append("Ignore Battery Optimization.\n")
            pop()
            append("This is highly recommended to auto-sync attendance in the background.")
        }
    )

    if (isExpanded) {
        Spacer(Modifier.height(10.dp))
        Box(
            modifier =
                Modifier.clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    textAlign = TextAlign.Start,
                    text =
                        buildAnnotatedString {
                            pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                            append("Power Saving Mode ")
                            pop()
                            append("or ")
                            pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                            append("Data Saver ")
                            pop()
                            append(
                                "restricts background activity or network access, " +
                                    "preventing Easeatten from syncing attendance automatically " +
                                    "in the background."
                            )
                            append(
                                " To keep attendance history up to date allowing the app to" +
                                    " ignore battery optimization is highly recommended."
                            )
                        },
                )
            }
        }
    }
    Spacer(Modifier.height(8.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Absolute.Right) {
        Text(
            modifier = Modifier.clickable(onClick = toggle),
            text = expandCollapseText,
            color = MaterialTheme.colorScheme.tertiary,
            style = MaterialTheme.typography.labelMedium,
        )
    }

    Spacer(Modifier.height(10.dp))
    RequestOptimizationButton(context)
}

@Composable
private fun RequestOptimizationButton(context: Context) {
    Button(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            try {
                context.startActivity(
                    Intent(
                        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                        "package:${context.packageName}".toUri(),
                    )
                )
            } catch (e: ActivityNotFoundException) {
                Log.e("Activity", "Activity not found:$e")
                context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
            }
        },
    ) {
        Text("Allow")
    }
}

@Composable
internal fun LogoutButton(
    navController: NavController,
    vm: HomeViewModel,
    modifier: Modifier = Modifier,
) {
    SettingsDangerButton(
        modifier = modifier,
        text = "Logout",
        confirmText = "Are you sure you want to logout?",
    ) {
        vm.logout()
        navController.navigate(NavDestination.LOGIN.route()) { popUpTo(0) }
    }
}
