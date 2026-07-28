package io.github.easeatten.ui.theme.colorscheme

import android.os.Build
import io.github.easeatten.ui.theme.colorscheme.data.defaultColorScheme

val isDynamicColorSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

enum class ColorScheme {
    DEFAULT;

    fun get(): ColorSchemeData =
        when (this) {
            DEFAULT -> defaultColorScheme
        }
}
