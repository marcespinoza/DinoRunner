package com.dino.runner


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.dino.runner.domain.Constants.CLOUD_W
import com.dino.runner.domain.Constants.DINO_SIZE
import com.dino.runner.domain.Constants.DINO_X
import com.dino.runner.domain.Constants.FORGIVENESS
import com.dino.runner.domain.Constants.GRAVITY
import com.dino.runner.domain.Constants.HIT_HEIGHT
import com.dino.runner.domain.Constants.HIT_LEFT
import com.dino.runner.domain.Constants.HIT_RIGHT
import com.dino.runner.domain.Constants.JUMP_VELOCITY
import com.dino.runner.domain.Constants.MAX_SPEED
import com.dino.runner.domain.Constants.START_SPEED
import com.dino.runner.ui.BIRD_ASPECT
import com.dino.runner.ui.CACTUS_RECTS
import com.dino.runner.ui.MainScreen
import com.dino.runner.ui.StartScreen
import kotlin.random.Random

private sealed interface Screen {
    data object Start : Screen
    data object Game : Screen
}

class DinoGameActivity : ComponentActivity() {

    private var paused by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { DinoApp(paused = paused) }
    }

    override fun onPause() {
        paused = true
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        paused = false
    }
}

@Composable
fun DinoApp(paused: Boolean) {
    val backStack = remember { mutableStateListOf<Any>(Screen.Start) }

    NavDisplay(
        backStack = backStack,
        onBack = {
            if (backStack.size > 1) {
                backStack.removeLastOrNull()
            }
        },
        entryProvider = { key ->
            when (key) {
                is Screen.Start -> NavEntry(key) {
                    StartScreen(
                        onStartGame = {
                            backStack.add(Screen.Game)
                        }
                    )
                }
                is Screen.Game -> NavEntry(key) {
                    MainScreen(paused = paused)
                }
                else -> error("Unknown route: $key")
            }
        }
    )
}

// ---------------------------------------------------------------------------------
// Todo el mundo del juego se mide en "unidades de alto" (1.0 = alto de la pantalla),
// asi se adapta a 16:9, 4:3, 9:16, etc. sin valores fijos en pixeles.
// El ancho del mundo es `aspect` (ancho / alto).
// ---------------------------------------------------------------------------------



class Obstacle(
    var x: Float,
    val w: Float,
    val h: Float,
    val bottom: Float = 0f,       // height above the ground (0 = cactus)
    val isBird: Boolean = false
)

class Cloud(var x: Float, var y: Float) // y = altura sobre el suelo

class GameState {
    val obstacles = mutableListOf<Obstacle>()
    var dinoY = 0f
    var vy = 0f
    var speed = START_SPEED
    var score = 0f
    var over = false
    var playCoin = false
    var playJump = false
    private var spawnIn = 1.2f
    private var coinTimer = 0f

    val clouds = mutableListOf<Cloud>()
    var distance = 0f

    private fun randomCloudY() = 0.35f + Random.nextFloat() * 0.3f

    fun jump() {
        when {
            over -> reset()
            dinoY == 0f -> {
                vy = JUMP_VELOCITY
                playJump = true
            }
        }
    }

    fun reset() {
        obstacles.clear()
        dinoY = 0f
        vy = 0f
        speed = START_SPEED
        score = 0f
        spawnIn = 1.2f
        over = false
        coinTimer = 0f
        playCoin = false
        playJump = false
        clouds.clear()
        distance = 0f
    }

    fun update(dt: Float, aspect: Float) {
        if (over) return

        if (clouds.isEmpty()) {
            repeat(3) { i ->
                clouds += Cloud(x = aspect * (i + Random.nextFloat()) / 3f, y = randomCloudY())
            }
        }

        distance += speed * dt
        clouds.forEach {
            it.x -= speed * 0.15f * dt // 15 % de la velocidad: parallax
            if (it.x + CLOUD_W < 0f) {
                it.x = aspect + Random.nextFloat() * 0.5f
                it.y = randomCloudY()
            }
        }

        playCoin = false

        coinTimer += dt
        if (coinTimer >= 10f) {
            coinTimer -= 10f
            playCoin = true
        }

        vy -= GRAVITY * dt
        dinoY += vy * dt
        if (dinoY <= 0f) {
            dinoY = 0f
            vy = 0f
        }

        obstacles.forEach { it.x -= speed * dt }
        obstacles.removeAll { it.x + it.w < 0f }

        spawnIn -= dt
        if (spawnIn <= 0f) {
            if (score > 100f && Random.nextFloat() < 0.3f) {
                val h = 0.07f
                obstacles += Obstacle(
                    x = aspect, w = h * BIRD_ASPECT, h = h,
                    bottom = 0.14f + Random.nextFloat() * 0.06f, isBird = true
                )
            } else {
                val h = 0.08f + Random.nextFloat() * 0.08f
                obstacles += Obstacle(x = aspect, w = h * (11f / 16f), h = h)
            }
            spawnIn = 0.9f + Random.nextFloat() * 0.9f
        }

        speed = (speed + 0.02f * dt).coerceAtMost(MAX_SPEED)
        score += dt * 10f

        if (obstacles.any { it.hits(HIT_LEFT, HIT_RIGHT, dinoY + FORGIVENESS, dinoY + HIT_HEIGHT) }) {
            over = true
        }

    }

    private fun Obstacle.hits(left: Float, right: Float, feet: Float, head: Float): Boolean {
        if (isBird) {
            return x + w * 0.1f < right && x + w * 0.9f > left &&
                    feet < bottom + h && head > bottom
        }
        return CACTUS_RECTS.any { p ->
            val pl = x + p.x0 * w
            val pr = x + p.x1 * w
            val top = bottom + h - p.y0 * h   // altura sobre el suelo
            val bot = bottom + h - p.y1 * h
            pl < right && pr > left && feet < top && head > bot
        }
    }
}



@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun DinoAppPreview() {
    DinoApp(paused = false)
}

@Preview(showBackground = true, widthDp = 640, heightDp = 360)
@Composable
private fun DinoGamePreview() {
    MainScreen(paused = false)
}