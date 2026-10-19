package io.github.easeatten.ui.nav.attendance

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import io.github.easeatten.data.sources.AttendanceData
import io.github.easeatten.data.sources.AttendanceRecord
import io.github.easeatten.data.sources.LoginDataStore
import io.github.easeatten.data.sources.SettingsData
import io.github.easeatten.database.DatabaseRepository
import io.github.easeatten.ui.icons.iconAssignment
import io.github.easeatten.ui.icons.iconExperiment
import io.github.easeatten.ui.icons.iconOpenInBrowser
import io.github.easeatten.ui.viewmodels.nav.AttendanceState
import io.github.easeatten.ui.viewmodels.nav.AttendanceStateFilter
import io.github.easeatten.ui.viewmodels.nav.AttendanceStateOrdering
import io.github.easeatten.ui.viewmodels.nav.AttendanceViewModel
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
internal fun SubjectList(
    vm: AttendanceViewModel,
    state: AttendanceState,
    settings: SettingsData,
    attendance: AttendanceData,
) {
    val target = settings.attendanceTargetPercentage

    PullToRefreshBox(
        onRefresh = { vm.refreshAttendanceData() },
        isRefreshing = state.isAttendanceDataRefreshing,
    ) {
        LazyColumn(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            val recordsFiltered =
                attendance.records.filter {
                    when (state.filter) {
                        AttendanceStateFilter.ALL -> true
                        AttendanceStateFilter.BEHIND -> it.getScore(target) < 0
                        AttendanceStateFilter.AHEAD -> it.getScore(target) >= 0
                        AttendanceStateFilter.THEORY -> !it.subjectPractical
                        AttendanceStateFilter.PRACTICAL -> it.subjectPractical
                    }
                }
            val recordsFilteredOrdered =
                when (state.ordering) {
                    AttendanceStateOrdering.NATURAL -> recordsFiltered
                    AttendanceStateOrdering.SCORE_ASCENDING ->
                        recordsFiltered.sortedBy { it.getScore(target) * 10 + it.getPercentage() }
                    AttendanceStateOrdering.SCORE_DESCENDING ->
                        recordsFiltered.sortedByDescending {
                            it.getScore(target) * 10 + it.getPercentage()
                        }
                }

            items(recordsFilteredOrdered, { attendance.records.indexOf(it) }) { record ->
                SubjectListItem(Modifier.animateItem(), record, record.getScore(target))
            }
        }
    }
}

@Composable
internal fun SubjectListItem(modifier: Modifier = Modifier, record: AttendanceRecord, score: Int) {
    val cardColors =
        if (score < 0)
            CardDefaults.cardColors()
                .copy(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                )
        else
            CardDefaults.cardColors()
                .copy(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                )

    var dialog by remember { mutableStateOf(false) }

    Card(modifier = modifier.fillMaxWidth().clickable { dialog = true }, colors = cardColors) {
        Row(
            modifier = Modifier.fillMaxHeight().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            SubjectSummary(Modifier.fillMaxHeight().weight(1f), record, displayPercentage = true)
            SubjectScore(Modifier.fillMaxHeight().width(60.dp), score)
        }
    }

    if (!dialog) return

    Dialog(onDismissRequest = { dialog = false }) { SubjectDialogCard(record, score, cardColors) }
}

