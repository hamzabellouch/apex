package com.tkno.apex.ui.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("CheckReturnValue")
public val DeleteForever: ImageVector
    get() {
        if (_deleteForever != null) {
            return _deleteForever!!
        }
        _deleteForever = ImageVector.Builder(
            name = "delete_forever",
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
                moveTo(9.4f, 16.5f)
                lineTo(12f, 13.9f)
                lineToRelative(2.6f, 2.6f)
                lineTo(16f, 15.1f)
                lineTo(13.4f, 12.5f)
                lineTo(16f, 9.9f)
                lineTo(14.6f, 8.5f)
                lineTo(12f, 11.1f)
                lineTo(9.4f, 8.5f)
                lineTo(8f, 9.9f)
                lineToRelative(2.6f, 2.6f)
                lineTo(8f, 15.1f)
                lineToRelative(1.4f, 1.4f)
                close()
                moveTo(7f, 21f)
                quadTo(6.18f, 21f, 5.59f, 20.41f)
                reflectiveQuadTo(5f, 19f)
                verticalLineTo(6f)
                horizontalLineTo(4f)
                verticalLineTo(4f)
                horizontalLineTo(9f)
                verticalLineTo(3f)
                horizontalLineToRelative(6f)
                verticalLineTo(4f)
                horizontalLineToRelative(5f)
                verticalLineTo(6f)
                horizontalLineTo(19f)
                verticalLineTo(19f)
                quadToRelative(0f, 0.82f, -0.59f, 1.41f)
                reflectiveQuadTo(17f, 21f)
                horizontalLineTo(7f)
                close()
                moveTo(17f, 6f)
                horizontalLineTo(7f)
                verticalLineTo(19f)
                horizontalLineTo(17f)
                verticalLineTo(6f)
                close()
                moveTo(7f, 6f)
                verticalLineTo(19f)
                verticalLineTo(6f)
                close()
            }
        }.build()
        return _deleteForever!!
    }

private var _deleteForever: ImageVector? = null

@Suppress("CheckReturnValue")
public val ArrowOutward: ImageVector
    get() {
        if (_arrowOutward != null) {
            return _arrowOutward!!
        }
        _arrowOutward = ImageVector.Builder(
            name = "arrow_outward",
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
                moveTo(6.4f, 18f)
                lineTo(5f, 16.6f)
                lineTo(14.6f, 7f)
                horizontalLineTo(6f)
                verticalLineTo(5f)
                horizontalLineTo(18f)
                verticalLineTo(17f)
                horizontalLineTo(16f)
                verticalLineTo(8.4f)
                lineTo(6.4f, 18f)
                close()
            }
        }.build()
        return _arrowOutward!!
    }

private var _arrowOutward: ImageVector? = null

@Suppress("CheckReturnValue")
public val DeployedCodeUpdate: ImageVector
    get() {
        if (_deployedCodeUpdate != null) {
            return _deployedCodeUpdate!!
        }
        _deployedCodeUpdate = ImageVector.Builder(
            name = "deployed_code_update",
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
                moveTo(18f, 22f)
                lineToRelative(3f, -3f)
                lineTo(20.3f, 18.3f)
                lineToRelative(-1.8f, 1.8f)
                verticalLineTo(16f)
                horizontalLineToRelative(-1f)
                verticalLineToRelative(4.1f)
                lineTo(15.7f, 18.3f)
                lineTo(15f, 19f)
                lineToRelative(3f, 3f)
                close()
                moveTo(12f, 4f)
                lineTo(6.08f, 7.43f)
                lineTo(12f, 10.85f)
                lineTo(17.93f, 7.43f)
                lineTo(12f, 4f)
                close()
                moveTo(3f, 15.98f)
                verticalLineTo(8.02f)
                quadToRelative(0f, -0.55f, 0.26f, -1f)
                reflectiveQuadTo(4f, 6.3f)
                lineTo(11f, 2.27f)
                quadToRelative(0.25f, -0.13f, 0.49f, -0.2f)
                reflectiveQuadTo(12f, 2f)
                quadToRelative(0.28f, 0f, 0.53f, 0.07f)
                reflectiveQuadTo(13f, 2.27f)
                lineTo(20f, 6.3f)
                quadToRelative(0.48f, 0.27f, 0.74f, 0.72f)
                reflectiveQuadToRelative(0.26f, 1f)
                verticalLineTo(12f)
                quadToRelative(-0.45f, 0f, -1f, 0f)
                reflectiveQuadToRelative(-1f, 0f)
                verticalLineTo(9.1f)
                lineToRelative(-7.02f, 4.05f)
                lineTo(5f, 9.1f)
                verticalLineToRelative(6.85f)
                lineToRelative(6f, 3.48f)
                quadToRelative(0f, 0.52f, 0f, 1.15f)
                reflectiveQuadToRelative(0f, 1.15f)
                lineTo(4f, 17.7f)
                quadTo(3.53f, 17.43f, 3.26f, 16.98f)
                quadTo(3f, 16.52f, 3f, 15.98f)
                close()
                moveToRelative(11.46f, 6.56f)
                quadTo(13f, 21.08f, 13f, 19f)
                reflectiveQuadToRelative(1.46f, -3.54f)
                reflectiveQuadTo(18f, 14f)
                reflectiveQuadToRelative(3.54f, 1.46f)
                reflectiveQuadTo(23f, 19f)
                reflectiveQuadToRelative(-1.46f, 3.54f)
                reflectiveQuadTo(18f, 24f)
                quadToRelative(-2.07f, 0f, -3.54f, -1.46f)
                close()
                moveTo(12f, 11.73f)
                close()
            }
        }.build()
        return _deployedCodeUpdate!!
    }

