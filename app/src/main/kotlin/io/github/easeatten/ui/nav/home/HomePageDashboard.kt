package io.github.easeatten.ui.nav.home

import android.icu.util.Calendar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.github.easeatten.data.sources.AttendanceData
import io.github.easeatten.data.sources.LoginData
import io.github.easeatten.data.sources.SettingsData
import io.github.easeatten.ui.nav.NavDestination
import io.github.easeatten.ui.viewmodels.nav.HomeState
import io.github.easeatten.ui.viewmodels.nav.HomeViewModel
import kotlin.math.roundToInt

@Composable
@Suppress("UnusedParameter") // Parameters are as per Composable in [`HomeDestination`]
fun HomePageDashboard(
    navController: NavController,
    homeNavController: NavController,
    vm: HomeViewModel,
    state: HomeState,
    settings: SettingsData,
    login: LoginData,
    attendance: AttendanceData,
) {
    if (!attendance.valid) return

    val greeting =
        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 5..11 -> "Morning"
            in 12..16 -> "Afternoon"
            else -> "Evening"
        }
    val firstName =
        attendance.name.substringBefore(' ').lowercase().replaceFirstChar { it.uppercase() }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 15.dp),
            text = "Good $greeting,\n$firstName",
            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
        )

        LazyVerticalStaggeredGrid(columns = StaggeredGridCells.Adaptive(300.dp)) {
            item { AttendanceCard(navController, settings, attendance) }
        }
    }
}

@Composable
internal fun AttendanceCard(
    navController: NavController,
    settings: SettingsData,
    attendance: AttendanceData,
) {
    ElevatedCard(
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier =
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp).clickable {
                navController.navigate(NavDestination.ATTENDANCE.route())
            },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(15.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Attendance",
                style = MaterialTheme.typography.titleLarge,
            )

            AttendanceCardInfo(settings, attendance)
        }
    }
}

@Composable
internal fun AttendanceCardInfo(settings: SettingsData, attendance: AttendanceData) {
    val percentage = attendance.getAggregatePercentage()
    val target = settings.attendanceTargetPercentage

    val percentageDisplay = (percentage * 1000).roundToInt() / 10.0
    val targetDisplay = (target * 100).roundToInt()

    val subjectsBelowTarget = attendance.records.count { it.getScore(target) < 0 }
    val subjectsAboveTarget = attendance.records.count { it.getScore(target) >= 0 }
    val attended = attendance.records.sumOf { it.attended }
    val delivered = attendance.records.sumOf { it.delivered }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(modifier = Modifier.fillMaxHeight().weight(1f)) {
            Text(
                text = "Aggregate – $percentageDisplay%",
                style = MaterialTheme.typography.titleMedium,
            )

            Text(text = "Target – $targetDisplay%", style = MaterialTheme.typography.titleMedium)

            Text(
                text = "∙ Below for $subjectsBelowTarget subject(s)",
                maxLines = 1,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelLarge,
            )

            Text(
                text = "∙ Above for $subjectsAboveTarget subject(s)",
                maxLines = 1,
                color = MaterialTheme.colorScheme.tertiary,
                style = MaterialTheme.typography.labelLarge,
            )
        }

        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                modifier = Modifier.size(100.dp),
                progress = { percentage },
                strokeWidth = 5.dp,
            )

            Row(verticalAlignment = Alignment.Bottom) {
                Text(text = "$attended", style = MaterialTheme.typography.titleLarge)
                Text(text = "/$delivered", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
