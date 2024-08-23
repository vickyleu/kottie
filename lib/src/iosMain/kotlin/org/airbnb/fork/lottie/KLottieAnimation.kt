@file:OptIn(ExperimentalForeignApi::class)
package org.airbnb.fork.lottie


import Lottie.CompatibleAnimationView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.NSLayoutConstraint
import platform.UIKit.UIColor
import platform.UIKit.UIView


@Composable
internal fun KLottieAnimation(
    modifier: Modifier,
    composition: Any?,
    progress: () -> Float,
    backgroundColor: Color,
){
    when (composition as? CompatibleAnimationView) {
        null -> {}
        else -> {
            UIKitView(
                factory = {
                    UIView().apply {
                        this.backgroundColor = UIColor.clearColor
                        this.opaque = false
                        this.setClipsToBounds(true)
                    }
                },
                modifier = modifier,
                update = { view ->
                    if(composition.superview == null) {
                        view.backgroundColor =  UIColor.clearColor
                        view.opaque = true
                        composition.translatesAutoresizingMaskIntoConstraints = false
                        view.addSubview(composition)
                        NSLayoutConstraint.activateConstraints(
                            listOf(
                                composition.widthAnchor.constraintEqualToAnchor(view.widthAnchor),
                                composition.heightAnchor.constraintEqualToAnchor(view.heightAnchor)
                            )
                        )
                    }
                },
                background = backgroundColor
            )
        }
    }
}

