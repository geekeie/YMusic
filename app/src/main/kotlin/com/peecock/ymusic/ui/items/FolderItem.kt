package com.peecock.ymusic.ui.items

import androidx.compose.foundation.Image
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.peecock.ymusic.R
import com.peecock.ymusic.models.Folder
import com.peecock.ymusic.ui.styling.LocalAppearance
import com.peecock.ymusic.utils.semiBold

@UnstableApi
@Composable
fun FolderItem(
    folder: Folder,
    thumbnailSizeDp: Dp,
    modifier: Modifier = Modifier,
    icon: Int = R.drawable.folder
) {
    val (colorPalette, typography) = LocalAppearance.current

    ItemContainer(
        alternative = false,
        thumbnailSizeDp = thumbnailSizeDp,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(thumbnailSizeDp)
        ) {

            Image(
                painter = painterResource(icon),
                contentDescription = null,
                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(colorPalette.text),
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(thumbnailSizeDp - 5.dp)
            )
        }

        ItemInfoContainer {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BasicText(
                    text = folder.name ?: "",
                    style = typography.xs.semiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .basicMarquee(iterations = Int.MAX_VALUE)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(modifier = Modifier.padding(horizontal = 2.dp))
            }
        }
    }
}