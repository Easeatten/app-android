package io.github.easeatten.data.sources

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStore
import io.github.easeatten.ui.theme.colorscheme.ColorScheme
import io.github.easeatten.ui.theme.typography.Typography
import java.io.InputStream
import java.io.OutputStream
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json

@Serializable
data class SettingsData(
    val valid: Boolean = false,
    // User has gone through the onboarding section of the app.
    val onboardingDone: Boolean = false,
    // The color scheme used as part of the app theme.
    val themeColorScheme: ColorScheme = ColorScheme.DEFAULT,
    // Whether dark theme is preferred, null represents system's choice.
    val themeDarkMode: Boolean? = null,
    // Whether dynamic color (Material You, Android 12+) is enabled.
    val themeDynamicColor: Boolean = false,
    // The fonts used as part of the app theme.
    val themeTypography: Typography = Typography.DEFAULT,
    // The per-subject percentage threshold set by the user.
    val attendanceTargetPercentage: Float = 0.75f,
    // The local time at which attendance is refreshed.
    @Serializable(with = LocalTimeSerializer::class)
    val attendanceRefreshTime: LocalTime = LocalTime.of(0, 0),
)

object SettingsSerializer : Serializer<SettingsData> {
    override val defaultValue = SettingsData(valid = true)

    override suspend fun readFrom(input: InputStream): SettingsData {
        try {
            return Json.decodeFromString<SettingsData>(input.readBytes().decodeToString())
        } catch (serialization: SerializationException) {
            throw CorruptionException("corrupted settings data:", serialization)
        }
    }

    override suspend fun writeTo(t: SettingsData, output: OutputStream) {
        withContext(Dispatchers.IO) {
            output.write(Json.encodeToString(t.copy(valid = true)).encodeToByteArray())
        }
    }
}

val Context.SettingsDataStore: DataStore<SettingsData> by
    dataStore(
        fileName = "settings.json",
        serializer = SettingsSerializer,
        corruptionHandler = ReplaceFileCorruptionHandler { SettingsSerializer.defaultValue },
    )

object LocalTimeSerializer : KSerializer<LocalTime> {
    // Pattern for 24hr format.
    private val formatter = DateTimeFormatter.ofPattern("HH:mm")

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("io.github.easeatten.LocalTime", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalTime) {
        encoder.encodeString(value.format(formatter))
    }

    override fun deserialize(decoder: Decoder): LocalTime {
        return LocalTime.parse(decoder.decodeString(), formatter)
    }
}
