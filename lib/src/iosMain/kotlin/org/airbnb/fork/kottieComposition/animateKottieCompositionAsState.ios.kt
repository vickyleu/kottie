package org.airbnb.fork.kottieComposition

import Lottie.CompatibleAnimationView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import org.airbnb.fork.kottieAnimationState.KottieAnimationState
import org.airbnb.fork.lottie.animateLottieCompositionAsState.animateLottieCompositionAsState

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun animateKottieCompositionAsState(
    composition: Any?,
    speed: Float,
    iterations: Int,
    isPlaying: Boolean,
    restartOnPlay: Boolean
): State<KottieAnimationState> {
    val kottieAnimationState = remember { mutableStateOf(KottieAnimationState()) }
    val animationState = animateLottieCompositionAsState(
        composition = composition as? CompatibleAnimationView,
        speed = speed,
        iterations = iterations,
        isPlaying = isPlaying,
        restartOnPlay = restartOnPlay
    )
    LaunchedEffect(
        animationState.progress
    ) {
        kottieAnimationState.value = kottieAnimationState.value.copy(
            composition = animationState.composition,
            isPlaying = animationState.isPlaying,
            isCompleted = animationState.isCompleted,
            progress = animationState.progress,
            duration = animationState.duration,
            iterations = animationState.iterations,
            speed = animationState.speed
        )
    }
    return kottieAnimationState
}