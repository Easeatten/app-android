package io.github.easeatten.ui.widgets

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.components.CircleIconButton
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.components.TitleBar
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import io.github.easeatten.MainActivity
import io.github.easeatten.R
import io.github.easeatten.data.repos.SettingsRepository
import io.github.easeatten.data.repos.UserRepository
import io.github.easeatten.data.sources.AttendanceData
import io.github.easeatten.data.sources.LoginData
import io.github.easeatten.data.sources.SettingsData
import io.github.easeatten.ui.theme.GlanceThemeManager

class AttendanceGlance : GlanceAppWidget() {
    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val userRepository = remember { UserRepository(context) }
            val settingsRepository = remember { SettingsRepository(context) }

            val attendanceState = userRepository.attendanceFlow.collectAsState(AttendanceData())
            val attendance = attendanceState.value
            val settingsState = settingsRepository.settingsFlow.collectAsState(SettingsData())
            val settings = settingsState.value
            val loginState = userRepository.loginFlow.collectAsState(LoginData())
            val login = loginState.value

            GlanceThemeManager(settings) { Content(settings, login, attendance) }
        }
    }

    @Composable
    private fun ErrorContent(errorImage: ImageProvider, errorDescription: String) {
        Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Image(
                provider = errorImage,
                contentDescription = errorDescription,
                colorFilter = ColorFilter.tint(GlanceTheme.colors.error),
            )
        }
    }

    @Composable
    private fun LoadingContent() {
        Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = GlanceTheme.colors.primary)
        }
    }

    @Composable
    private fun Content(settings: SettingsData, login: LoginData, attendance: AttendanceData) {
        val titleBarHeight = 50.dp // Approximation
        val contentHeight = LocalSize.current.height - titleBarHeight
        val showContentTitleBar = contentHeight > titleBarHeight
        val showContentStatusRowExtraInfo = contentHeight > 60.dp
        val showContentSubjectList = contentHeight > 100.dp

        Scaffold(
            modifier =
                GlanceModifier.fillMaxSize()
                    .padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
                    .clickable(actionStartActivity<MainActivity>()),
            backgroundColor = GlanceTheme.colors.surface,
            titleBar = { if (showContentTitleBar) ContentTitleBar() },
        ) {
            if (!settings.valid || !login.valid) {
                LoadingContent()
                return@Scaffold
            }

            if (!login.loggedIn) {
                ErrorContent(ImageProvider(R.drawable.baseline_login_24px), "Login Required")
                return@Scaffold
            }

            if (!attendance.valid) {
                ErrorContent(ImageProvider(R.drawable.baseline_error_24px), "Attendance Error")
                return@Scaffold
            }

            val target = settings.attendanceTargetPercentage

            Column(
                modifier =
                    if (showContentSubjectList) GlanceModifier.fillMaxSize().padding(bottom = 10.dp)
                    else GlanceModifier.fillMaxSize(),
                verticalAlignment =
                    if (showContentSubjectList) Alignment.Top else Alignment.CenterVertically,
            ) {
                ContentStatusRow(attendance, target, showContentStatusRowExtraInfo)

                if (showContentSubjectList) {
                    Spacer(modifier = GlanceModifier.height(10.dp))
                    ContentSubjectList(attendance, target)
                }
            }
        }
    }

    @Composable
    private fun ContentTitleBar() {
        TitleBar(
            startIcon = ImageProvider(R.drawable.ic_launcher_foreground),
            title = "Attendance",
            iconColor = GlanceTheme.colors.primary,
            textColor = GlanceTheme.colors.onSurface,
            actions = {
                Box {
                    CircleIconButton(
                        imageProvider = ImageProvider(R.drawable.baseline_sync_24px),
                        contentDescription = "Sync",
                        contentColor = GlanceTheme.colors.secondary,
                        backgroundColor = null,
                        onClick = actionRunCallback<AttendanceGlanceRefreshAction>(),
                    )
                }
            },
        )
    }

    @Composable
    private fun ContentStatusRow(attendance: AttendanceData, target: Float, extraInfo: Boolean) {
        val subjectsBehind = attendance.records.count { it.getScore(target) < 0 }
        val subjectsOnPar = attendance.records.count { it.getScore(target) == 0 }

        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val statusOkay = subjectsBehind == 0

            Image(
                provider =
                    ImageProvider(
                        if (statusOkay) R.drawable.baseline_thumb_up_24px
                        else R.drawable.baseline_thumb_down_24px
                    ),
                contentDescription = if (statusOkay) "Attendance Okay" else "Attendance Not Okay",
                colorFilter =
                    ColorFilter.tint(
                        if (statusOkay) GlanceTheme.colors.tertiary else GlanceTheme.colors.error
                    ),
            )

            Spacer(modifier = GlanceModifier.width(10.dp))

            Column {
                if (subjectsBehind > 0) ContentStatusRowSubjectsBehindText(subjectsBehind)
                else ContentStatusRowAggregateText(attendance.getAggregatePercentage())

                if (extraInfo && subjectsOnPar > 0) {
                    ContentStatusRowSubjectsOnParText(subjectsOnPar)
                }
            }
        }
    }

    @Composable
    private fun ContentStatusRowAggregateText(value: Float) {
        val percentageDisplay = (value * 100).toInt()

        Text(
            modifier = GlanceModifier.fillMaxWidth(),
            text = "$percentageDisplay% aggregate",
            maxLines = 1,
            style =
                TextStyle(
                    fontSize = 14.sp,
                    color = GlanceTheme.colors.onSurface,
                    fontWeight = FontWeight.Bold,
                ),
        )
    }

    @Composable
    private fun ContentStatusRowSubjectsBehindText(value: Int) {
        Text(
            modifier = GlanceModifier.fillMaxWidth(),
            text = "Behind: $value subject(s)",
            maxLines = 1,
            style =
                TextStyle(
                    fontSize = 14.sp,
                    color = GlanceTheme.colors.onSurface,
                    fontWeight = FontWeight.Bold,
                ),
        )
    }

    @Composable
    private fun ContentStatusRowSubjectsOnParText(value: Int) {
        Text(
            modifier = GlanceModifier.fillMaxWidth(),
            text = "On Par: $value subject(s)",
            maxLines = 1,
            style =
                TextStyle(
                    fontSize = 14.sp,
                    color = GlanceTheme.colors.onSurface,
                    fontWeight = FontWeight.Bold,
                ),
        )
    }

    @Composable
    private fun ContentSubjectList(attendance: AttendanceData, target: Float) {
        LazyColumn(modifier = GlanceModifier.cornerRadius(10.dp)) {
            val recordsByScore = attendance.records.sortedBy { it.getScore(target) }

            items(recordsByScore) {
                Box(modifier = GlanceModifier.padding(vertical = 2.dp)) {
                    val score = it.getScore(target)
                    val backgroundColor =
                        if (score < 0) GlanceTheme.colors.errorContainer
                        else GlanceTheme.colors.tertiaryContainer
                    val foregroundColor =
                        if (score < 0) GlanceTheme.colors.onErrorContainer
                        else GlanceTheme.colors.onTertiaryContainer

                    Column(
                        modifier =
                            GlanceModifier.fillMaxSize()
                                .cornerRadius(10.dp)
                                .padding(10.dp)
                                .background(backgroundColor)
                    ) {
                        Text(
                            text = it.subject,
                            maxLines = 1,
                            style =
                                TextStyle(
                                    fontSize = 14.sp,
                                    color = foregroundColor,
                                    fontWeight = FontWeight.Bold,
                                ),
                        )

                        Row {
                            val statusText = if (score < 0) "${-score} behind" else "$score ahead"
                            val style = TextStyle(fontSize = 13.sp, color = foregroundColor)

                            Text(text = statusText, maxLines = 1, style = style)

                            if (it.subjectPractical) {
                                Text(text = " ⋅ ", style = style)
                                Text(text = "Practical", style = style)
                            }
                        }
                    }
                }
            }
        }
    }
}

class AttendanceGlanceRefreshAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        UserRepository(context).refreshAttendanceData()
        AttendanceGlance().update(context, glanceId)
    }
}

class AttendanceGlanceReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = AttendanceGlance()
}
