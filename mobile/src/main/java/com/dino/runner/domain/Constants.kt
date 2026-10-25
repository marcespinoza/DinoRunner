package com.dino.runner.domain

object Constants {
    const val DINO_X = 0.15f
    const val DINO_SIZE = 0.12f
    const val GRAVITY = 4.5f
    const val JUMP_VELOCITY = 1.4f
    const val START_SPEED = 0.8f
    const val MAX_SPEED = 1.8f
    const val S = DINO_SIZE / 32f
    const val HIT_LEFT = DINO_X + 8 * S
    const val HIT_RIGHT = DINO_X + 25 * S
    const val HIT_HEIGHT = 27 * S
    const val FORGIVENESS = 0.01f

    const val CLOUD_W = 0.2f
}