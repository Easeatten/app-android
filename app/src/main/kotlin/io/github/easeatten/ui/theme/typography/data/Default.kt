package io.github.easeatten.ui.theme.typography.data

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import io.github.easeatten.R
import io.github.easeatten.ui.theme.typography.TypographyData

internal val baseline = Typography()
internal val displayFontFamily =
    FontFamily(
        // Licensed under Open Font License (OFL)
        Font(R.font.stack_sans_headline_regular, FontWeight.Normal),
        Font(R.font.stack_sans_headline_bold, FontWeight.Bold),
        Font(R.font.stack_sans_headline_medium, FontWeight.Medium),
        Font(R.font.stack_sans_headline_light, FontWeight.Light),
    )

val defaultTypography =
    TypographyData(
        name = "Default",
        description = "The Original Font Style",
        typography =
            Typography(
                displayLarge =
                    baseline.displayLarge.copy(
                        fontFamily = displayFontFamily,
                        fontWeight = FontWeight.Bold,
                    ),
                displayMedium =
                    baseline.displayMedium.copy(
                        fontFamily = displayFontFamily,
                        fontWeight = FontWeight.Medium,
                    ),
                displaySmall = baseline.displaySmall.copy(fontFamily = displayFontFamily),
                headlineLarge =
                    baseline.headlineLarge.copy(
                        fontFamily = displayFontFamily,
                        fontWeight = FontWeight.Bold,
                    ),
                headlineMedium =
                    baseline.headlineMedium.copy(
                        fontFamily = displayFontFamily,
                        fontWeight = FontWeight.Medium,
                    ),
                headlineSmall = baseline.headlineSmall.copy(fontFamily = displayFontFamily),
                titleLarge =
                    baseline.titleLarge.copy(
                        fontFamily = displayFontFamily,
                        fontWeight = FontWeight.Bold,
                    ),
                titleMedium =
                    baseline.titleMedium.copy(
                        fontFamily = displayFontFamily,
                        fontWeight = FontWeight.Medium,
                    ),
                titleSmall = baseline.titleSmall.copy(fontFamily = displayFontFamily),
            ),
    )
