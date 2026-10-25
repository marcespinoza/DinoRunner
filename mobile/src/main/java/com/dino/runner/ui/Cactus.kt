package com.dino.runner.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

private val CACTUS = listOf(
    ".....#.....",
    "....###....",
    "....###....",
    "....###.##.",
    "....###.##.",
    ".##.###.##.",
    ".##.###.##.",
    ".##.###.##.",
    ".##.######.",
    ".##.######.",
    ".##.###....",
    ".######....",
    "..#####....",
    "....###....",
    "....###....",
    "....###...."
)

private const val CACTUS_ASPECT = 11f / 16f // ancho / alto
class CactusRect(val x0: Float, val x1: Float, val y0: Float, val y1: Float)

private val CACTUS_COLS = CACTUS.maxOf { it.length }

// Un rectángulo por tramo horizontal, normalizado (x de 0 a 1 del ancho, y de 0 a 1 desde arriba)
val CACTUS_RECTS: List<CactusRect> = buildList {
    CACTUS.forEachIndexed { y, row ->
        var x = 0
        while (x < row.length) {
            if (row[x] == '#') {
                val start = x
                while (x < row.length && row[x] == '#') x++
                add(
                    CactusRect(
                        start / CACTUS_COLS.toFloat(), x / CACTUS_COLS.toFloat(),
                        y / CACTUS.size.toFloat(), (y + 1) / CACTUS.size.toFloat()
                    )
                )
            } else x++
        }
    }
}

fun DrawScope.drawPixelCactus(
    topLeft: Offset,
    height: Float,
    color: Color = Color(0xFF535353) // mismo gris que tu dino
) {
    val cell = height / CACTUS.size
    CACTUS.forEachIndexed { y, row ->
        var x = 0
        while (x < row.length) {
            if (row[x] == '#') {
                val start = x
                while (x < row.length && row[x] == '#') x++ // agrupa el tramo continuo
                drawRect(
                    color = color,
                    topLeft = Offset(topLeft.x + start * cell, topLeft.y + y * cell),
                    size = Size((x - start) * cell, cell)
                )
            } else {
                x++
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CactusPreview() {
    Canvas(
        modifier = Modifier.size(100.dp)
    ) {
        drawPixelCactus(
            topLeft = Offset.Zero,
            height = 100f
        )
    }
}
