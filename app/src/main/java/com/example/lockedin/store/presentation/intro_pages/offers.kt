package com.example.lockedin.store.presentation.intro_pages

import android.net.Uri
import android.widget.VideoView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.lockedin.models.AuthState
import com.example.lockedin.models.AuthViewModel
import com.example.lockedin.models.UserViewModel
import com.example.lockedin.ui.theme.poppinsFontFamily

@Composable
fun Offers(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel,
) {
    val authState = authViewModel.authState.observeAsState()
    var videoVisible by remember { mutableStateOf(false) }

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Unauthenticated -> navController.navigate("login")
            else -> Unit
        }
        videoVisible = true // Show the video with animation
    }

    Scaffold {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Video Section
            AnimatedVisibility(
                visible = videoVisible,
                enter = fadeIn()
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    AndroidView(
                        factory = { context ->
                            VideoView(context).apply {
                                val videoUri = Uri.parse("android.resource://${context.packageName}/raw/offersvid")
                                setVideoURI(videoUri)
                                setOnPreparedListener { mediaPlayer ->
                                    mediaPlayer.isLooping = true // Loop the video

                                    // Scale the video to fit the entire screen
                                    val videoWidth = mediaPlayer.videoWidth
                                    val videoHeight = mediaPlayer.videoHeight
                                    val screenWidth = context.resources.displayMetrics.widthPixels
                                    val screenHeight = context.resources.displayMetrics.heightPixels

                                    val scaleX = screenWidth.toFloat() / videoWidth
                                    val scaleY = screenHeight.toFloat() / videoHeight
                                    val scale = scaleX.coerceAtLeast(scaleY)

                                    // Apply scaling
                                    this.scaleX = scale
                                    this.scaleY = scale

                                    start()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize() // Ensure the VideoView fills the entire screen
                    )

                    // Text Overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)) // Optional: Dim background for readability
                            .align(Alignment.Center)
                    ) {
                        Text(
                            text = "What LockedIn Offers",
                            fontFamily = poppinsFontFamily,
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .align(Alignment.TopCenter) // Position text at the top center
                                .padding(top = 100.dp) // Add padding from the top edge
                        )
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {

                            Text(
                                text = "Community Engagement",
                                fontFamily = poppinsFontFamily,
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 50.dp)
                            )
                            Text(
                                text = "Join or create communities tailored to your goals",
                                fontFamily = poppinsFontFamily,
                                color = Color.White,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
                            )

                            Spacer(modifier = Modifier.height(50.dp))

                            Text(
                                text = "Accountability Features",
                                fontFamily = poppinsFontFamily,
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Timed reminders to upload your daily progress",
                                fontFamily = poppinsFontFamily,
                                color = Color.White,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
                            )

                            Spacer(modifier = Modifier.height(50.dp))

                            Text(
                                text = "Personalized Insights",
                                fontFamily = poppinsFontFamily,
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Tools to track your progress and maintain consistency",
                                fontFamily = poppinsFontFamily,
                                color = Color.White,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
                            )

                            Spacer(modifier = Modifier.height(50.dp))

                            Box(
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .fillMaxWidth(0.6f) // Set width to 60% of the screen
                                    .shadow(
                                        elevation = 8.dp, // Add shadow for 3D effect
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(50.dp),
                                        clip = false
                                    )
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0xFF1C1C1C), // Dark gray (start)
                                                Color(0xFF2C2C2C)  // Slightly lighter dark gray (end)
                                            )
                                        ),
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(50.dp)
                                    )
                                    .padding(vertical = 12.dp) // Add padding for a taller button
                                    .clickable{navController.navigate("steps")},
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    "Continue",
                                    color = Color.White,
                                    fontFamily = poppinsFontFamily,
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
