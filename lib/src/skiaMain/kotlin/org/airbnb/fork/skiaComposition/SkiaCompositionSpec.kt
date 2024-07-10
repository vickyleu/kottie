package org.airbnb.fork.skiaComposition


import kotlin.jvm.JvmInline


internal sealed interface SkiaCompositionSpec {
    @JvmInline
    value class Url(val url: String) : SkiaCompositionSpec
    @JvmInline
    value class File (val path: String) : SkiaCompositionSpec
    @JvmInline
    value class JsonString(val jsonString: String) : SkiaCompositionSpec
}