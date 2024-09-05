package com.peecock.ymusic.ui.components.themed

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.peecock.ymusic.ui.styling.LocalAppearance
import com.peecock.ymusic.utils.center
import com.peecock.ymusic.utils.semiBold

@Composable
fun SliderControl(
    //title: String,
    //text: String,
    state: Float,
    range: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier,
    onSlide: (Float) -> Unit = { },
    onSlideComplete: () -> Unit = { },
    toDisplay: @Composable (Float) -> String = { it.toString() },
    steps: Int = 0,
    isEnabled: Boolean = true,
    usePadding: Boolean = true,
    showValue: Boolean = true
) = Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier.height(36.dp)
) {

    Box(
        modifier = Modifier
            //.weight(1f)
            .fillMaxSize()
    ) {
        Slider(
            state = state,
            setState = onSlide,
            onSlideComplete = onSlideComplete,
            range = range,
            steps = steps,
            modifier = Modifier
                .height(36.dp)
                .alpha(if (isEnabled) 0.6f else 0.5f)
                .let { if (usePadding) it.padding(start = 12.dp) else it }
                .padding(vertical = 16.dp)
                .fillMaxWidth()
        )

        if (showValue)
            BasicText(
                text = toDisplay(state),
                style = LocalAppearance.current.typography.xs.semiBold.center,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 10.dp)
            )
    }

}