package io.github.easeatten.ui.nav.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.github.easeatten.ui.icons.iconArrowForward
import io.github.easeatten.ui.nav.NavDestination

@Composable
fun LandingPage(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
        Text(text = "Welcome to easeatten.", style = MaterialTheme.typography.displayLarge)

        Button(
            onClick = { navController.navigate(NavDestination.ONBOARDING_LICENSE.route()) },
            modifier = Modifier.width(60.dp).height(60.dp),
            contentPadding = PaddingValues(0.dp),
        ) {
            Icon(imageVector = iconArrowForward, contentDescription = "Next")
        }
    }
}
