package org.airbnb.fork.kottieComposition

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.airbnb.fork.skiaComposition.SkiaCompositionSpec


@Composable
actual fun kottieComposition(
    spec: KottieCompositionSpec
): Any? {
    var skiaSpec by remember { mutableStateOf<SkiaCompositionSpec?>(null) }
    LaunchedEffect(spec) {
        skiaSpec = when (spec) {
            is KottieCompositionSpec.File -> {
                SkiaCompositionSpec.File(spec.path)
            }

            is KottieCompositionSpec.Url -> {
                SkiaCompositionSpec.Url(spec.url)
            }

            is KottieCompositionSpec.JsonString -> {
                SkiaCompositionSpec.JsonString(spec.jsonString)
            }
        }
    }
    return skiaSpec
}