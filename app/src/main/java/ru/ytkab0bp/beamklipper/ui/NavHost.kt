package ru.ytkab0bp.beamklipper.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.ytkab0bp.beamklipper.R
import ru.ytkab0bp.beamklipper.ui.screens.ConfigScreen
import ru.ytkab0bp.beamklipper.ui.screens.HomeScreen
import ru.ytkab0bp.beamklipper.ui.screens.LogsScreen
import ru.ytkab0bp.beamklipper.ui.state.MainViewModel
import ru.ytkab0bp.beamklipper.ui.theme.Accent
import ru.ytkab0bp.beamklipper.ui.theme.Ink
import ru.ytkab0bp.beamklipper.ui.theme.Paper

const val NAV_HOME = 0
const val NAV_CONFIG = 1
const val NAV_LOGS = 2

@Composable
fun NavHost(
    isCurrentLauncher: Boolean,
    mainViewModel: MainViewModel
) {
    var nav by remember { mutableStateOf(NAV_HOME) }
    val statusInsets = WindowInsets.statusBars.asPaddingValues()
    val navBarInsets = WindowInsets.navigationBars.asPaddingValues()

    BackHandler(enabled = nav != NAV_HOME) {
        nav = NAV_HOME
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Paper)
            .padding(top = statusInsets.calculateTopPadding())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.AppName),
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 32.sp),
                color = Ink,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            Spacer(Modifier.width(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NavTabButton(
                    active = nav == NAV_HOME,
                    icon = R.drawable.ic_home_outline_28,
                    onClick = { nav = NAV_HOME }
                )
                NavTabButton(
                    active = nav == NAV_LOGS,
                    icon = R.drawable.ic_inbox_outline_28,
                    onClick = { nav = NAV_LOGS }
                )
                NavTabButton(
                    active = nav == NAV_CONFIG,
                    icon = R.drawable.ic_settings_outline_28,
                    onClick = { nav = NAV_CONFIG }
                )
}
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(bottom = navBarInsets.calculateBottomPadding()),
            contentAlignment = Alignment.TopStart
        ) {
            when (nav) {
                NAV_CONFIG -> ConfigScreen()
                NAV_LOGS -> LogsScreen()
                else -> HomeScreen(
                    isCurrentLauncher = isCurrentLauncher,
                    mainViewModel = mainViewModel
                )
            }
        }
    }
}

@Composable
private fun NavTabButton(
    active: Boolean,
    icon: Int,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RectangleShape)
            .background(if (active) Accent else Paper, RectangleShape)
            .border(2.dp, Ink, RectangleShape)
            .clickable(onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = Ink,
            modifier = Modifier.size(24.dp)
        )
    }
}
