package org.airbnb.fork.kottieComposition

import androidx.compose.runtime.Composable
import kotlinx.cinterop.ExperimentalForeignApi
import org.airbnb.fork.lottie.lottieComposition.LottieCompositionSpec
import org.airbnb.fork.lottie.lottieComposition.rememberLottieComposition


@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun rememberKottieComposition(
    spec: KottieCompositionSpec
): Any? {
    return when(spec){
        is KottieCompositionSpec.File -> {
            (kottieComposition(spec = spec) as? LottieCompositionSpec)?.let {
                rememberLottieComposition(it)
            }
        }
        is KottieCompositionSpec.Url -> {
            (kottieComposition(spec = spec) as? LottieCompositionSpec)?.let {
                rememberLottieComposition(it)
            }
        }
        is KottieCompositionSpec.JsonString -> {
            (kottieComposition(spec = spec) as? LottieCompositionSpec)?.let {
                rememberLottieComposition(it)
            }
        }
    }
}