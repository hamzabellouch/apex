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

public val DigitalWellbeing: ImageVector
    get() {
        if (_digitalWellbeing != null) {
            return _digitalWellbeing!!
        }
        _digitalWellbeing = ImageVector.Builder(
            name = "digital_wellbeing",
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
                moveTo(9.53f, 7.97f)
                quadTo(8.5f, 6.95f, 8.5f, 5.5f)
                reflectiveQuadTo(9.53f, 3.02f)
                reflectiveQuadTo(12f, 2f)
                reflectiveQuadToRelative(2.48f, 1.02f)
                reflectiveQuadTo(15.5f, 5.5f)
                reflectiveQuadTo(14.48f, 7.97f)
                reflectiveQuadTo(12f, 9f)
                reflectiveQuadTo(9.53f, 7.97f)
                close()
                moveTo(13.06f, 6.56f)
                quadTo(13.5f, 6.13f, 13.5f, 5.5f)
                reflectiveQuadTo(13.06f, 4.44f)
                reflectiveQuadTo(12f, 4f)
                reflectiveQuadTo(10.94f, 4.44f)
                reflectiveQuadTo(10.5f, 5.5f)
                reflectiveQuadToRelative(0.44f, 1.06f)
                reflectiveQuadTo(12f, 7f)
                reflectiveQuadTo(13.06f, 6.56f)
                close()
                moveTo(12f, 22f)
                lineTo(6f, 16f)
                quadTo(5.5f, 15.5f, 5.25f, 14.88f)
                reflectiveQuadTo(5f, 13.5f)
                quadTo(5f, 12.02f, 6.01f, 11.01f)
                reflectiveQuadTo(8.5f, 10f)
                quadToRelative(0.72f, 0f, 1.34f, 0.27f)
                reflectiveQuadToRelative(1.11f, 0.78f)
                lineTo(12f, 12.1f)
                lineToRelative(1.05f, -1.05f)
                quadToRelative(0.5f, -0.5f, 1.11f, -0.78f)
                reflectiveQuadTo(15.5f, 10f)
                quadToRelative(1.48f, 0f, 2.49f, 1.01f)
                reflectiveQuadTo(19f, 13.5f)
                quadToRelative(0f, 0.75f, -0.25f, 1.38f)
                reflectiveQuadTo(18f, 16f)
                lineToRelative(-6f, 6f)
                close()
                moveToRelative(0f, -2.85f)
                lineTo(16.55f, 14.6f)
                quadToRelative(0.23f, -0.22f, 0.34f, -0.51f)
                reflectiveQuadTo(17f, 13.5f)
                quadToRelative(0f, -0.6f, -0.43f, -1.05f)
                reflectiveQuadTo(15.5f, 12f)
                quadToRelative(-0.3f, 0f, -0.54f, 0.09f)
                quadTo(14.73f, 12.18f, 14.5f, 12.4f)
                lineTo(12f, 14.9f)
                lineTo(9.5f, 12.4f)
                quadTo(9.35f, 12.25f, 9.11f, 12.13f)
                reflectiveQuadTo(8.5f, 12f)
                quadTo(7.85f, 12f, 7.43f, 12.45f)
                reflectiveQuadTo(7f, 13.5f)
                quadToRelative(0f, 0.3f, 0.13f, 0.56f)
                reflectiveQuadToRelative(0.33f, 0.49f)
                lineTo(12f, 19.15f)
                close()
                moveTo(12f, 5.5f)
                close()
                moveToRelative(0f, 10.07f)
                close()
            }
        }.build()
        return _digitalWellbeing!!
    }

private var _digitalWellbeing: ImageVector? = null

