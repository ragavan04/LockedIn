package com.example.lockedin.store.presentation.intro_pages

import android.net.Uri
import android.widget.VideoView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.lockedin.models.AuthState
import com.example.lockedin.models.AuthViewModel
import com.example.lockedin.models.CommunityViewModel
import com.example.lockedin.models.UserViewModel
import com.example.lockedin.ui.theme.poppinsFontFamily

@Composable
fun Welcome(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel,
    communityViewModel: CommunityViewModel = viewModel()
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
                                val videoUri = Uri.parse("android.resource://${context.packageName}/raw/welcomevid")
                                setVideoURI(videoUri)
                                setOnPreparedListener {
                                    it.isLooping = true // Loop the video
                                    start()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize() // Ensure the video takes up the entire screen
                    )

                    // Gradient Fade on Top
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp) // Gradient height
                            .align(Alignment.TopCenter)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.95f), // Increased opacity for stronger gradient
                                        Color.Black.copy(alpha = 0.7f), // Intermediate darker shade for a smoother transition
                                        Color.Transparent // Transparent at the end
                                    )
                                )
                            )
                    )

                    // Gradient Fade on Bottom
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(500.dp) // Gradient height
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent, // Transparent at the start
                                        Color.Black.copy(alpha = 0.8f), // Intermediate darker shade for a smoother transition
                                        Color.Black.copy(alpha = 1.0f) // Increased opacity for stronger gradient
                                    )
                                )
                            )
                    )

                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title Text
                Text(
                    "Welcome to LockedIn",
                    fontSize = 32.sp,
                    fontFamily = poppinsFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                // Centered Body Text
                Text(
                    "Build habits, stay accountable, \nand join an engaging community",
                    fontSize = 18.sp,
                    fontFamily = poppinsFontFamily,
                    color = Color.White, // Ensure the text is visible
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .align(Alignment.CenterHorizontally),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center // Centered alignment
                )
                // Aesthetic Button
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
                        .clickable{navController.navigate("offers")},
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
