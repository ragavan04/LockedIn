    package com.example.lockedin.store.presentation.dashboard

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import androidx.compose.ui.text.font.FontWeight
import com.example.lockedin.BottomNavigationBar
import com.example.lockedin.models.Community
import com.example.lockedin.models.UserViewModel
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.LaunchedEffect

import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import com.example.lockedin.store.presentation.signup_screen.LightBlue
import com.example.lockedin.store.presentation.signup_screen.LightPurple

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    userViewModel: UserViewModel
) {
    val currentUser = com.google.firebase.Firebase.auth.currentUser
    val username = currentUser?.displayName ?: "User"


    LaunchedEffect(Unit) {
        userViewModel.fetchUserCommunities("")
        val photoUrlString = currentUser?.photoUrl?.toString() ?: ""
        userViewModel.updateProfilePic(photoUrlString)

    }

    LaunchedEffect(userViewModel.userCommunities.observeAsState().value) {
        userViewModel.updateAverageConsistency("")
    }

    val userCommunities = userViewModel.userCommunities.observeAsState(emptyList()).value


    var localConsistency: Float = 0f

    if(userViewModel.averageConsistency.value != null) {
        localConsistency = userViewModel.averageConsistency.value!!
    }


    Scaffold(
        topBar = { HomeHeader(navController) },
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            HelloMessage(username)
            Spacer(modifier = Modifier.height(16.dp))
            OverviewProgressToggle()
            Spacer(modifier = Modifier.height(16.dp))
            ConsistencyRating(localConsistency)
            Spacer(modifier = Modifier.height(16.dp))
            CommunitiesSection(userCommunities, navController)
        }


    }
}

@Composable
fun HomeHeader(navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = 16.dp)
            .background(Color.Black),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left side: Title Text
        Text(
            text = "Home",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Row {
            IconButton(onClick = { navController.navigate("community_screen") }) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.White
                )
            }
            IconButton(onClick = { navController.navigate("profile_screen") }) {
                val photoUrl = Firebase.auth.currentUser?.photoUrl

                if (photoUrl != null) {
                    // If there's a profile picture, show it
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = "Profile Image",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color.Gray, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // If no profile picture, show a white circle as a placeholder
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                }
            }
        }
    }
}

@Composable
fun HelloMessage(username: String) {
   Row(
     modifier = Modifier
         .height(200.dp)
         .padding(top = 60.dp)
//         .background(Color.Yellow)
         .fillMaxWidth(),
   ) {
       Text(
           text = "Hello,\n$username",
           fontSize = 54.sp,
           fontWeight = FontWeight.Bold,
           color = Color.White


       )
   }
}

@Composable
fun Other (username: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(128.dp)
//            .padding(top = 16.dp)
//            .background(color = "red")
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Hello\n$username",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun OverviewProgressToggle() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        PillButton("Overview", true)
        PillButton("Progress", false)
    }
}

@Composable
fun PillButton(text: String, isSelected: Boolean) {
    Button(
        onClick = { /* Handle click */ },
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) LightBlue else LightPurple,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(50)
    ) {
        Text(text)
    }
}

@Composable
fun ConsistencyRating(consistency: Float) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        LightBlue,
                        LightPurple
                    )
                ),
                RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Consistency Rating",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "See your consistency rating",
            fontSize = 14.sp,
            color = Color.LightGray
        )
        Spacer(modifier = Modifier.height(8.dp))

        val intConsistency = consistency?.toInt()

        Text(
            text = "${intConsistency}%",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        val localPercentage = consistency?.div(100)
        val stringPercentage = "${localPercentage}f"
        val percentage = stringPercentage.toFloat()

        PercentageBar(percentage)

    }
}

@Composable
fun PercentageBar(percentage: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .background(Color.Gray, shape = RoundedCornerShape(10.dp))
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth(percentage.coerceIn(0f, 1f))
                .fillMaxHeight()
                .background(Color.Blue, shape = RoundedCornerShape(10.dp))
        )
    }
}


@Composable
fun CommunitiesSection(communityList: List<Community>, navController: NavController) {
    Column(modifier = Modifier.padding(0.dp)) {
        Text(
            text = "Communities",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize()
        ) {
            items(communityList.size) { index ->
                CommunityCard(communityList[index], navController)
            }
        }
    }
}

@Composable
fun CommunityCard(community: Community, navController: NavController) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable {
                navController.navigate("CommunityPosts/${community.id}")
            },

        colors = CardDefaults.cardColors(
            containerColor = Color.DarkGray
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        AsyncImage(model = community.communityImage, contentDescription = "Community Image")
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