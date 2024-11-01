package com.example.lockedin.store.presentation.community_feed

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.lockedin.MyApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import com.example.lockedin.BottomNavigationBar
import com.example.lockedin.R
import com.example.lockedin.models.AuthState
import com.example.lockedin.models.AuthViewModel
import com.example.lockedin.models.Community
import com.example.lockedin.models.CommunityViewModel
import com.example.lockedin.store.presentation.progress_screen.ProgressItem
import com.example.lockedin.store.presentation.progress_screen.ProgressItemView
import com.example.lockedin.store.presentation.util.components.LoadingDialog
import org.checkerframework.common.subtyping.qual.Bottom
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lockedin.store.presentation.community_posts.CommunityPosts

@Composable
fun CommunityFeed(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    communityViewModel: CommunityViewModel = viewModel()
) {
    val authState = authViewModel.authState.observeAsState()

    LaunchedEffect(authState.value) {
        when(authState.value){
            is AuthState.Unauthenticated -> navController.navigate("login")
            else -> Unit
        }
    }

    // Trigger community data fetching
    LaunchedEffect(Unit) {
        communityViewModel.fetchCommunities()
    }

    // Observe the community list
    val communities = communityViewModel.communityList
    val CommunityItems = remember{ mutableListOf<CommunityItem>() }

    // Clear and populate communityItems only once when the data changes
    LaunchedEffect(communities) {
        CommunityItems.clear()
        for (community in communities) {
            CommunityItems.add(CommunityItem(community.name, community.communityImage, community.id))
        }
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) },
        modifier = Modifier.background(Color.Black) // Set the Scaffold background to black
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black) // Set the Column background to black
                .padding(28.dp)
        ) {
            // Title
            Text(
                text = "\nFind Your",
                fontSize = 42.sp,
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                textAlign = TextAlign.Left
            )

            Text(
                text = "Community",
                fontSize = 54.sp,
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                textAlign = TextAlign.Left
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        // Handle button click action here
                    },
                    modifier = Modifier
                        .padding(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.LightGray.copy(alpha = 0.5f),  // Customize the button background
                        contentColor = Color.White    // Customize the text color
                    )
                ) {
                    Text(
                        text = "Join Community"
                    )  // The button label
                }

                Button(
                    onClick = {
                        // Handle button click action here
                    },
                    modifier = Modifier
                        .padding(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,  // Customize the button background
                        contentColor = Color.White    // Customize the text color
                    )
                ) {
                    Text(
                        text = "Your Communities"
                    )  // The button label
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(1),
                modifier = Modifier.fillMaxSize()
            ) {
                items(CommunityItems.size) { index ->
                    CommunityItemView(CommunityItems[index], navController)
                }
            }
        }
    }
}

@Composable
fun CommunityItemView(item: CommunityItem, navController: NavController) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1E90FF)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.samplecommunity2),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2f)
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = item.date,
                fontSize = 18.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterVertically)
            )

            Button(
                onClick = { navController.navigate("CommunityPosts/${item.communityId}") },
                modifier = Modifier.align(Alignment.CenterVertically),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                )
            ) {
                Text(
                    "Join",
                    color = Color.White
                )
            }
        }
    }
}

data class CommunityItem(val date: String, val imageRes: String, val communityId: String)
