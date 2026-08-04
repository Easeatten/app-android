package io.github.easeatten.ui.nav.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.github.easeatten.data.sources.AttendanceData
import io.github.easeatten.data.sources.LoginData
import io.github.easeatten.data.sources.SettingsData
import io.github.easeatten.ui.nav.NavDestination
import io.github.easeatten.ui.viewmodels.nav.HomeState
import io.github.easeatten.ui.viewmodels.nav.HomeViewModel
import java.util.Calendar

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
            item { AttendanceCard(navController, attendance, settings) }
        }
    }
}

@Composable
internal fun AttendanceCard(
    navController: NavController,
    attendance: AttendanceData,
    settings: SettingsData,
) {
    ElevatedCard(
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier =
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp).clickable {
                navController.navigate(NavDestination.ATTENDANCE.route())
            },
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(15.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(text = "Attendance", style = MaterialTheme.typography.titleLarge)

                val percentageDisplay = (attendance.getAggregatePercentage() * 100).toInt()

                Text(text = "$percentageDisplay%", style = MaterialTheme.typography.titleLarge)
            }

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                thickness = 2.dp,
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Subject",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                )

                Text(
                    text = "Dues & Extras",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                )
            }

            attendance.records.forEach { record ->
                val score = record.getScore(settings.attendanceTargetPercentage)

                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = record.subject,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Spacer(modifier = Modifier.padding(horizontal = 5.dp))

                    Row {
                        Text(
                            text = score.toString(),
                            style = MaterialTheme.typography.bodyMedium,
                            color =
                                if (score < 0) MaterialTheme.colorScheme.error
                                else if (score == 0) MaterialTheme.colorScheme.secondary
                                else MaterialTheme.colorScheme.tertiary,
                        )
                    }
                }
            }
        }
    }
}
