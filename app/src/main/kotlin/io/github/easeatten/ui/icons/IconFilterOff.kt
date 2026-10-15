package io.github.easeatten.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("CheckReturnValue")
public val iconFilterOff: ImageVector
    get() {
        if (_filter_list_off != null) {
            return _filter_list_off!!
        }
        _filter_list_off =
            ImageVector.Builder(
                    name = "filter_list_off",
                    defaultWidth = 24.dp,
                    defaultHeight = 24.dp,
                    viewportWidth = 24f,
                    viewportHeight = 24f,
                )
                .apply {
                    path(
                        fill = SolidColor(Color.Black),
                        fillAlpha = 1f,
                        stroke = null,
                        strokeAlpha = 1f,
                        strokeLineWidth = 1f,
                        strokeLineCap = StrokeCap.Butt,
                        strokeLineJoin = StrokeJoin.Bevel,
                        strokeLineMiter = 1f,
                        pathFillType = PathFillType.Companion.NonZero,
                    ) {
                        moveTo(19.78f, 22.63f)
                        lineTo(1.38f, 4.22f)
                        lineTo(2.8f, 2.8f)
                        lineTo(21.2f, 21.2f)
                        lineToRelative(-1.43f, 1.43f)
                        close()
                        moveTo(15.83f, 13f)
                        lineToRelative(-2f, -2f)
                        horizontalLineTo(18f)
                        verticalLineToRelative(2f)
                        horizontalLineTo(15.83f)
                        close()
                        moveToRelative(-5f, -5f)
                        lineToRelative(-2f, -2f)
                        horizontalLineTo(21f)
                        verticalLineTo(8f)
                        horizontalLineTo(10.83f)
                        close()
                        moveTo(10f, 18f)
                        verticalLineTo(16f)
                        horizontalLineToRelative(4f)
                        verticalLineToRelative(2f)
                        horizontalLineTo(10f)
                        close()
                        moveTo(6f, 13f)
                        verticalLineTo(11f)
                        horizontalLineToRelative(4.15f)
                        verticalLineToRelative(2f)
                        horizontalLineTo(6f)
                        close()
                        moveTo(3f, 8f)
                        verticalLineTo(6f)
                        horizontalLineTo(5.15f)
                        verticalLineTo(8f)
                        horizontalLineTo(3f)
                        close()
                    }
                }
                .build()
        return _filter_list_off!!
    }

private var _filter_list_off: ImageVector? = null
