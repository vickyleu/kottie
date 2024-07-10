package org.airbnb.fork.kottieAnimationState

data class KottieAnimationState(
    val composition: Any? = null,
    val isPlaying: Boolean = false,
    val isCompleted: Boolean = false,
    val progress: Float = 0f,
    val duration: Float = 0f,
    val iterations: Int = 0,
    val speed: Float = 0f
)