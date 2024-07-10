package org.airbnb.fork

import org.jetbrains.skia.skottie.Animation

data class SkiaAnimationState(
    val composition: Animation? = null,
    val isPlaying: Boolean = false,
    val isCompleted: Boolean = false,
    val progress: Float = 0.0f,
    val duration: Float = 0.0f,
    val iterations: Int = 0,
    val speed: Float = 1f
)