private var _deployedCodeUpdate: ImageVector? = null

@Suppress("CheckReturnValue")
public val InfoMaterial: ImageVector
    get() {
        if (_infoMaterial != null) {
            return _infoMaterial!!
        }
        _infoMaterial = ImageVector.Builder(
            name = "info",
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
                moveTo(11f, 17f)
                horizontalLineToRelative(2f)
                verticalLineTo(11f)
                horizontalLineTo(11f)
                verticalLineToRelative(6f)
                close()
                moveTo(12.71f, 8.71f)
                quadTo(13f, 8.42f, 13f, 8f)
                quadTo(13f, 7.57f, 12.71f, 7.29f)
                reflectiveQuadTo(12f, 7f)
                reflectiveQuadTo(11.29f, 7.29f)
                reflectiveQuadTo(11f, 8f)
                quadToRelative(0f, 0.42f, 0.29f, 0.71f)
                reflectiveQuadTo(12f, 9f)
                reflectiveQuadTo(12.71f, 8.71f)
                close()
                moveTo(12f, 22f)
                quadTo(9.93f, 22f, 8.1f, 21.21f)
                quadTo(6.28f, 20.43f, 4.93f, 19.08f)
                quadTo(3.58f, 17.73f, 2.79f, 15.9f)
                reflectiveQuadTo(2f, 12f)
                quadTo(2f, 9.92f, 2.79f, 8.1f)
                quadTo(3.58f, 6.27f, 4.93f, 4.93f)
                quadTo(6.28f, 3.57f, 8.1f, 2.79f)
                quadTo(9.93f, 2f, 12f, 2f)
                reflectiveQuadToRelative(3.9f, 0.79f)
                reflectiveQuadToRelative(3.17f, 2.14f)
                quadToRelative(1.35f, 1.35f, 2.14f, 3.17f)
                quadTo(22f, 9.92f, 22f, 12f)
                reflectiveQuadToRelative(-0.79f, 3.9f)
                reflectiveQuadToRelative(-2.14f, 3.17f)
                quadToRelative(-1.35f, 1.35f, -3.17f, 2.14f)
                reflectiveQuadTo(12f, 22f)
                close()
                moveToRelative(0f, -2f)
                quadToRelative(3.35f, 0f, 5.68f, -2.32f)
                reflectiveQuadTo(20f, 12f)
                reflectiveQuadTo(17.68f, 6.32f)
                reflectiveQuadTo(12f, 4f)
                reflectiveQuadTo(6.33f, 6.32f)
                reflectiveQuadTo(4f, 12f)
                reflectiveQuadToRelative(2.33f, 5.68f)
                reflectiveQuadTo(12f, 20f)
                close()
                moveToRelative(0f, -8f)
                close()
            }
        }.build()
        return _infoMaterial!!
    }

private var _infoMaterial: ImageVector? = null