public val Policy: ImageVector
    get() {
        if (_policy != null) {
            return _policy!!
        }
        _policy = ImageVector.Builder(
            name = "policy",
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
                quadTo(8.53f, 21.13f, 6.26f, 18.01f)
                reflectiveQuadTo(4f, 11.1f)
                verticalLineTo(5f)
                lineTo(12f, 2f)
                lineToRelative(8f, 3f)
                verticalLineToRelative(6.1f)
                quadToRelative(0f, 2.13f, -0.72f, 4.09f)
                reflectiveQuadTo(17.2f, 18.65f)
                lineTo(14f, 15.45f)
                quadToRelative(-0.45f, 0.28f, -0.96f, 0.41f)
                reflectiveQuadTo(12f, 16f)
                quadTo(10.35f, 16f, 9.18f, 14.83f)
                reflectiveQuadTo(8f, 12f)
                reflectiveQuadTo(9.18f, 9.17f)
                reflectiveQuadTo(12f, 8f)
                reflectiveQuadToRelative(2.83f, 1.17f)
                reflectiveQuadTo(16f, 12f)
                quadToRelative(0f, 0.55f, -0.14f, 1.06f)
                reflectiveQuadToRelative(-0.41f, 0.99f)
                lineToRelative(1.5f, 1.5f)
                quadToRelative(0.5f, -1.02f, 0.78f, -2.15f)
                reflectiveQuadTo(18f, 11.1f)
                verticalLineTo(6.38f)
                lineTo(12f, 4.13f)
                lineTo(6f, 6.38f)
                verticalLineTo(11.1f)
                quadToRelative(0f, 3.03f, 1.7f, 5.5f)
                reflectiveQuadTo(12f, 19.9f)
                quadToRelative(0.65f, -0.2f, 1.24f, -0.51f)
                reflectiveQuadTo(14.4f, 18.65f)
                lineToRelative(1.4f, 1.4f)
                quadToRelative(-0.82f, 0.68f, -1.79f, 1.18f)
                reflectiveQuadTo(12f, 22f)
                close()
                moveToRelative(1.41f, -8.59f)
                quadTo(14f, 12.83f, 14f, 12f)
                reflectiveQuadTo(13.41f, 10.59f)
                reflectiveQuadTo(12f, 10f)
                reflectiveQuadToRelative(-1.41f, 0.59f)
                quadTo(10f, 11.18f, 10f, 12f)
                reflectiveQuadToRelative(0.59f, 1.41f)
                reflectiveQuadTo(12f, 14f)
                reflectiveQuadToRelative(1.41f, -0.59f)
                close()
                moveTo(12.2f, 12.08f)
                close()
            }
        }.build()
        return _policy!!
    }

private var _policy: ImageVector? = null

