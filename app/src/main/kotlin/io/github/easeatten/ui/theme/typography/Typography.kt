package io.github.easeatten.ui.theme.typography

import io.github.easeatten.ui.theme.typography.data.defaultTypography
import io.github.easeatten.ui.theme.typography.data.systemTypography

enum class Typography {
  DEFAULT,
  SYSTEM,
  ;

  fun get(): TypographyData =
      when (this) {
        DEFAULT -> defaultTypography
        SYSTEM -> systemTypography
      }
}
