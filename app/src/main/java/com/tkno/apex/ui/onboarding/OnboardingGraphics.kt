package com.tkno.apex.ui.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tkno.apex.R

// Official Apex Brand Color
val ApexBlue = Color(0xFF48AFFF)

/**
 * Screen 1 Illustration: Hero Vector Graphic
 * Spans full width from far right to far left.
 */
@Composable
fun WelcomeIllustration(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.ic_onboarding_welcome_hero),
        contentDescription = null,
        modifier = modifier.fillMaxWidth(),
        contentScale = ContentScale.FillWidth
    )
}

/**
 * Screen 2 Illustration: Features Vector Graphic
 * Centered illustration.
 */
@Composable
fun FeaturesIllustration(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_onboarding_features_hero),
            contentDescription = null,
            modifier = Modifier.size(240.dp),
            contentScale = ContentScale.Fit
        )
    }
}

/**
 * Screen 3 Illustration: Level Up Vector Graphic
 * Centered illustration.
 */
@Composable
fun LevelUpIllustration(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_onboarding_level_up_hero),
            contentDescription = null,
            modifier = Modifier.size(240.dp),
            contentScale = ContentScale.Fit
        )
    }
}

/**
 * Apex Official Brand Badge
 */
@Composable
fun ApexBrandBadge(
    modifier: Modifier = Modifier,
    iconSize: Dp = 22.dp
) {
    Image(
        painter = painterResource(id = R.drawable.ic_app_logo),
        contentDescription = null,
        modifier = modifier
            .size(iconSize)
            .clip(CircleShape)
    )
}
