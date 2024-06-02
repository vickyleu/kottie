@file:OptIn(
    ExperimentalForeignApi::class,
    ExperimentalComposeUiApi::class,
    BetaInteropApi::class,
    NativeRuntimeApi::class
)

@file:Suppress(
    "INVISIBLE_MEMBER",
    "INVISIBLE_REFERENCE",
    "CANNOT_OVERRIDE_INVISIBLE_MEMBER",
    "FunctionName",
)


import androidx.compose.ui.ExperimentalComposeUiApi
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlin.native.runtime.NativeRuntimeApi
import androidx.compose.ui.window.UITouchesEventPhase
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.PointerEventType


import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.InternalComposeApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.ComposeScene
import androidx.compose.ui.LocalSystemTheme
import androidx.compose.ui.SystemTheme
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.pointer.HistoricalChange
import androidx.compose.ui.input.pointer.PointerId
//import androidx.compose.ui.interop.LocalLayerContainer
import androidx.compose.ui.interop.LocalUIKitInteropContext
import androidx.compose.ui.interop.LocalUIViewController
import androidx.compose.ui.interop.UIKitInteropContext
import androidx.compose.ui.interop.UIKitInteropTransaction
import androidx.compose.ui.platform.*
import androidx.compose.ui.semantics.SemanticsOwner
import androidx.compose.ui.text.input.PlatformTextInputService
import androidx.compose.ui.uikit.*
import androidx.compose.ui.unit.*
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExportObjCClass
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.readValue
import kotlinx.cinterop.useContents
import kotlinx.coroutines.Dispatchers
import org.jetbrains.skia.Canvas
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.OSVersion
import org.jetbrains.skiko.SkikoUIView
import platform.CoreGraphics.CGPoint
import org.jetbrains.skiko.available
import platform.CoreGraphics.CGAffineTransformIdentity
import platform.CoreGraphics.CGAffineTransformInvert
import platform.CoreGraphics.CGPointMake
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSize
import platform.CoreGraphics.CGSizeEqualToSize
import platform.Foundation.*
import platform.UIKit.*
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue


