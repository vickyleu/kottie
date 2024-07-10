package org.airbnb.fork.kottieComposition

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import org.airbnb.fork.kottieAnimationState.KottieAnimationState


@Composable
expect fun animateKottieCompositionAsState(
    composition: Any?,
    speed: Float = 1f,
    iterations: Int = 1,
    isPlaying: Boolean = true,
    restartOnPlay: Boolean = true
): State<KottieAnimationState>