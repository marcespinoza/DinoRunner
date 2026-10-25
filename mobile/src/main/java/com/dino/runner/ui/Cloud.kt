package com.dino.runner.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.random.Random

private val CLOUD = listOf(
    "......####......",
    "....########....",
    "..############..",
    "################",
    "################",
    ".##############."
)

fun DrawScope.drawCloud(topLeft: Offset, width: Float, color: Color = Color(0xFFDCDCDC)) {
    val cell = width / CLOUD[0].length
    CLOUD.forEachIndexed { y, row ->
        var x = 0
        while (x < row.length) {
            if (row[x] == '#') {
                val start = x
                while (x < row.length && row[x] == '#') x++
                drawRect(
                    color,
                    Offset(topLeft.x + start * cell, topLeft.y + y * cell),
                    Size((x - start) * cell, cell)
                )
            } else x++
        }
    }
}

// --- Suelo: piedritas y lomitas repetidas cada GROUND_PERIOD unidades ---
private const val GROUND_PERIOD = 4f
private const val CELL = 0.006f // tamaño de un "pixel" del suelo

private class Pebble(val x: Float, val dy: Float, val w: Float)

private val PEBBLES = Random(7).let { r ->
    List(40) {
        Pebble(
            x = r.nextFloat() * GROUND_PERIOD,
            dy = 0.012f + r.nextFloat() * 0.04f,
            w = 0.01f + r.nextFloat() * 0.02f
        )
    }
}
private val BUMPS = listOf(0.9f, 2.6f)

fun DrawScope.drawGround(groundY: Float, u: Float, distance: Float, widthHu: Float) {
    fun shifted(x: Float): Float {
        val m = (x - distance) % GROUND_PERIOD
        return if (m < 0f) m + GROUND_PERIOD else m
    }

    PEBBLES.forEach { p ->
        var x = shifted(p.x)
        while (x < widthHu) {
            drawRect(
                Color(0xFF9E9E9E),
                Offset(x * u, groundY + p.dy * u),
                Size(p.w * u, CELL * u)
            )
            x += GROUND_PERIOD
        }
    }

    BUMPS.forEach { bx ->
        var x = shifted(bx)
        while (x < widthHu) {
            val dark = Color(0xFF535353)
            drawRect(dark, Offset(x * u, groundY - 2 * CELL * u), Size(0.06f * u, 2 * CELL * u))
            drawRect(dark, Offset((x + 0.01f) * u, groundY - 3 * CELL * u), Size(0.04f * u, CELL * u))
            x += GROUND_PERIOD
        }
    }
}