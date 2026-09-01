package com.spacca.app.ui.screens.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spacca.app.ui.components.DefaultButton
import com.spacca.app.ui.components.DefaultText
import com.spacca.app.ui.components.clickableNoRipple
import com.spacca.app.ui.theme.AccentGreen
import com.spacca.app.ui.theme.BackgroundPrimary
import com.spacca.app.ui.theme.Grey
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import spaccamobile.composeapp.generated.resources.Res
import spaccamobile.composeapp.generated.resources.ic_onboarding_pager_1
import spaccamobile.composeapp.generated.resources.ic_onboarding_pager_2
import spaccamobile.composeapp.generated.resources.ic_onboarding_pager_3

private data class OnboardingSlide(
    val title: String,
    val subtitle: String,
    val image: DrawableResource
)

private val slides = listOf(
    OnboardingSlide(
        "Enjoy your fresh coffee with SPACCA",
        "Huge variants of cold and hot coffee",
        Res.drawable.ic_onboarding_pager_1
    ),
    OnboardingSlide(
        "Huge variants of cold and hot coffee",
        "Delicious bakeries and desserts",
        Res.drawable.ic_onboarding_pager_2
    ),
    OnboardingSlide(
        "Delicious bakeries and desserts",
        "Order your favorites and pick them up",
        Res.drawable.ic_onboarding_pager_3
    )
)

@Composable
fun OnboardingScreen(
    onGetStarted: () -> Unit,
    onContinueAsGuest: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { slides.size })

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundPrimary)
    ) {
        // Responsive pager height: capped at 430dp in portrait, shrinks in
        // landscape so the dots + buttons stay reachable. The whole column is
        // scrollable as a fallback for very short screens.
        val pagerHeight = minOf(430.dp, maxHeight * 0.55f)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Pager content flows from the top (image + text grouped together),
            // so the text sits close to the dots/buttons below.
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(pagerHeight)
            ) { page ->
                val slide = slides[page]
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Image(
                        painter = painterResource(slide.image),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )
                    DefaultText(
                        text = slide.title,
                        fontSize = 22,
                        lineHeight = 30,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 24.dp)
                    )
                    DefaultText(
                        text = slide.subtitle,
                        fontSize = 14,
                        lineHeight = 20,
                        fontColor = Grey,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

        // Page indicator dots
            Row(
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(slides.size) { index ->
                    Box(
                        modifier = Modifier
                            .size(if (pagerState.currentPage == index) 10.dp else 8.dp)
                            .clip(CircleShape)
                            .background(if (pagerState.currentPage == index) AccentGreen else Grey)
                    )
                }
            }

            DefaultButton(
                text = "Get Started",
                onClick = onGetStarted
            )
            DefaultText(
                text = "Continue as a guest",
                fontSize = 14,
                fontColor = AccentGreen,
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 24.dp)
                    .clickableNoRipple(onClick = onContinueAsGuest)
            )
        }
    }
}