//
//private val uiContentSizeCategoryToFontScaleMap = mapOf(
//    UIContentSizeCategoryExtraSmall to 0.8f,
//    UIContentSizeCategorySmall to 0.85f,
//    UIContentSizeCategoryMedium to 0.9f,
//    UIContentSizeCategoryLarge to 1f, // default preference
//    UIContentSizeCategoryExtraLarge to 1.1f,
//    UIContentSizeCategoryExtraExtraLarge to 1.2f,
//    UIContentSizeCategoryExtraExtraExtraLarge to 1.3f,
//
//    // These values don't work well if they match scale shown by
//    // Text Size control hint, because iOS uses non-linear scaling
//    // calculated by UIFontMetrics, while Compose uses linear.
//    UIContentSizeCategoryAccessibilityMedium to 1.4f, // 160% native
//    UIContentSizeCategoryAccessibilityLarge to 1.5f, // 190% native
//    UIContentSizeCategoryAccessibilityExtraLarge to 1.6f, // 235% native
//    UIContentSizeCategoryAccessibilityExtraExtraLarge to 1.7f, // 275% native
//    UIContentSizeCategoryAccessibilityExtraExtraExtraLarge to 1.8f, // 310% native
//
//    // UIContentSizeCategoryUnspecified
//)
//
//fun TransparentComposeUIViewController(content: @Composable () -> Unit): UIViewController =
//    TransparentComposeUIViewController(configure = {}, content = content)
//
//fun TransparentComposeUIViewController(
//    configure: ComposeUIViewControllerConfiguration.() -> Unit = {},
//    content: @Composable () -> Unit
//): UIViewController =
//    ComposeWindow().apply {
//        configuration = ComposeUIViewControllerConfiguration()
//            .apply(configure)
//        setContent(content)
//    }
//
//private class AttachedComposeContext(
//    val scene: ComposeScene,
//    val view: SkikoUIView,
//    val interopContext: UIKitInteropContext
//) {
//    private var constraints: List<NSLayoutConstraint> = emptyList()
//        set(value) {
//            if (field.isNotEmpty()) {
//                NSLayoutConstraint.deactivateConstraints(field)
//            }
//            field = value
//            NSLayoutConstraint.activateConstraints(value)
//        }
//
//    fun setConstraintsToCenterInView(parentView: UIView, size: CValue<CGSize>) {
//        size.useContents {
//            constraints = listOf(
//                view.centerXAnchor.constraintEqualToAnchor(parentView.centerXAnchor),
//                view.centerYAnchor.constraintEqualToAnchor(parentView.centerYAnchor),
//                view.widthAnchor.constraintEqualToConstant(width),
//                view.heightAnchor.constraintEqualToConstant(height)
//            )
//        }
//    }
//
//    fun setConstraintsToFillView(parentView: UIView) {
//        constraints = listOf(
//            view.leftAnchor.constraintEqualToAnchor(parentView.leftAnchor),
//            view.rightAnchor.constraintEqualToAnchor(parentView.rightAnchor),
//            view.topAnchor.constraintEqualToAnchor(parentView.topAnchor),
//            view.bottomAnchor.constraintEqualToAnchor(parentView.bottomAnchor)
//        )
//    }
//
//    fun dispose() {
//        scene.close()
//        // After scene is disposed all UIKit interop actions can't be deferred to be synchronized with rendering
//        // Thus they need to be executed now.
//        interopContext.retrieve().actions.forEach { it.invoke() }
//        view.dispose()
//    }
//}
//
//@OptIn(InternalComposeApi::class)
//@ExportObjCClass
//internal class ComposeWindow : UIViewController {
//
//    internal lateinit var configuration: ComposeUIViewControllerConfiguration
//    private val keyboardOverlapHeightState = mutableStateOf(0f)
//    private var isInsideSwiftUI = false
//    private var safeAreaState by mutableStateOf(PlatformInsets())
//    private var layoutMarginsState by mutableStateOf(PlatformInsets())
//
//    /*
//     * Initial value is arbitarily chosen to avoid propagating invalid value logic
//     * It's never the case in real usage scenario to reflect that in type system
//     */
//    private val interfaceOrientationState = mutableStateOf(
//        InterfaceOrientation.Portrait
//    )
//
//    private val systemTheme = mutableStateOf(
//        traitCollection.userInterfaceStyle.asComposeSystemTheme()
//    )
//
//    /*
//     * On iOS >= 13.0 interfaceOrientation will be deduced from [UIWindowScene] of [UIWindow]
//     * to which our [ComposeWindow] is attached.
//     * It's never UIInterfaceOrientationUnknown, if accessed after owning [UIWindow] was made key and visible:
//     * https://developer.apple.com/documentation/uikit/uiwindow/1621601-makekeyandvisible?language=objc
//     */
//    private val currentInterfaceOrientation: InterfaceOrientation?
//        get() {
//            // Modern: https://developer.apple.com/documentation/uikit/uiwindowscene/3198088-interfaceorientation?language=objc
//            // Deprecated: https://developer.apple.com/documentation/uikit/uiapplication/1623026-statusbarorientation?language=objc
//            return if (available(OS.Ios to OSVersion(13))) {
//                view.window?.windowScene?.interfaceOrientation?.let {
//                    InterfaceOrientation.getByRawValue(it)
//                }
//            } else {
//                InterfaceOrientation.getByRawValue(UIApplication.sharedApplication.statusBarOrientation)
//            }
//        }
//
//    private val _windowInfo = WindowInfoImpl().apply {
//        isWindowFocused = true
//    }
//
//    @OverrideInit
//    constructor() :  super(nibName = null, bundle = null)
//
//    @OverrideInit
//    constructor(coder: NSCoder) : super(coder)
//
//    private val fontScale: Float
//        get() {
//            val contentSizeCategory =
//                traitCollection.preferredContentSizeCategory ?: UIContentSizeCategoryUnspecified
//
//            return uiContentSizeCategoryToFontScaleMap[contentSizeCategory] ?: 1.0f
//        }
//
//    private val density: Density
//        get() = Density(
//            attachedComposeContext?.view?.contentScaleFactor?.toFloat() ?: 1f,
//            fontScale
//        )
//
//    private lateinit var content: @Composable () -> Unit
//
//    private var attachedComposeContext: AttachedComposeContext? = null
//
//    private val keyboardVisibilityListener = object : NSObject() {
//        @Suppress("unused")
//        @ObjCAction
//        fun keyboardWillShow(arg: NSNotification) {
//            val keyboardInfo = arg.userInfo!!["UIKeyboardFrameEndUserInfoKey"] as NSValue
//            val keyboardHeight = keyboardInfo.CGRectValue().useContents { size.height }
//            val screenHeight = UIScreen.mainScreen.bounds.useContents { size.height }
//
//            val composeViewBottomY = UIScreen.mainScreen.coordinateSpace.convertPoint(
//                point = CGPointMake(0.0, view.frame.useContents { size.height }),
//                fromCoordinateSpace = view.coordinateSpace
//            ).useContents { y }
//            val bottomIndent = screenHeight - composeViewBottomY
//
//            if (bottomIndent < keyboardHeight) {
//                keyboardOverlapHeightState.value = (keyboardHeight - bottomIndent).toFloat()
//            }
//
//            val scene = attachedComposeContext?.scene ?: return
//
//            if (configuration.onFocusBehavior == OnFocusBehavior.FocusableAboveKeyboard) {
//                val focusedRect = scene.mainOwner?.focusOwner?.getFocusRect()?.toDpRect(density)
//
//                if (focusedRect != null) {
//                    updateViewBounds(
//                        offsetY = calcFocusedLiftingY(focusedRect, keyboardHeight)
//                    )
//                }
//            }
//        }
//
//        @Suppress("unused")
//        @ObjCAction
//        fun keyboardWillHide(arg: NSNotification) {
//            keyboardOverlapHeightState.value = 0f
//            if (configuration.onFocusBehavior == OnFocusBehavior.FocusableAboveKeyboard) {
//                updateViewBounds(offsetY = 0.0)
//            }
//        }
//
//        private fun calcFocusedLiftingY(focusedRect: DpRect, keyboardHeight: Double): Double {
//            val viewHeight = attachedComposeContext?.view?.frame?.useContents {
//                size.height
//            } ?: 0.0
//
//            val hiddenPartOfFocusedElement: Double =
//                keyboardHeight - viewHeight + focusedRect.bottom.value
//            return if (hiddenPartOfFocusedElement > 0) {
//                // If focused element is partially hidden by the keyboard, we need to lift it upper
//                val focusedTopY = focusedRect.top.value
//                val isFocusedElementRemainsVisible = hiddenPartOfFocusedElement < focusedTopY
//                if (isFocusedElementRemainsVisible) {
//                    // We need to lift focused element to be fully visible
//                    hiddenPartOfFocusedElement
//                } else {
//                    // In this case focused element height is bigger than remain part of the screen after showing the keyboard.
//                    // Top edge of focused element should be visible. Same logic on Android.
//                    maxOf(focusedTopY, 0f).toDouble()
//                }
//            } else {
//                // Focused element is not hidden by the keyboard.
//                0.0
//            }
//        }
//
//        private fun updateViewBounds(offsetX: Double = 0.0, offsetY: Double = 0.0) {
//            view.layer.setBounds(
//                view.frame.useContents {
//                    CGRectMake(
//                        x = offsetX,
//                        y = offsetY,
//                        width = size.width,
//                        height = size.height
//                    )
//                }
//            )
//        }
//    }
//
//    @Suppress("unused")
//    @ObjCAction
//    fun viewSafeAreaInsetsDidChange() {
//        // super.viewSafeAreaInsetsDidChange() // TODO: call super after Kotlin 1.8.20
//        view.safeAreaInsets.useContents {
//            safeAreaState = PlatformInsets(
//                left = left.dp,
//                top = top.dp,
//                right = right.dp,
//                bottom = bottom.dp,
//            )
//        }
//        view.directionalLayoutMargins.useContents {
//            layoutMarginsState = PlatformInsets(
//                left = leading.dp, // TODO: Check RTL support
//                top = top.dp,
//                right = trailing.dp, // TODO: Check RTL support
//                bottom = bottom.dp,
//            )
//        }
//    }
//
//    override fun loadView() {
//        view = UIView().apply {
//            backgroundColor = UIColor.clearColor
//            opaque = true
//            setClipsToBounds(true)
//        } // rootView needs to interop with UIKit
//    }
//
//    override fun viewDidLoad() {
//        super.viewDidLoad()
//
//        PlistSanityCheck.performIfNeeded()
//
//        configuration.delegate.viewDidLoad()
//    }
//
//    override fun traitCollectionDidChange(previousTraitCollection: UITraitCollection?) {
//        super.traitCollectionDidChange(previousTraitCollection)
//
//        systemTheme.value = traitCollection.userInterfaceStyle.asComposeSystemTheme()
//    }
//
//    override fun viewWillLayoutSubviews() {
//        super.viewWillLayoutSubviews()
//
//        // UIKit possesses all required info for layout at this point
//        currentInterfaceOrientation?.let {
//            interfaceOrientationState.value = it
//        }
//
//        attachedComposeContext?.let {
//            updateLayout(it)
//        }
//    }
//
//    private fun updateLayout(context: AttachedComposeContext) {
//        val scale = density.density
//        val size = view.frame.useContents {
//            IntSize(
//                width = (size.width * scale).roundToInt(),
//                height = (size.height * scale).roundToInt()
//            )
//        }
//        _windowInfo.containerSize = size
//        context.scene.density = density
//        context.scene.constraints = Constraints(
//            maxWidth = size.width,
//            maxHeight = size.height
//        )
//
//        context.view.needRedraw()
//    }
//
//    override fun viewWillTransitionToSize(
//        size: CValue<CGSize>,
//        withTransitionCoordinator: UIViewControllerTransitionCoordinatorProtocol
//    ) {
//        super.viewWillTransitionToSize(size, withTransitionCoordinator)
//
//        if (isInsideSwiftUI || presentingViewController != null) {
//            // SwiftUI will do full layout and scene constraints update on each frame of orientation change animation
//            // This logic is not needed
//
//            // When presented modally, UIKit performs non-trivial hierarchy update durting orientation change,
//            // its logic is not feasible to integrate into
//            return
//        }
//
//        val attachedComposeContext = attachedComposeContext ?: return
//
//        // Happens during orientation change from LandscapeLeft to LandscapeRight, for example
//        val isSameSizeTransition = view.frame.useContents {
//            CGSizeEqualToSize(size, this.size.readValue())
//        }
//        if (isSameSizeTransition) {
//            return
//        }
//
//        val startSnapshotView =
//            attachedComposeContext.view.snapshotViewAfterScreenUpdates(false) ?: return
//
//        startSnapshotView.translatesAutoresizingMaskIntoConstraints = false
//        view.addSubview(startSnapshotView)
//        size.useContents {
//            NSLayoutConstraint.activateConstraints(
//                listOf(
//                    startSnapshotView.widthAnchor.constraintEqualToConstant(height),
//                    startSnapshotView.heightAnchor.constraintEqualToConstant(width),
//                    startSnapshotView.centerXAnchor.constraintEqualToAnchor(view.centerXAnchor),
//                    startSnapshotView.centerYAnchor.constraintEqualToAnchor(view.centerYAnchor)
//                )
//            )
//        }
//
//        attachedComposeContext.view.isForcedToPresentWithTransactionEveryFrame = true
//
//        attachedComposeContext.setConstraintsToCenterInView(view, size)
//        attachedComposeContext.view.transform = withTransitionCoordinator.targetTransform
//
//        view.layoutIfNeeded()
//
//        withTransitionCoordinator.animateAlongsideTransition(
//            animation = {
//                startSnapshotView.alpha = 0.0
//                startSnapshotView.transform =
//                    CGAffineTransformInvert(withTransitionCoordinator.targetTransform)
//                attachedComposeContext.view.transform = CGAffineTransformIdentity.readValue()
//            },
//            completion = {
//                startSnapshotView.removeFromSuperview()
//                attachedComposeContext.setConstraintsToFillView(view)
//                attachedComposeContext.view.isForcedToPresentWithTransactionEveryFrame = false
//            }
//        )
//    }
//
//    override fun viewWillAppear(animated: Boolean) {
//        super.viewWillAppear(animated)
//
//        isInsideSwiftUI = checkIfInsideSwiftUI()
//        attachComposeIfNeeded()
//        configuration.delegate.viewWillAppear(animated)
//    }
//
//    override fun viewDidAppear(animated: Boolean) {
//        super.viewDidAppear(animated)
//
//        NSNotificationCenter.defaultCenter.addObserver(
//            observer = keyboardVisibilityListener,
//            selector = NSSelectorFromString(keyboardVisibilityListener::keyboardWillShow.name + ":"),
//            name = UIKeyboardWillShowNotification,
//            `object` = null
//        )
//        NSNotificationCenter.defaultCenter.addObserver(
//            observer = keyboardVisibilityListener,
//            selector = NSSelectorFromString(keyboardVisibilityListener::keyboardWillHide.name + ":"),
//            name = UIKeyboardWillHideNotification,
//            `object` = null
//        )
//
//        configuration.delegate.viewDidAppear(animated)
//
//    }
//
//    // viewDidUnload() is deprecated and not called.
//    override fun viewWillDisappear(animated: Boolean) {
//        super.viewWillDisappear(animated)
//
//        NSNotificationCenter.defaultCenter.removeObserver(
//            observer = keyboardVisibilityListener,
//            name = UIKeyboardWillShowNotification,
//            `object` = null
//        )
//        NSNotificationCenter.defaultCenter.removeObserver(
//            observer = keyboardVisibilityListener,
//            name = UIKeyboardWillHideNotification,
//            `object` = null
//        )
//
//        configuration.delegate.viewWillDisappear(animated)
//    }
//
//    override fun viewDidDisappear(animated: Boolean) {
//        super.viewDidDisappear(animated)
//
//        dispose()
//
//        dispatch_async(dispatch_get_main_queue()) {
//            kotlin.native.internal.GC.collect()
//        }
//
//        configuration.delegate.viewDidDisappear(animated)
//    }
//
//    override fun didReceiveMemoryWarning() {
//        println("didReceiveMemoryWarning")
//        kotlin.native.internal.GC.collect()
//        super.didReceiveMemoryWarning()
//    }
//
//    fun setContent(
//        content: @Composable () -> Unit
//    ) {
//        this.content = content
//    }
//
//    fun dispose() {
//        attachedComposeContext?.dispose()
//        attachedComposeContext = null
//    }
//
//    private fun attachComposeIfNeeded() {
//        if (attachedComposeContext != null) {
//            return // already attached
//        }
//
//        val skikoUIView = SkikoUIView()
//
//        val interopContext = UIKitInteropContext(requestRedraw = skikoUIView::needRedraw)
//
//        skikoUIView.translatesAutoresizingMaskIntoConstraints = false
//        view.addSubview(skikoUIView)
//
//        val inputServices = UIKitTextInputService(
//            showSoftwareKeyboard = {
//                skikoUIView.showScreenKeyboard()
//            },
//            hideSoftwareKeyboard = {
//                skikoUIView.hideScreenKeyboard()
//            },
//            updateView = {
//                skikoUIView.setNeedsDisplay() // redraw on next frame
//                platform.QuartzCore.CATransaction.flush() // clear all animations
//                skikoUIView.reloadInputViews() // update input (like screen keyboard)
//            },
//            textWillChange = { skikoUIView.textWillChange() },
//            textDidChange = { skikoUIView.textDidChange() },
//            selectionWillChange = { skikoUIView.selectionWillChange() },
//            selectionDidChange = { skikoUIView.selectionDidChange() },
//        )
//
//        val inputTraits = inputServices.skikoUITextInputTraits
//
//        val platform = object : Platform by Platform.Empty {
//            override var dialogScrimBlendMode by mutableStateOf(BlendMode.SrcOver)
//
//            override val focusManager = EmptyFocusManager
//            override fun accessibilityController(owner: SemanticsOwner) =
//                object : AccessibilityController {
//                    override fun onSemanticsChange() = Unit
//                    override fun onLayoutChange(layoutNode: androidx.compose.ui.node.LayoutNode) =
//                        Unit
//                    override suspend fun syncLoop() = Unit
//                }
//
//            override val windowInfo: WindowInfo
//                get() = _windowInfo
//            override val textInputService: PlatformTextInputService = inputServices
//            override val viewConfiguration =
//                object : ViewConfiguration {
//                    override val longPressTimeoutMillis: Long get() = 500
//                    override val doubleTapTimeoutMillis: Long get() = 300
//                    override val doubleTapMinTimeMillis: Long get() = 40
//
//                    // this value is originating from iOS 16 drag behavior reverse engineering
//                    override val touchSlop: Float get() = with(density) { 10.dp.toPx() }
//                }
//            override val textToolbar = object : TextToolbar {
//                override fun showMenu(
//                    rect: Rect,
//                    onCopyRequested: (() -> Unit)?,
//                    onPasteRequested: (() -> Unit)?,
//                    onCutRequested: (() -> Unit)?,
//                    onSelectAllRequested: (() -> Unit)?
//                ) {
//                    val skiaRect = with(density) {
//                        org.jetbrains.skia.Rect.makeLTRB(
//                            l = rect.left / density,
//                            t = rect.top / density,
//                            r = rect.right / density,
//                            b = rect.bottom / density,
//                        )
//                    }
//                    skikoUIView.showTextMenu(
//                        targetRect = skiaRect,
//                        textActions = object : TextActions {
//                            override val copy: (() -> Unit)? = onCopyRequested
//                            override val cut: (() -> Unit)? = onCutRequested
//                            override val paste: (() -> Unit)? = onPasteRequested
//                            override val selectAll: (() -> Unit)? = onSelectAllRequested
//                        }
//                    )
//                }
//
//                /**
//                 * TODO on UIKit native behaviour is hide text menu, when touch outside
//                 */
//                override fun hide() = skikoUIView.hideTextMenu()
//
//                override val status: TextToolbarStatus
//                    get() = if (skikoUIView.isTextMenuShown())
//                        TextToolbarStatus.Shown
//                    else
//                        TextToolbarStatus.Hidden
//            }
//
//            override val inputModeManager = DefaultInputModeManager(InputMode.Touch)
//        }
//
//        val scene = ComposeScene(
//            coroutineContext = Dispatchers.Main,
//            platform = platform,
//            density = density,
//            invalidate = skikoUIView::needRedraw,
//        )
//        val isReadyToShowContent = mutableStateOf(false)
//
//        skikoUIView.input = inputServices.skikoInput
//        skikoUIView.inputTraits = inputTraits
//        skikoUIView.delegate = object : SkikoUIViewDelegate {
//            override fun onKeyboardEvent(event: SkikoKeyboardEvent) {
//                scene.sendKeyEvent(KeyEvent(event))
//            }
//
//            override fun pointInside(point: CValue<CGPoint>, event: UIEvent?): Boolean =
//                point.useContents {
//                    val position = Offset(
//                        (x * density.density).toFloat(),
//                        (y * density.density).toFloat()
//                    )
//
//                    !scene.hitTestInteropView(position)
//                }
//
//            override fun onTouchesEvent(view: UIView, event: UIEvent, phase: UITouchesEventPhase) {
//                val density = density.density
//
//                scene.sendPointerEvent(
//                    eventType = phase.toPointerEventType(),
//                    pointers = event.touchesForView(view)?.map {
//                        val touch = it as UITouch
//                        val id = touch.hashCode().toLong()
//
//                        val position = touch.offsetInView(view, density)
//
//                        ComposeScene.Pointer(
//                            id = PointerId(id),
//                            position = position,
//                            pressed = touch.isPressed,
//                            type = PointerType.Touch,
//                            pressure = touch.force.toFloat(),
//                            historical = event.historicalChangesForTouch(touch, view, density)
//                        )
//                    } ?: emptyList(),
//                    timeMillis = (event.timestamp * 1e3).toLong(),
//                    nativeEvent = event
//                )
//            }
//
//            override fun retrieveInteropTransaction(): UIKitInteropTransaction =
//                interopContext.retrieve()
//
//            override fun render(canvas: Canvas, targetTimestamp: NSTimeInterval) {
//                // The calculation is split in two instead of
//                // `(targetTimestamp * 1e9).toLong()`
//                // to avoid losing precision for fractional part
//                val integral = floor(targetTimestamp)
//                val fractional = targetTimestamp - integral
//                val secondsToNanos = 1_000_000_000L
//                val nanos =
//                    integral.roundToLong() * secondsToNanos + (fractional * 1e9).roundToLong()
//
//                scene.render(canvas, nanos)
//            }
//
//            override fun onAttachedToWindow() {
//                attachedComposeContext!!.scene.density = density
//                isReadyToShowContent.value = true
//            }
//        }
//
//        scene.setContent(
//            onPreviewKeyEvent = inputServices::onPreviewKeyEvent,
//            onKeyEvent = { false },
//            content = {
//                if (!isReadyToShowContent.value) return@setContent
//                CompositionLocalProvider(
//                    LocalLayerContainer provides view,
//                    LocalUIViewController provides this,
//                    LocalKeyboardOverlapHeightState provides keyboardOverlapHeightState,
//                    LocalSafeArea provides safeAreaState,
//                    LocalLayoutMargins provides layoutMarginsState,
//                    LocalInterfaceOrientationState provides interfaceOrientationState,
//                    LocalSystemTheme provides systemTheme.value,
//                    LocalUIKitInteropContext provides interopContext,
//                    content = content
//                )
//            },
//        )
//
//        attachedComposeContext =
//            AttachedComposeContext(scene, skikoUIView, interopContext).also {
//                it.setConstraintsToFillView(view)
//                updateLayout(it)
//            }
//    }
//}
//
//private fun UITouch.offsetInView(view: UIView, density: Float): Offset =
//    locationInView(view).useContents {
//        Offset(x.toFloat() * density, y.toFloat() * density)
//    }
//
//private fun UIEvent.historicalChangesForTouch(
//    touch: UITouch,
//    view: UIView,
//    density: Float
//): List<HistoricalChange> {
//    val touches = coalescedTouchesForTouch(touch) ?: return emptyList()
//
//    return if (touches.size > 1) {
//        // subList last index is exclusive, so the last touch in the list is not included
//        // because it's the  touch for which coalesced touches were requested
//        touches.subList(0, touches.size - 1).map {
//            val historicalTouch = it as UITouch
//            HistoricalChange(
//                uptimeMillis = (historicalTouch.timestamp * 1e3).toLong(),
//                position = historicalTouch.offsetInView(view, density)
//            )
//        }
//    } else {
//        emptyList()
//    }
//}
//
//private val UITouch.isPressed
//    get() = when (phase) {
//        UITouchPhase.UITouchPhaseEnded, UITouchPhase.UITouchPhaseCancelled -> false
//        else -> true
//    }
//
//private fun UITouchesEventPhase.toPointerEventType(): PointerEventType =
//    when (this) {
//        UITouchesEventPhase.BEGAN -> PointerEventType.Press
//        UITouchesEventPhase.MOVED -> PointerEventType.Move
//        UITouchesEventPhase.ENDED -> PointerEventType.Release
//        UITouchesEventPhase.CANCELLED -> PointerEventType.Release
//    }
//
//private fun UIViewController.checkIfInsideSwiftUI(): Boolean {
//    var parent = parentViewController
//
//    while (parent != null) {
//        val isUIHostingController = parent.`class`()?.let {
//            val className = NSStringFromClass(it)
//            // SwiftUI UIHostingController has mangled name depending on generic instantiation type,
//            // It always contains UIHostingController substring though
//            return className.contains("UIHostingController")
//        } ?: false
//
//        if (isUIHostingController) {
//            return true
//        }
//
//        parent = parent.parentViewController
//    }
//
//    return false
//}
//
//private fun UIUserInterfaceStyle.asComposeSystemTheme(): SystemTheme {
//    return when (this) {
//        UIUserInterfaceStyle.UIUserInterfaceStyleLight -> SystemTheme.Light
//        UIUserInterfaceStyle.UIUserInterfaceStyleDark -> SystemTheme.Dark
//        else -> SystemTheme.Unknown
//    }
//}