package com.example.lockedin.store.presentation.community_feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Text
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import coil3.compose.AsyncImage
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.lockedin.MyApp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.navigation.NavController
import com.example.lockedin.BottomNavigationBar
import com.example.lockedin.R
import com.example.lockedin.models.AuthState
import com.example.lockedin.models.AuthViewModel
import com.example.lockedin.models.CommunityViewModel
import com.example.lockedin.store.presentation.progress_screen.ProgressItemView
import com.example.lockedin.store.presentation.util.components.LoadingDialog
import org.checkerframework.common.subtyping.qual.Bottom
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lockedin.store.presentation.community_posts.CommunityPosts
import com.example.lockedin.models.UserViewModel
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.FloatingActionButton
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import com.google.firebase.auth.FirebaseAuth

@Composable
fun CommunityFeed(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel,
    communityViewModel: CommunityViewModel = viewModel()
) {
    val authState = authViewModel.authState.observeAsState()
    val isLoading by communityViewModel.isLoading
    val communities = communityViewModel.communityList

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Unauthenticated -> navController.navigate("login")
            else -> Unit
        }
    }

    LaunchedEffect(Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            communityViewModel.fetchCommunitiesForDiscover(userId)
        }
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("CreateCommunityScreen") },
                backgroundColor = Color(0xFF007BFF),
                contentColor = Color.White
            ) {
                Text(
                    text = "+",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        },
        modifier = Modifier.background(Color.Black)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(innerPadding)
                .padding(16.dp)
        ) {

            // Page Title
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


            // Search Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                var searchQuery by remember { mutableStateOf("") }

                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text("Search for communities...", color = Color.Gray)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF333333)),
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFF333333),
                        focusedContainerColor = Color(0xFF333333),
                        unfocusedTextColor = Color.White,
                        focusedTextColor = Color.White
                    ),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            communityViewModel.searchCommunities(searchQuery)
                        }
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (searchQuery.isEmpty()) {
                            val userId = FirebaseAuth.getInstance().currentUser?.uid
                            if (userId != null) {
                                communityViewModel.fetchCommunitiesForDiscover(userId)
                            }
                        } else {
                            communityViewModel.searchCommunities(searchQuery)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Display Loading or Communities
            if (isLoading) {
                Text(
                    text = "Loading communities...",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else if (communities.isEmpty()) {
                Text(
                    text = "No communities found.",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(1),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(communities.distinctBy { it.id }) { community ->
                        CommunityItemView(
                            CommunityItem(
                                name = community.name,
                                imageRes = community.communityImage,
                                communityId = community.id,
                                description = community.description,
                            ),
                            navController,
                            userViewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CommunityItemView(item: CommunityItem, navController: NavController, userViewModel: UserViewModel) {
    Row(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1E1E1E)) // Dark background
            .padding(16.dp), // Inner padding for content
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Community Image
        AsyncImage(
            model = item.imageRes, // Replace with dynamic image loading
            contentDescription = null,
            modifier = Modifier
                .size(64.dp) // Circular image
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Community Name, Description, and View Button
        Column(
            modifier = Modifier.weight(1f) // Occupy remaining width
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.name, // Community Name
                    fontSize = 18.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f) // Allow space for the button
                )

                // Small View Button
                Button(
                    onClick = {
                        navController.navigate("viewCommunity/${item.communityId}")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF007BFF),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp), // Smaller button padding
                    modifier = Modifier
                        .height(30.dp) // Smaller height for the button
                        .clip(RoundedCornerShape(8.dp)) // Rounded edges
                ) {
                    Text("View", fontSize = 12.sp, color = Color.White) // Smaller font size
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Community Description
            Text(
                text = item.description, // Replace with actual description
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}


data class CommunityItem(val name: String, val imageRes: String, val communityId: String, val description: String)
