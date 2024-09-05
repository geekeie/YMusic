package com.peecock.ymusic.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import com.peecock.ymusic.R
import com.peecock.ymusic.enums.IconLikeType

@Composable
fun getLikedIcon(): Int {
    val iconLikeType by rememberPreference(iconLikeTypeKey, IconLikeType.Essential)

    return when (iconLikeType) {
        IconLikeType.Essential -> R.drawable.heart
        IconLikeType.Gift -> R.drawable.heart_gift
        IconLikeType.Apple -> R.drawable.heart_apple
        IconLikeType.Brilliant -> R.drawable.heart_brilliant
        IconLikeType.Shape -> R.drawable.heart_shape
        IconLikeType.Breaked -> R.drawable.heart_breaked_no
        IconLikeType.Striped -> R.drawable.heart_striped
    }
}

@Composable
fun getUnlikedIcon(): Int {
    val iconLikeType by rememberPreference(iconLikeTypeKey, IconLikeType.Essential)

    return when (iconLikeType) {
        IconLikeType.Essential -> R.drawable.heart_outline
        IconLikeType.Gift -> R.drawable.heart_gift_outline
        IconLikeType.Apple -> R.drawable.heart_apple_outline
        IconLikeType.Brilliant -> R.drawable.heart_brilliant_outline
        IconLikeType.Shape -> R.drawable.heart_shape_outline
        IconLikeType.Breaked -> R.drawable.heart_breaked_yes
        IconLikeType.Striped -> R.drawable.heart_striped_outline
    }
}