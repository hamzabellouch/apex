package com.tkno.apex.ui.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

public val LeftPanelOpen: ImageVector
    get() {
        if (_leftPanelOpen != null) {
            return _leftPanelOpen!!
        }
        _leftPanelOpen = ImageVector.Builder(
            name = "left_panel_open",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1f,
                stroke = null,
                strokeAlpha = 1f,
                strokeLineWidth = 1f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Bevel,
                strokeLineMiter = 1f,
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(12.5f, 8f)
                verticalLineToRelative(8f)
                lineToRelative(4f, -4f)
                lineToRelative(-4f, -4f)
                close()
                moveTo(5f, 21f)
                quadTo(4.18f, 21f, 3.59f, 20.41f)
                reflectiveQuadTo(3f, 19f)
                verticalLineTo(5f)
                quadTo(3f, 4.17f, 3.59f, 3.59f)
                reflectiveQuadTo(5f, 3f)
                horizontalLineTo(19f)
                quadToRelative(0.83f, 0f, 1.41f, 0.59f)
                reflectiveQuadTo(21f, 5f)
                verticalLineTo(19f)
                quadToRelative(0f, 0.82f, -0.59f, 1.41f)
                reflectiveQuadTo(19f, 21f)
                horizontalLineTo(5f)
                close()
                moveTo(8f, 19f)
                verticalLineTo(5f)
                horizontalLineTo(5f)
                verticalLineTo(19f)
                horizontalLineTo(8f)
                close()
                moveToRelative(2f, 0f)
                horizontalLineToRelative(9f)
                verticalLineTo(5f)
                horizontalLineTo(10f)
                verticalLineTo(19f)
                close()
                moveTo(8f, 19f)
                horizontalLineTo(5f)
                horizontalLineTo(8f)
                close()
            }
        }.build()
        return _leftPanelOpen!!
    }

private var _leftPanelOpen: ImageVector? = null

public val LeftPanelClose: ImageVector
    get() {
        if (_leftPanelClose != null) {
            return _leftPanelClose!!
        }
        _leftPanelClose = ImageVector.Builder(
            name = "left_panel_close",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1f,
                stroke = null,
                strokeAlpha = 1f,
                strokeLineWidth = 1f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Bevel,
                strokeLineMiter = 1f,
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(16.5f, 16f)
                verticalLineTo(8f)
                lineToRelative(-4f, 4f)
                lineToRelative(4f, 4f)
                close()
                moveTo(5f, 21f)
                quadTo(4.18f, 21f, 3.59f, 20.41f)
                reflectiveQuadTo(3f, 19f)
                verticalLineTo(5f)
                quadTo(3f, 4.17f, 3.59f, 3.59f)
                reflectiveQuadTo(5f, 3f)
                horizontalLineTo(19f)
                quadToRelative(0.83f, 0f, 1.41f, 0.59f)
                reflectiveQuadTo(21f, 5f)
                verticalLineTo(19f)
                quadToRelative(0f, 0.82f, -0.59f, 1.41f)
                reflectiveQuadTo(19f, 21f)
                horizontalLineTo(5f)
                close()
                moveTo(8f, 19f)
                verticalLineTo(5f)
                horizontalLineTo(5f)
                verticalLineTo(19f)
                horizontalLineTo(8f)
                close()
                moveToRelative(2f, 0f)
                horizontalLineToRelative(9f)
                verticalLineTo(5f)
                horizontalLineTo(10f)
                verticalLineTo(19f)
                close()
                moveTo(8f, 19f)
                horizontalLineTo(5f)
                horizontalLineTo(8f)
                close()
            }
        }.build()
        return _leftPanelClose!!
    }

private var _leftPanelClose: ImageVector? = null

public val DataUsage: ImageVector
    get() {
        if (_dataUsage != null) {
            return _dataUsage!!
        }
        _dataUsage = ImageVector.Builder(
            name = "data_usage",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(
                fill = SolidColor(Color.Black),
                fillAlpha = 1f,
                stroke = null,
                strokeAlpha = 1f,
                strokeLineWidth = 1f,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Bevel,
                strokeLineMiter = 1f,
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(12f, 22f)
                quadTo(9.93f, 22f, 8.1f, 21.21f)
                quadTo(6.28f, 20.43f, 4.93f, 19.06f)
                reflectiveQuadTo(2.79f, 15.88f)
                reflectiveQuadTo(2f, 12f)
                quadTo(2f, 8.07f, 4.6f, 5.25f)
                quadTo(7.2f, 2.42f, 11f, 2.05f)
                verticalLineToRelative(3f)
                quadTo(8.43f, 5.4f, 6.71f, 7.36f)
                quadTo(5f, 9.32f, 5f, 12f)
                quadToRelative(0f, 2.9f, 2.05f, 4.95f)
                reflectiveQuadTo(12f, 19f)
                quadToRelative(1.65f, 0f, 3.09f, -0.7f)
                quadToRelative(1.44f, -0.7f, 2.41f, -1.9f)
                lineToRelative(2.6f, 1.5f)
                quadToRelative(-1.35f, 1.88f, -3.48f, 2.99f)
                reflectiveQuadTo(12f, 22f)
                close()
                moveToRelative(9.15f, -5.95f)
                lineToRelative(-2.6f, -1.5f)
                quadToRelative(0.23f, -0.6f, 0.34f, -1.24f)
                reflectiveQuadTo(19f, 12f)
                quadTo(19f, 9.32f, 17.29f, 7.36f)
                reflectiveQuadTo(13f, 5.05f)
                verticalLineToRelative(-3f)
                quadToRelative(3.8f, 0.38f, 6.4f, 3.2f)
                quadTo(22f, 8.07f, 22f, 12f)
                quadToRelative(0f, 1.1f, -0.2f, 2.13f)
                reflectiveQuadToRelative(-0.65f, 1.93f)
                close()
            }
        }.build()
        return _dataUsage!!
    }

private var _dataUsage: ImageVector? = null
