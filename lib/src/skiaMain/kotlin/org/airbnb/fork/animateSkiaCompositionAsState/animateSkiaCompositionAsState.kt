package org.airbnb.fork.animateSkiaCompositionAsState

import org.airbnb.fork.SkiaAnimationState


import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.repeatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.jetbrains.skia.skottie.Animation
import kotlin.math.roundToInt


@Composable
fun animateSkiaCompositionAsState(
    composition: Animation?,
    speed: Float = 1f,
    iterations: Int = 1,
    isPlaying: Boolean = true,
    restartOnPlay: Boolean = true
): State<SkiaAnimationState>{

    require(iterations > 0) { "Iterations must be a positive number ($iterations)." }
    require(speed.isFinite()) { "Speed must be a finite number. It is $speed." }

    val animatable = remember { Animatable(initialValue = 0f) }
    var wasPlaying by remember { mutableStateOf(isPlaying) }

    val skiaAnimationState = remember { mutableStateOf(SkiaAnimationState()) }

    LaunchedEffect(
        composition,
        isPlaying,
        speed,
        iterations
    ) {
        when (val animation = composition) {
            null -> {}
            else -> {
                if (isPlaying && !wasPlaying && restartOnPlay){
                    animatable.snapTo(0f)
                }
                wasPlaying = isPlaying

                if (!isPlaying) return@LaunchedEffect

                animatable.animateTo(
                    targetValue = animation.duration,
                    animationSpec = if (iterations == Int.MAX_VALUE) {
                        infiniteRepeatable(
                            animation = tween(
                                durationMillis = (animation.duration * 1000 / speed).roundToInt(),
                                easing = LinearEasing
                            ),
                            repeatMode = RepeatMode.Restart
                        )
                    } else {
                        repeatable(
                            iterations = iterations,
                            animation = tween(
                                durationMillis = (animation.duration * 1000 / speed).roundToInt(),
                                easing = LinearEasing
                            ),
                            repeatMode = RepeatMode.Restart
                        )
                    }
                )
            }
        }
    }
    LaunchedEffect(
        key1 = animatable.value
    ) {
        if (composition == null) {
            return@LaunchedEffect
        }
        skiaAnimationState.value = skiaAnimationState.value.copy(
            composition = composition,
            isPlaying = animatable.isRunning,
            isCompleted = animatable.value > 0.0 && animatable.value == composition.duration,
            progress = animatable.value,
            duration = composition.duration,
            iterations = iterations,
            speed = speed
        )
    }
    return skiaAnimationState
}


