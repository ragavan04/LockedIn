package com.example.lockedin.store.presentation.view_community

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role.Companion.Button
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.lockedin.R
import com.example.lockedin.models.*
import com.example.lockedin.store.presentation.community_posts.CommunityHeader


@Composable
fun viewCommunity(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel,
    communityViewModel: CommunityViewModel,
    communityId: String,
) {
    val authState = authViewModel.authState.observeAsState()
    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Unauthenticated -> navController.navigate("login")
            else -> Unit
        }
    }

    communityViewModel.fetchCommunityById(communityId)
    val community = communityViewModel.currentCommunity.value

    Scaffold(
        modifier = Modifier.background(Color(0xFF333333))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF131313)),
            horizontalAlignment = Alignment.CenterHorizontally // Center children horizontally
        ) {
            community?.let {
                communityHeader(community, navController, communityId)
            }

            Spacer(Modifier.height(50.dp))

            // Notification Time with Styled Box
            if (community != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .wrapContentHeight()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF2C2C2C), Color(0xFF3A3A3A))
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(vertical = 16.dp)
                        .shadow(elevation = 8.dp, shape = RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Notification Time: ${community.notificationTime}",
                        fontSize = 18.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            JoinCommunityButton(navController, userViewModel, communityId)
        }
    }
}


@Composable
fun JoinCommunityButton(
    navController: NavController,
    userViewModel: UserViewModel,
    communityId: String
) {
    var isJoined by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                userViewModel.joinUserCommunity(communityId)
                isJoined = true
                Handler(Looper.getMainLooper()).postDelayed({
                    navController.navigate("CommunityPosts/$communityId")
                }, 500)
            },
            modifier = Modifier
                .padding(16.dp)
                .size(width = 200.dp, height = 60.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isJoined) Color.Green else Color.Black,
                contentColor = Color.White
            )
        ) {
            if (isJoined) {
                Icon(
                    painter = painterResource(id = R.drawable.checkmark),
                    contentDescription = "Checkmark",
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text("Join", color = Color.White, fontSize = 18.sp)
            }
        }
    }

}


@Composable
fun communityHeader(
    community: Community, navController: NavController, communityId: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 25.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Box to overlay title and back button properly
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center // Center title
        ) {
            // Back IconButton aligned to the start
            IconButton(
                onClick = { navController.navigate("community_screen") },
                modifier = Modifier.align(Alignment.CenterStart).size(36.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "Back",
                    modifier = Modifier.size(24.dp)
                )
            }

            // Title centered
            Text(
                text = community.name,
                fontSize = 24.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(5.dp))

        // Community Description
        Text(
            text = community.description,
            fontSize = 14.sp,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(5.dp))

        // Divider
        Divider(
            color = Color.White,
            thickness = 4.dp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}