public val LocalFireDepartment: ImageVector
    get() {
        if (_localFireDepartment != null) {
            return _localFireDepartment!!
        }
        _localFireDepartment = ImageVector.Builder(
            name = "local_fire_department",
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
                moveTo(6f, 14f)
                quadToRelative(0f, 1.3f, 0.53f, 2.46f)
                reflectiveQuadToRelative(1.5f, 2.04f)
                quadTo(8f, 18.38f, 8f, 18.27f)
                quadToRelative(0f, -0.1f, 0f, -0.22f)
                quadToRelative(0f, -0.8f, 0.3f, -1.5f)
                reflectiveQuadTo(9.18f, 15.28f)
                lineTo(12f, 12.5f)
                lineToRelative(2.83f, 2.78f)
                quadToRelative(0.57f, 0.57f, 0.88f, 1.28f)
                reflectiveQuadToRelative(0.3f, 1.5f)
                quadToRelative(0f, 0.13f, 0f, 0.22f)
                quadToRelative(0f, 0.1f, -0.02f, 0.23f)
                quadToRelative(0.97f, -0.88f, 1.5f, -2.04f)
                reflectiveQuadTo(18f, 14f)
                quadToRelative(0f, -1.25f, -0.46f, -2.36f)
                reflectiveQuadTo(16.2f, 9.65f)
                quadToRelative(-0.5f, 0.33f, -1.05f, 0.49f)
                reflectiveQuadTo(14.03f, 10.3f)
                quadToRelative(-1.55f, 0f, -2.69f, -1.03f)
                reflectiveQuadTo(10.03f, 6.75f)
                quadTo(9.05f, 7.57f, 8.3f, 8.46f)
                reflectiveQuadToRelative(-1.26f, 1.8f)
                reflectiveQuadTo(6.26f, 12.13f)
                reflectiveQuadTo(6f, 14f)
                close()
                moveToRelative(6f, 1.3f)
                lineToRelative(-1.42f, 1.4f)
                quadToRelative(-0.28f, 0.28f, -0.43f, 0.63f)
                quadTo(10f, 17.68f, 10f, 18.05f)
                quadToRelative(0f, 0.8f, 0.59f, 1.38f)
                reflectiveQuadTo(12f, 20f)
                reflectiveQuadToRelative(1.41f, -0.57f)
                reflectiveQuadTo(14f, 18.05f)
                quadToRelative(0f, -0.4f, -0.15f, -0.74f)
                reflectiveQuadTo(13.43f, 16.7f)
                lineTo(12f, 15.3f)
                close()
                moveTo(12f, 3f)
                verticalLineTo(6.3f)
                quadToRelative(0f, 0.85f, 0.59f, 1.42f)
                reflectiveQuadTo(14.03f, 8.3f)
                quadToRelative(0.45f, 0f, 0.84f, -0.19f)
                quadTo(15.25f, 7.93f, 15.55f, 7.55f)
                lineTo(16f, 7f)
                quadToRelative(1.85f, 1.05f, 2.93f, 2.92f)
                reflectiveQuadTo(20f, 14f)
                quadToRelative(0f, 3.35f, -2.32f, 5.68f)
                reflectiveQuadTo(12f, 22f)
                reflectiveQuadTo(6.33f, 19.68f)
                reflectiveQuadTo(4f, 14f)
                quadTo(4f, 10.77f, 6.16f, 7.88f)
                quadTo(8.33f, 4.97f, 12f, 3f)
                close()
            }
        }.build()
        return _localFireDepartment!!
    }

private var _localFireDepartment: ImageVector? = null

public val Translate: ImageVector
    get() {
        if (_translate != null) {
            return _translate!!
        }
        _translate = ImageVector.Builder(
            name = "translate",
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
                moveTo(11.9f, 22f)
                lineTo(16.45f, 10f)
                horizontalLineToRelative(2.1f)
                lineTo(23.1f, 22f)
                horizontalLineTo(21f)
                lineTo(19.93f, 18.95f)
                horizontalLineTo(15.08f)
                lineTo(14f, 22f)
                horizontalLineTo(11.9f)
                close()
                moveTo(4f, 19f)
                lineTo(2.6f, 17.6f)
                lineTo(7.65f, 12.55f)
                quadToRelative(-0.88f, -0.88f, -1.59f, -2f)
                reflectiveQuadTo(4.75f, 8f)
                horizontalLineToRelative(2.1f)
                quadToRelative(0.5f, 0.97f, 1f, 1.7f)
                reflectiveQuadToRelative(1.2f, 1.45f)
                quadTo(9.88f, 10.33f, 10.76f, 8.84f)
                reflectiveQuadTo(12.1f, 6f)
                horizontalLineTo(1f)
                verticalLineTo(4f)
                horizontalLineTo(8f)
                verticalLineTo(2f)
                horizontalLineToRelative(2f)
                verticalLineTo(4f)
                horizontalLineToRelative(7f)
                verticalLineTo(6f)
                horizontalLineTo(14.1f)
                quadTo(13.58f, 7.8f, 12.53f, 9.7f)
                reflectiveQuadToRelative(-2.07f, 2.9f)
                lineToRelative(2.4f, 2.45f)
                lineTo(12.1f, 17.1f)
                lineTo(9.05f, 13.98f)
                lineTo(4f, 19f)
                close()
                moveTo(15.7f, 17.2f)
                horizontalLineToRelative(3.6f)
                lineTo(17.5f, 12.1f)
                lineToRelative(-1.8f, 5.1f)
                close()
            }
        }.build()
        return _translate!!
    }

private var _translate: ImageVector? = null
