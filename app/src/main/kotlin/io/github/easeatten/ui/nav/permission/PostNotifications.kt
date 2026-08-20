package io.github.easeatten.ui.nav.permission

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import io.github.easeatten.ui.nav.NavDestination

@Composable
fun RequestNotificationPermission(navController: NavController) {
    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted
            ->
            if (isGranted) {
                navController.navigate(navController) { popUpTo(id = 0) }
            }
        }

    Scaffold(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Get notifications alert?")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Button(
                    onClick = {
                        navController.navigate(NavDestination.NOTIFICATION_PERMISSION.route()) {
                            popUpTo(id = 0)
                        }
                    }
                ) {
                    Text("Skip")
                }

                Button(
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            navController.navigate(NavDestination.NOTIFICATION_PERMISSION.route()) {
                                popUpTo(id = 0)
                            }
                        }
                    }
                ) {
                    Text("Allow")
                }
            }
        }
    }
}
