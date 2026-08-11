package com.tkno.apex.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tkno.apex.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onOnboardingFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val darkBg = MaterialTheme.colorScheme.background
    val accentBlue = Color(0xFF48AFFF)
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 3 })

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(darkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Pager Content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 60.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    when (page) {
                        0 -> OnboardingPageOne()
                        1 -> OnboardingPageTwo()
                        2 -> OnboardingPageThree()
                    }
                }
            }

            // Privacy Policy note (Shown only on Page 3 as in reference image)
            if (pagerState.currentPage == 2) {
                val privacyPolicyText = stringResource(R.string.privacy_policy)
                Text(
                    text = buildAnnotatedString {
                        append("By proceeding, you confirm you accept\nApex's ")
                        withStyle(style = SpanStyle(color = accentBlue, fontWeight = FontWeight.SemiBold)) {
                            append(privacyPolicyText)
                        }
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(34.dp))
            }

            // Page Indicator Dots
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 24.dp)
            ) {
                repeat(3) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(if (isSelected) 10.dp else 8.dp)
                            .background(
                                color = if (isSelected) accentBlue else MaterialTheme.colorScheme.outlineVariant,
                                shape = CircleShape
                            )
                    )
                }
            }

            // Bottom Navigation Button (NEXT / GET STARTED)
            Button(
                onClick = {
                    if (pagerState.currentPage < 2) {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        onOnboardingFinished()
                    }
                },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
            ) {
                Text(
                    text = if (pagerState.currentPage == 2) stringResource(R.string.btn_get_started) else stringResource(R.string.btn_next),
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
private fun OnboardingPageOne() {
    // Official Apex app launcher logo
    Image(
        painter = painterResource(id = R.drawable.ic_app_logo),
        contentDescription = stringResource(R.string.cd_apex_app_icon),
        modifier = Modifier
            .size(100.dp)
            .clip(RoundedCornerShape(24.dp))
    )

    Spacer(modifier = Modifier.height(36.dp))

    Text(
        text = stringResource(R.string.app_name),
        color = MaterialTheme.colorScheme.onBackground,
        fontSize = 26.sp,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = stringResource(R.string.onboarding_page1_desc),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 15.sp,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Center,
        lineHeight = 22.sp,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@Composable
private fun OnboardingPageTwo() {
    val accentBlue = Color(0xFF48AFFF)
    val circleContainerBg = MaterialTheme.colorScheme.surfaceContainerHigh

    // Blue bell icon in dark circle container matching Image 2
    Box(
        modifier = Modifier
            .size(105.dp)
            .background(circleContainerBg, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = stringResource(R.string.cd_notifications_icon),
            tint = accentBlue,
            modifier = Modifier.size(48.dp)
        )
    }

    Spacer(modifier = Modifier.height(36.dp))

    Text(
        text = stringResource(R.string.onboarding_page2_title),
        color = MaterialTheme.colorScheme.onBackground,
        fontSize = 26.sp,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = stringResource(R.string.onboarding_page2_desc),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 15.sp,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Center,
        lineHeight = 22.sp,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@Composable
private fun OnboardingPageThree() {
    val accentBlue = Color(0xFF48AFFF)
    val circleContainerBg = MaterialTheme.colorScheme.surfaceContainerHigh

    // Blue checkmark icon in dark circle container matching Image 1
    Box(
        modifier = Modifier
            .size(105.dp)
            .background(circleContainerBg, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = stringResource(R.string.cd_checkmark_icon),
            tint = accentBlue,
            modifier = Modifier.size(48.dp)
        )
    }

    Spacer(modifier = Modifier.height(36.dp))

    Text(
        text = stringResource(R.string.onboarding_page3_title),
        color = MaterialTheme.colorScheme.onBackground,
        fontSize = 26.sp,
        fontWeight = FontWeight.Bold
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = stringResource(R.string.onboarding_page3_desc),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 15.sp,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Center,
        lineHeight = 22.sp,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

