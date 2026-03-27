package com.recipeapp.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.recipeapp.viewmodel.OnboardingViewModel
import kotlinx.coroutines.launch

data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val gradient: List<Color>
)

val onboardingPages = listOf(
    OnboardingPage(
        title = "Discover Recipes",
        description = "Browse hundreds of delicious meals from around the world. Find inspiration for every occasion.",
        icon = Icons.Default.Search,
        gradient = listOf(Color(0xFFFF7043), Color(0xFFFF8A65))
    ),
    OnboardingPage(
        title = "Explore Cuisines",
        description = "Filter by category or cuisine area. From Italian pasta to Japanese sushi — it's all here.",
        icon = Icons.Default.Explore,
        gradient = listOf(Color(0xFFE91E63), Color(0xFFF48FB1))
    ),
    OnboardingPage(
        title = "Save Your Favourites",
        description = "Love a recipe? Save it to your favourites and access it anytime, even offline.",
        icon = Icons.Default.Favorite,
        gradient = listOf(Color(0xFF4CAF50), Color(0xFF81C784))
    )
)

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    var currentPage by remember { mutableIntStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    val page = onboardingPages[currentPage]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(page.gradient)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = page.icon,
                    contentDescription = null,
                    modifier = Modifier.size(72.dp),
                    tint = Color.White
                )
            }

            Spacer(Modifier.height(48.dp))

            Text(
                text = page.title,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = page.description,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                lineHeight = 26.sp
            )

            Spacer(Modifier.height(56.dp))

            // Page indicators
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                onboardingPages.indices.forEach { index ->
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(if (index == currentPage) 28.dp else 8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (index == currentPage) Color.White
                                else Color.White.copy(alpha = 0.4f)
                            )
                    )
                }
            }

            Spacer(Modifier.height(48.dp))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedVisibility(visible = currentPage > 0) {
                    TextButton(onClick = { currentPage-- }) {
                        Text("Back", color = Color.White, fontWeight = FontWeight.Medium)
                    }
                }
                if (currentPage == 0) Spacer(Modifier.weight(1f))

                Button(
                    onClick = {
                        if (currentPage < onboardingPages.lastIndex) {
                            currentPage++
                        } else {
                            coroutineScope.launch {
                                viewModel.completeOnboarding()
                                onFinish()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = page.gradient.first()
                    ),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.height(52.dp)
                ) {
                    Text(
                        text = if (currentPage == onboardingPages.lastIndex) "Get Started" else "Next",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}