@Composable
internal fun SubjectSummary(
    modifier: Modifier = Modifier,
    record: AttendanceRecord,
    subjectMaxLines: Int = 1,
    displayPercentage: Boolean = false,
    displayTeachers: Boolean = false,
) {
    Column(modifier = modifier) {
        Text(
            text = record.subject,
            maxLines = subjectMaxLines,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.titleMedium,
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val percentageDisplay = (record.getPercentage() * 10000).roundToInt() / 100.0

            if (record.subjectPractical) {
                Icon(
                    modifier = Modifier.size(18.dp),
                    imageVector = iconExperiment,
                    contentDescription = "Practical",
                )
            } else {
                Icon(
                    modifier = Modifier.size(18.dp),
                    imageVector = iconAssignment,
                    contentDescription = "Theory",
                )
            }

            if (displayPercentage) {
                Text(text = "⋅", style = MaterialTheme.typography.labelLarge)
                Text(text = "$percentageDisplay%", style = MaterialTheme.typography.labelLarge)
            }
            if (displayTeachers) {
                val scrollState = rememberScrollState()
                Row(modifier = Modifier.horizontalScroll(scrollState)) {
                    record.professors.forEach { professor ->
                        Text(text = " • ", style = MaterialTheme.typography.labelLarge)
                        Text(text = professor, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

@Composable
internal fun SubjectScore(modifier: Modifier = Modifier, score: Int) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        val statusText = if (score < 0) "Behind" else "Ahead"

        // Apply an arbitrary threshold to prevent abnormally large numbers from being displayed.
        val absoluteScoreDisplay =
            if (score <= -100 || score >= 100) "∞" else score.absoluteValue.toString()

        Text(text = absoluteScoreDisplay, style = MaterialTheme.typography.titleLarge)
        Text(text = statusText, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
internal fun SubjectDialogCard(record: AttendanceRecord, score: Int, cardColors: CardColors) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = cardColors,
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                SubjectSummary(
                    modifier = Modifier.weight(1f),
                    record = record,
                    subjectMaxLines = 3,
                    displayTeachers = true,
                )
                LaunchLinkIcon(record)
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                thickness = 2.dp,
                color = cardColors.contentColor.copy(alpha = 0.1f),
            )

            Row {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(text = "${record.delivered}", style = MaterialTheme.typography.titleLarge)
                    Text(text = "Delivered", style = MaterialTheme.typography.labelLarge)
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(text = "${record.attended}", style = MaterialTheme.typography.titleLarge)
                    Text(text = "Attended", style = MaterialTheme.typography.labelLarge)
                }

                SubjectScore(modifier = Modifier.weight(1f), score = score)
            }
        }
    }
}

@Composable
private fun LaunchLinkIcon(record: AttendanceRecord) {
    val context = LocalContext.current
    val handler = LocalUriHandler.current
    val scope = rememberCoroutineScope()

    var showRequestLinkDialog by remember { mutableStateOf(false) }

    IconButton(
        onClick = {
            scope.launch {
                val linkString = record.getSyllabusLink()

                if (linkString != null) handler.openUri(linkString)
                else showRequestLinkDialog = true
            }
        }
    ) {
        Icon(
            modifier = Modifier.height(20.dp).width(20.dp),
            imageVector = iconOpenInBrowser,
            contentDescription = "Syllabus link",
        )
    }

    AnimatedVisibility(
        visible = showRequestLinkDialog,
        enter = fadeIn() + scaleIn(initialScale = 0.8f),
        exit = fadeOut() + scaleOut(targetScale = 0.8f),
    ) {
        RequestLinkDialog(context, scope, record.subject) { showRequestLinkDialog = false }
    }
}

@Composable
private fun RequestLinkDialog(
    context: Context,
    scope: CoroutineScope,
    subject: String,
    dismiss: () -> Unit,
) {
    AlertDialog(
        title = { Text(text = "Syllabus not found") },
        text = {
            Text(
                buildAnnotatedString {
                    append("Request syllabus for ")
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 12.sp))
                    append(subject)
                    pop()
                    append(" ?")
                }
            )
        },
        onDismissRequest = { dismiss() },
        confirmButton = {
            TextButton(
                onClick = {
                    scope.launch {
                        val loginData = context.LoginDataStore.data.first()

                        val data =
                            mapOf(
                                "Department" to loginData.departmentCode,
                                "Semester" to loginData.semester.toString(),
                                "Subject" to subject,
                            )
                        val database = DatabaseRepository<Any>("syllabus/requests")
                        database.write(
                            data,
                            addOnSuccessListener = {
                                Toast.makeText(context, "Request sent", Toast.LENGTH_SHORT).show()
                            },
                            addOnFailureListener = {
                                Toast.makeText(
                                        context,
                                        "Request sending failed",
                                        Toast.LENGTH_SHORT,
                                    )
                                    .show()
                            },
                        )
                    }
                    dismiss()
                }
            ) {
                Text("Send")
            }
        },
        dismissButton = { TextButton(onClick = { dismiss() }) { Text("Dismiss") } },
    )
}
