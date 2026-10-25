package com.dino.runner.ui

import android.content.Context
import android.media.MediaPlayer
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dino.runner.GameState
import com.dino.runner.R
import com.dino.runner.domain.Constants.CLOUD_W
import com.dino.runner.domain.Constants.DINO_SIZE
import com.dino.runner.domain.Constants.DINO_X
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val BIRD_WINGS_UP = listOf(
    ".........#.",
    ".........##",
    ".........###",
    "....##...####",
    "...####..#####",
    "..######.######",
    ".#######.#######",
    "####################",
    "......###########",
    "........###########",
    "..........######"
)
private val BIRD_WINGS_DOWN = listOf(
    "...........",
    "...........",
    "............",
    "....##......",
    "...####.....",
    "..######.#ooooo",
    ".#######.#oooo##",
    "##########ooo#######",
    "......####oo#####",
    "........##o########",
    "..........######"
)
private val BIRD_COLORS = mapOf(
    '#' to Color(0xFF535353),
    'o' to Color(0xFF8A8A8A),
    'w' to Color.White
)
const val BIRD_ASPECT = 20f / 11f // width / height

fun DrawScope.drawBird(topLeft: Offset, height: Float, wingsUp: Boolean) {
    val rows = if (wingsUp) BIRD_WINGS_UP else BIRD_WINGS_DOWN
    val cell = height / rows.size
    rows.forEachIndexed { y, row ->
        var x = 0
        while (x < row.length) {
            val ch = row[x]
            val color = BIRD_COLORS[ch]
            if (color == null) { x++; continue }
            val start = x
            while (x < row.length && row[x] == ch) x++ // merge same-color run
            drawRect(
                color = color,
                topLeft = Offset(topLeft.x + start * cell, topLeft.y + y * cell),
                size = Size((x - start) * cell, cell)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BirdPreview() {
    Row(
        modifier = Modifier
            .background(Color(0xFFF7F7F7))
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Canvas(
            modifier = Modifier
                .height(100.dp)
                .aspectRatio(BIRD_ASPECT)
        ) {
            drawBird(
                topLeft = Offset.Zero,
                height = size.height,
                wingsUp = true
            )
        }
        Canvas(
            modifier = Modifier
                .height(100.dp)
                .aspectRatio(BIRD_ASPECT)
        ) {
            drawBird(
                topLeft = Offset.Zero,
                height = size.height,
                wingsUp = false
            )
        }
    }
}
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "dino_prefs",
    produceMigrations = { context ->
        listOf(SharedPreferencesMigration(context, "dino_prefs"))
    }
)
private val HIGH_SCORE_KEY = intPreferencesKey("high_score")

@Composable
fun MainScreen(paused: Boolean) {
val state = remember { GameState() }
val context = LocalContext.current
val coroutineScope = rememberCoroutineScope()
val highScoreFlow = remember(context) {
    context.dataStore.data.map { preferences ->
        preferences[HIGH_SCORE_KEY] ?: 0
    }
}
val highScoreState = highScoreFlow.collectAsState(initial = 0)
val highScore = highScoreState.value
var tick by remember { mutableLongStateOf(0L) }
var boxSize by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }
var aspect by remember { mutableFloatStateOf(16f / 9f) }
var score by remember { mutableIntStateOf(0) }
var gameOver by remember { mutableStateOf(false) }


LaunchedEffect(paused) {
    if (paused) return@LaunchedEffect
    var last = withFrameNanos { it }
    var accumulator = 0f
    val fixedDt = 1f / 60f
    while (true) {
        withFrameNanos { now ->
            val frameTime = ((now - last) / 1_000_000_000f).coerceAtMost(0.1f)
            last = now
            val wasOver = state.over
            if (!paused) {
                accumulator += frameTime
                while (accumulator >= fixedDt) {
                    state.update(fixedDt, aspect)
                    accumulator -= fixedDt
                }
                score = state.score.toInt()
                if (!wasOver && state.over) {
                    val finalScore = state.score.toInt()
                    coroutineScope.launch {
                        context.dataStore.edit { preferences ->
                            val currentHigh = preferences[HIGH_SCORE_KEY] ?: 0
                            if (finalScore > currentHigh) {
                                preferences[HIGH_SCORE_KEY] = finalScore
                            }
                        }
                    }
                    try {
                        val mediaPlayer = MediaPlayer.create(context, R.raw.dinocrash)
                        mediaPlayer?.setOnCompletionListener { it.release() }
                        mediaPlayer?.start()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                if (state.playCoin) {
                    try {
                        val mediaPlayer = MediaPlayer.create(context, R.raw.mario_coin_sound)
                        mediaPlayer?.setOnCompletionListener { it.release() }
                        mediaPlayer?.start()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                if (state.playJump) {
                    state.playJump = false
                    try {
                        val mediaPlayer = MediaPlayer.create(context, R.raw.jump_sound)
                        mediaPlayer?.setOnCompletionListener { it.release() }
                        mediaPlayer?.start()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                gameOver = state.over
                tick = now
            }
        }
    }
}

    Box(
        modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFFF7F7F7))
        .safeDrawingPadding()
        .pointerInput(Unit) { detectTapGestures { state.jump() } }
        .onSizeChanged {
            boxSize = it
            if (it.height > 0) aspect = it.width.toFloat() / it.height
        }
    ) {
        Canvas(
            Modifier.fillMaxSize()
        ) {
            tick
            val u = size.height
            val groundY = u * 0.8f

            drawLine(
                color = Color.Gray,
                start = Offset(0f, groundY),
                end = Offset(size.width, groundY),
                strokeWidth = u * 0.005f
            )
            state.clouds.forEach {
                drawCloud(Offset(it.x * u, groundY - it.y * u), CLOUD_W * u)
            }

            drawGround(groundY, u, state.distance, size.width / u)
            state.obstacles.forEach {
                val topLeft = Offset(it.x * u, groundY - (it.bottom + it.h) * u)
                if (it.isBird) {
                    // `tick` is in nanoseconds: flap about every 150 ms
                    drawBird(topLeft, it.h * u, wingsUp = (tick / 250_000_000L) % 2L == 0L)
                } else {
                    drawPixelCactus(
                        topLeft = Offset(it.x * u, groundY - it.h * u),
                        height = it.h * u
                    )
                }
            }
        }

        val u = boxSize.height.toFloat()
        if (u > 0f) {
            val groundY = u * 0.8f
            val dinoSizePx = DINO_SIZE * u
            val feetGap = 3f / 32f * dinoSizePx
            Dino(
                modifier = Modifier
                    .size(with(LocalDensity.current) { dinoSizePx.toDp() })
                    .graphicsLayer {
                        tick // leer el estado acá para que se actualice en cada frame
                        translationX = DINO_X * u
                        translationY = groundY - (state.dinoY + DINO_SIZE) * u +feetGap
                    }
            )
        }
        Row(
            modifier = Modifier
                .wrapContentHeight()
                .align(Alignment.TopEnd),
            horizontalArrangement = Arrangement.End,
        ){
            Text(
                text = "High Score: $highScore",
                style = TextStyle(fontSize = 20.sp, color = Color.DarkGray),
                modifier = Modifier
                    .padding(16.dp)
            )
            Text(
                text =  score.toString(),
                style = TextStyle(fontSize = 20.sp, color = Color.DarkGray),
                modifier = Modifier
                    .padding(16.dp)
                    .width(50.dp)
            )
        }

        if (gameOver) {
            BasicText(
                text = "Fin del juego. Toca para reintentar",
                style = TextStyle(fontSize = 22.sp, color = Color.DarkGray),
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }

}