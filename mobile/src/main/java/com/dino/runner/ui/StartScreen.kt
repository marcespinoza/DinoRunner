package com.dino.runner.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dino.runner.R

val PixelifyFont = FontFamily(
    Font(R.font.pixelifysans_medium, FontWeight.Medium)
)

@Composable
fun StartScreen(onStartGame: (isTwoPlayers: Boolean) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1428)),
        contentAlignment = Alignment.Center
    ) {
        // Grid background lines
        Canvas(modifier = Modifier.fillMaxSize()) {
            val gridSize = 60f
            var x = 0f
            while (x < size.width) {
                drawLine(
                    color = Color(0xFF2C223D),
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1f
                )
                x += gridSize
            }
            var y = 0f
            while (y < size.height) {
                drawLine(
                    color = Color(0xFF2C223D),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
                y += gridSize
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Title: START GAME
            BasicText(
                text = "START GAME",
                style = TextStyle(
                    fontSize = 42.sp,
                    fontFamily = PixelifyFont,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFFF5722),
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.height(34.dp))

            // 1 PLAYER option
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { onStartGame(false) }
                    .padding(8.dp)
            ) {
                BasicText(
                    text = "1 PLAYER",
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontFamily = PixelifyFont,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFFFCC80)
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2 PLAYER option
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { onStartGame(true) }
                    .padding(8.dp)
            ) {
                BasicText(
                    text = "2 PLAYERS",
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontFamily = PixelifyFont,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFFFC107)
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun StartScreenPreview() {
    StartScreen(onStartGame = {})
}
