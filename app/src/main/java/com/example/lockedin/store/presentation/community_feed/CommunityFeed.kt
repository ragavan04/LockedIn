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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.TextField
import com.example.lockedin.models.UserViewModel
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

@Composable
fun CommunityFeed(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel,
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

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = {
                        navController.navigate("CreateCommunityScreen")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier
                        .size(45.dp)
                ) {
                    Text(text = "+", fontSize = 25.sp)
                }
            }

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
                .fillMaxWidth(),
//                .padding(horizontal = 1.dp),  // Move padding to Row instead of TextField
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                var searchQuery by remember { mutableStateOf("") }

                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search for communities...", color = Color.Gray) },
                    modifier = Modifier
//                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 16.dp),
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

                IconButton(
                    onClick = {
                        if (searchQuery.isEmpty()) {
                            communityViewModel.fetchCommunities()
                        } else {
                            println("Search Query is looking for: ${searchQuery}")
                            communityViewModel.searchCommunities(searchQuery)
                        }
                    },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.White
                    )
                }



            }

            Spacer(modifier = Modifier.height(40.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(1),
                modifier = Modifier.fillMaxSize()
            ) {
                items(CommunityItems.size) { index ->
                    CommunityItemView(CommunityItems[index], navController, userViewModel)
                }
            }
        }
    }
}

@Composable
fun CommunityItemView(item: CommunityItem, navController: NavController, userViewModel: UserViewModel) {
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
                onClick = {

                    userViewModel.joinUserCommunity(item.communityId)
                    navController.navigate("CommunityPosts/${item.communityId}")

                          },
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
