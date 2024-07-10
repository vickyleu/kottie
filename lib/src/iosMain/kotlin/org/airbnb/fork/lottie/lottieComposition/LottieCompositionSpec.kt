package org.airbnb.fork.lottie.lottieComposition

internal sealed interface LottieCompositionSpec {
    value class Url(val url: String) : LottieCompositionSpec
    value class File (val path: String) : LottieCompositionSpec
    value class JsonString(val jsonString: String) : LottieCompositionSpec
}