import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kottie.sample.shared.generated.resources.Res
import org.airbnb.fork.KottieAnimation
import org.airbnb.fork.kottieComposition.KottieCompositionSpec
import org.airbnb.fork.kottieComposition.animateKottieCompositionAsState
import org.airbnb.fork.kottieComposition.rememberKottieComposition
import org.airbnb.fork.utils.KottieConstants
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.InternalResourceApi
import org.jetbrains.compose.resources.readResourceBytes


@OptIn(InternalResourceApi::class, ExperimentalResourceApi::class)
@Composable
fun App(
    modifier: Modifier = Modifier,
) {
    val currentResourceDir = "composeResources/kottie.sample.shared.generated.resources"
    val composition = rememberKottieComposition(
        spec = KottieCompositionSpec.File("$currentResourceDir/files/Animation.json")
    )

    val animationState by animateKottieCompositionAsState(
        composition = composition,
        iterations = KottieConstants.IterateForever
    )

    MaterialTheme {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text("中间的view如何显示呢")

            KottieAnimation(
                composition = composition,
                progress = { animationState.progress },
                modifier = modifier.size(300.dp),
                backgroundColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
            )

        }
    }


}



