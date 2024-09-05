package com.peecock.ymusic.ui.screens.player

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.util.UnstableApi
import com.peecock.ymusic.R
import com.peecock.ymusic.ui.components.themed.SecondaryTextButton
import com.peecock.ymusic.ui.styling.LocalAppearance
import com.peecock.ymusic.utils.hasPermission
import com.peecock.ymusic.utils.isCompositionLaunched
import com.peecock.ymusic.utils.rememberPreference
import com.peecock.ymusic.utils.semiBold
import com.peecock.ymusic.utils.visualizerEnabledKey

@UnstableApi
@Composable
fun NextVisualizer(
    isDisplayed: Boolean
) {

    val context = LocalContext.current
    val (_,typography) = LocalAppearance.current

    val visualizerEnabled by rememberPreference(visualizerEnabledKey, false)

    if (visualizerEnabled) {

        val permission =  Manifest.permission.RECORD_AUDIO

        var relaunchPermission by remember {
            mutableStateOf(false)
        }

        var hasPermission by remember(isCompositionLaunched()) {
            mutableStateOf(context.applicationContext.hasPermission(permission))
        }

        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { hasPermission = it }
        )

        if (!hasPermission) {

            LaunchedEffect(Unit, relaunchPermission) { launcher.launch(permission) }

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BasicText(
                    text = stringResource(R.string.require_mic_permission),
                    modifier = Modifier.fillMaxWidth(0.75f),
                    style = typography.xs.semiBold
                )
                /*
                Spacer(modifier = Modifier.height(12.dp))
                SecondaryTextButton(
                    text = stringResource(R.string.grant_permission),
                    onClick = {
                        relaunchPermission = !relaunchPermission
                    }
                )
                 */
                Spacer(modifier = Modifier.height(20.dp))
                SecondaryTextButton(
                    text = stringResource(R.string.open_permission_settings),
                    onClick = {
                        context.startActivity(
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                setData(Uri.fromParts("package", context.packageName, null))
                            }
                        )
                    }
                )

            }

        } else {
        AnimatedVisibility(
            visible = isDisplayed,
            enter = fadeIn(tween(500)),
            exit = fadeOut(tween(500)),
        ) {
            com.peecock.ymusic.extensions.nextvisualizer.NextVisualizer()
        }

    }

}

}
