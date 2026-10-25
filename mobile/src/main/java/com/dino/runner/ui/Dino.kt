package com.dino.runner.ui


import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun Dino(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        val unit = size.width / 30
        val dino = Color(0xFF535353)

        // Each row contains horizontal ranges: startX until endX.
        val rows = listOf(
            2 to listOf(18 to 26),
            3 to listOf(16 to 28),
            4 to listOf(16 to 19, 20 to 28),
            5 to listOf(16 to 28),
            6 to listOf(16 to 28),
            7 to listOf(16 to 28),
            8 to listOf(16 to 28),
            9 to listOf(16 to 28),
            10 to listOf(16 to 22),
            11 to listOf(15 to 26),
            12 to listOf(14 to 21),
            13 to listOf(13 to 21),
            14 to listOf(12 to 21),
            15 to listOf(11 to 23),
            16 to listOf(10 to 21, 22 to 23),
            17 to listOf(7 to 21, 22 to 23),
            18 to listOf(5 to 20),
            19 to listOf(5 to 19),
            20 to listOf(5 to 18),
            21 to listOf(5 to 17),
            22 to listOf(7 to 17),
            23 to listOf(7 to 12, 14 to 15),
            24 to listOf(7 to 11),
            25 to listOf(7 to 10),
        )

        rows.forEach { (y, ranges) ->
            ranges.forEach { (start, end) ->
                drawRect(
                    color = dino,
                    topLeft = Offset(start * unit, y * unit),
                    size = Size((end - start) * unit, unit)
                )
            }
        }

        // Tail
        drawRect(dino, Offset(4 * unit, 13 * unit),
            Size(unit, 7 * unit))
        drawRect(dino, Offset(5 * unit, 15 * unit),
            Size(unit, 8 * unit))
        drawRect(dino, Offset(6 * unit, 16 * unit),
            Size(unit, 9 * unit))

        // Legs
        drawRect(dino, Offset(7 * unit, 25 * unit),
            Size(2 * unit, 4 * unit))
        drawRect(dino, Offset(7 * unit, 28 * unit),
            Size(4 * unit, unit))

        drawRect(dino, Offset(15 * unit, 23 * unit),
            Size(2 * unit, 6 * unit))
        drawRect(dino, Offset(15 * unit, 28 * unit),
            Size(4 * unit, unit))

        /*// Eye
        drawRect(
            Color.Yellow,
            Offset(14 * unit, 4 * unit),
            Size(unit, unit)
        )*/
    }
}

@Preview(showBackground = true)
@Composable
private fun DinoPreview() {
    Dino()
}

