package org.airbnb.fork

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.airbnb.fork.lottie.KLottieAnimation


@Composable
actual fun KottieAnimation(
    modifier: Modifier,
    composition: Any?,
    progress: () -> Float,
    backgroundColor: Color
) {
    KLottieAnimation(
        composition = composition,
        progress = { progress() },
        modifier = modifier,
        backgroundColor = backgroundColor
    )
}


