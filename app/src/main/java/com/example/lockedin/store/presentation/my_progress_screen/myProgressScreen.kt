package com.example.lockedin.store.presentation.my_progress_screen

import android.os.Handler
import android.os.Looper
import android.text.format.DateUtils
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lockedin.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.ImeAction
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lockedin.BottomNavigationBar
import com.example.lockedin.models.*
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import com.example.lockedin.store.presentation.progress_screen.ProgressItemView
import com.example.lockedin.models.CommunityViewModel
import com.example.lockedin.store.presentation.community_feed.CommunityItem
import com.example.lockedin.store.presentation.community_feed.CommunityItemView
import com.example.lockedin.models.UserViewModel
import com.example.lockedin.store.presentation.dashboard.CommunityCard
import com.example.lockedin.ui.theme.poppinsFontFamily

@Composable
fun MyProgressScreen(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel, userViewModel: UserViewModel, communityViewModel: CommunityViewModel = viewModel()) {

    val CommunityItems = remember{ mutableListOf<CommunityItem>() }


    val userCommunities = userViewModel.userCommunities.observeAsState(emptyList()).value

    val authState = authViewModel.authState.observeAsState()
    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Unauthenticated -> navController.navigate("login")
            else -> Unit
        }
    }

    LaunchedEffect(Unit) {
        userViewModel.fetchUserCommunities()
    }

    androidx.compose.material.Scaffold(
        bottomBar = { BottomNavigationBar(navController) },
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF131313))
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Title
            androidx.compose.material.Text(
                text = "My Progress",
                fontSize = 36.sp,
                color = Color.White,
                fontFamily = poppinsFontFamily,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            CommunitiesSection(userCommunities, userViewModel, navController)
        }
    }
}


@Composable
fun CommunitiesSection(communityList: List<Community>, userViewModel: UserViewModel, navController: NavController) {
    Column(modifier = Modifier.padding(2.dp)) {

        LazyVerticalGrid(
            columns = GridCells.Fixed(1),
            modifier = Modifier.fillMaxSize()
        ) {

            println("There are ${communityList.size} communities")
            items(communityList.size) { index ->
                println("Passing into the banner: ${communityList[index].name}")
                ExpandableBanner(communityList[index], userViewModel, navController)
            }
        }
    }
}


@Composable
fun ExpandableBanner(
    community: Community,
    userViewModel: UserViewModel,
    navController: NavController
) {
    var isExpanded by remember { mutableStateOf(false) }
    var localPoints by remember { mutableStateOf<Int?>(null) }
    var localStreak by remember { mutableStateOf<Int?>(null) }
    var localConsistency by remember { mutableStateOf<Float?>(null) }


    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),

        colors = CardDefaults.cardColors(
            containerColor = Color.DarkGray
        ),
        shape = RoundedCornerShape(16.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    isExpanded = !isExpanded

                    if (isExpanded) {
                        userViewModel.fetchPointsForCommunity(community.id) { points ->
                            localPoints = points
                        }

                        userViewModel.fetchStreakForCommunity(community.id) { streak ->
                            localStreak = streak
                        }

                        userViewModel.fetchConsistencyForCommunity(community.id) { consistency ->
                            localConsistency = consistency
                        }
                    }


                }
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = community.name,
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
                    .padding(top = 8.dp)
                    .padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .padding(end = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        val communityId = community.id
                        println("Traveling to the community ${community.name}")
                        navController.navigate("ProgressScreen/${communityId}")
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF9D19A9),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier
                        .height(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .size(width = 75.dp, height = 25.dp),
                ) {

                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "View",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                }

                Spacer(Modifier.width(8.dp))

                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = Color.White
                )
            }
        }

        if (isExpanded) {
            Column(modifier = Modifier.padding(6.dp)) {

                if (localPoints == null) {
                    Text(
                        text = "Loading points...",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                } else {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 5.dp), // Adjust padding as needed
                        contentAlignment = Alignment.Center
                    ) {

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(bottom = 10.dp)
                        ) {

                            Box(
                                modifier = Modifier.padding(bottom = 10.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.white_star),
                                    contentDescription = "Image",
                                    modifier = Modifier.size(40.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = "Points: ${localPoints}",
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }


                if (localStreak == null) {
                    Text(
                        text = "Loading streak...",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                } else {

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp, end = 20.dp), // Adjust padding as needed

                        contentAlignment = Alignment.Center
                    ) {

                        Row(

                            verticalAlignment = Alignment.CenterVertically,
                            //modifier = Modifier.padding(horizontal = 8.dp),
                        ) {

                            Box(
                                modifier = Modifier.padding(bottom = 10.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.white_flame),
                                    contentDescription = "Image",
                                    modifier = Modifier.size(50.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = "${localStreak} Days",
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(3.dp)
                    ) {
                        val localPercentage = localConsistency?.div(100)
                        val stringPercentage = "${localPercentage}f"
                        val percentage = stringPercentage.toFloat()
                        PercentageBar(percentage = percentage)
                    }


                }

                val intConsistency = localConsistency?.toInt()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 1.dp)
                    ) {
                        Text(
                            text = "${intConsistency}% Consistency",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun PercentageBar(percentage: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(20.dp)
            .background(Color.Gray, shape = RoundedCornerShape(10.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(percentage.coerceIn(0f, 1f))
                .fillMaxHeight()
                .background(Color.White, shape = RoundedCornerShape(10.dp))
        )
    }
}

@Composable
fun CommunityCard(community: Community) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .aspectRatio(1f),

        colors = CardDefaults.cardColors(
            containerColor = Color.DarkGray
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = community.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}