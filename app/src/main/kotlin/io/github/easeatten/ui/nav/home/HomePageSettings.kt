package io.github.easeatten.ui.nav.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import io.github.easeatten.data.sources.AttendanceData
import io.github.easeatten.data.sources.LoginData
import io.github.easeatten.data.sources.SettingsData
import io.github.easeatten.ui.nav.NavDestination
import io.github.easeatten.ui.theme.colorscheme.ColorScheme
import io.github.easeatten.ui.theme.colorscheme.isDynamicColorSupported
import io.github.easeatten.ui.theme.typography.Typography
import io.github.easeatten.ui.viewmodels.nav.HomeState
import io.github.easeatten.ui.viewmodels.nav.HomeViewModel

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
            modifier = Modifier.padding(20.dp),
            text = "Settings",
            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
        )

        LazyColumn {
            item {
                Text(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    text = "Appearance",
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            val modifier = Modifier.padding(horizontal = 20.dp, vertical = 15.dp)

            item { ColorSchemeSettingsOption(vm, settings, modifier) }

            item { DarkModeSettingsOption(vm, settings, modifier) }

            if (isDynamicColorSupported) {
                item { DynamicColorSettingsSwitch(vm, settings, modifier) }
            }

            item { TypographySettingsOption(vm, settings, modifier) }

            item {
                SettingsDangerButton(
                    modifier = modifier,
                    text = "Logout",
                    confirmText = "Are you sure you want to logout?",
                ) {
                    vm.logout()
                    navController.navigate(NavDestination.LOGIN.route()) { popUpTo(0) }
                }
            }
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
        Card {
            Column {
                Text(
                    modifier = Modifier.padding(20.dp),
                    text = text,
                    style = MaterialTheme.typography.titleLarge,
                )

                LazyColumn(modifier = Modifier.selectableGroup()) {
                    iterable.forEach { elem ->
                        item {
                            Box(
                                modifier =
                                    Modifier.fillMaxWidth()
                                        .selectable(
                                            role = Role.RadioButton,
                                            selected = (selected == elem),
                                            onClick = {
                                                onSelectedChange(elem)
                                                dialog = false
                                            },
                                        )
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    RadioButton(
                                        modifier = Modifier.padding(horizontal = 10.dp),
                                        selected = (selected == elem),
                                        onClick = null,
                                    )

                                    Text(
                                        modifier = Modifier.padding(horizontal = 10.dp),
                                        text = labelMap(elem),
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.padding(vertical = 10.dp))
            }
        }
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
        Card {
            Column(
                modifier = Modifier.fillMaxWidth().padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Text(text = text, style = MaterialTheme.typography.titleLarge)

                Text(text = confirmText)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(
                        onClick = {
                            dialog = false
                            onClick()
                        }
                    ) {
                        Text(text = "Yes")
                    }

                    Spacer(modifier = Modifier.padding(horizontal = 5.dp))

                    TextButton(onClick = { dialog = false }) { Text(text = "No") }
                }
            }
        }
    }
}

@Composable
internal fun DynamicColorSettingsSwitch(
    vm: HomeViewModel,
    settings: SettingsData,
    modifier: Modifier = Modifier,
) {
    SettingsSwitch(
        modifier = modifier,
        text = "Dynamic Colors",
        subtext = "Colors by Material You",
        checked = settings.themeDynamicColor,
    ) {
        vm.updateDynamicColor(!settings.themeDynamicColor)
    }
}

@Composable
internal fun ColorSchemeSettingsOption(
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
internal fun DarkModeSettingsOption(
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
internal fun TypographySettingsOption(
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
