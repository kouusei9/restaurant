package com.kouusei.restaurant.presentation.utils

import android.location.Location
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng


fun Location.toLatLng(): LatLng {
    return LatLng(latitude, longitude)
}

fun splitBusinessHours(raw: String): List<Pair<String, String>> {
    val regex =
        Regex("""([月火水木金土日祝前日、～]+): (.*?)(?=([月火水木金土日祝前日、～]+):|${'$'})""")
    val matches = regex.findAll(raw)
    return matches.map { matchResult ->
        val dayPart = matchResult.groupValues[1]
        val timePart = matchResult.groupValues[2]
            .replace("（", "\n").replace("）", "")
        Pair(dayPart, timePart)
    }.toList()
}

@Composable
fun ZigzagDivider(
    modifier: Modifier = Modifier,
    color: Color = Color.Gray,
    shadowColor: Color = Color.Black,
    zigzagHeight: Dp = 16.dp,
    zigzagWidth: Dp = 16.dp,
    shadowBlurRadius: Dp = 15.dp
) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val zigzagH = zigzagHeight.toPx()
        val zigzagW = zigzagWidth.toPx()

        val path = Path().apply {
            moveTo(0f, 0f)

            var currentX = 0f
            var goingDown = true

            while (currentX < width) {
                val nextX = (currentX + zigzagW).coerceAtMost(width)
                val nextY = if (goingDown) zigzagH else 2f

                lineTo(nextX, nextY)
                currentX = nextX
                goingDown = !goingDown
            }

            lineTo(width, 0f)
            lineTo(0f, 0f)
            close()
        }

        // Draw shadow first
        drawIntoCanvas { canvas ->
            val paint = Paint().apply {
                this.color = shadowColor
                this.asFrameworkPaint().apply {
                    isAntiAlias = true
                    setShadowLayer(
                        shadowBlurRadius.toPx(),
                        0f,
                        10f,
                        shadowColor.toArgb()
                    )
                }
            }
            canvas.drawPath(path, paint)
        }

        // Draw solid zigzag
        drawPath(
            path = path,
            color = color
        )
    }
}