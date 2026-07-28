package io.github.easeatten.ui.nav.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import io.github.easeatten.R
import io.github.easeatten.data.repos.SettingsRepository
import io.github.easeatten.ui.icons.iconArrowBack
import io.github.easeatten.ui.icons.iconArrowForward
import io.github.easeatten.ui.nav.NavDestination
import io.github.easeatten.ui.viewmodels.nav.OnboardingState
import io.github.easeatten.ui.viewmodels.nav.OnboardingViewModel
import io.github.easeatten.ui.viewmodels.nav.OnboardingViewModelFactory

@Composable
fun LicensePage(navController: NavController) {
    val context = LocalContext.current.applicationContext

    // Data Repositories
    val settingsRepository = remember { SettingsRepository(context) }
    // `ViewModel` Synthesis
    val vmFactory = remember { OnboardingViewModelFactory(settingsRepository) }
    val vm: OnboardingViewModel = viewModel(factory = vmFactory)

    val state by vm.state.collectAsStateWithLifecycle()
    val settings by vm.settings.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopBar(navController) },
        bottomBar = { BottomBar(navController, vm, state) },
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(it)) {
            Column(
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .verticalScroll(rememberScrollState())
            ) {
                Text(
                    modifier = Modifier.padding(vertical = 5.dp),
                    text = stringResource(R.string.license_text),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TopBar(navController: NavController) {
    TopAppBar(
        title = { Text(text = "License Agreement") },
        navigationIcon = {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(imageVector = iconArrowBack, contentDescription = "Back")
            }
        },
    )
}

@Composable
internal fun BottomBar(
    navController: NavController,
    vm: OnboardingViewModel,
    state: OnboardingState,
) {
    BottomAppBar {
        Row(
            modifier = Modifier.fillMaxSize().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = state.agreedToLicense,
                    onCheckedChange = { vm.updateAgreedToLicense(it) },
                )

                Text(text = "I Agree")
            }

            Button(
                modifier = Modifier.width(60.dp).height(60.dp),
                contentPadding = PaddingValues(0.dp),
                enabled = state.agreedToLicense,
                onClick = {
                    vm.setOnboardingDone()
                    navController.navigate(NavDestination.LOGIN.route()) { popUpTo(0) }
                },
            ) {
                Icon(imageVector = iconArrowForward, contentDescription = "Next")
            }
        }
    }
}
