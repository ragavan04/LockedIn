package com.example.lockedin.store.presentation.progress_screen

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import com.example.lockedin.BottomNavigationBar
import com.example.lockedin.R
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.example.lockedin.models.*
import com.example.lockedin.ui.theme.poppinsFontFamily
import kotlinx.coroutines.Dispatchers
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.withContext
import java.net.URL

@Composable
fun ProgressScreen(
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    communityViewModel: CommunityViewModel,
    userViewModel: UserViewModel,
    communityId: String
) {

    val authState = authViewModel.authState.observeAsState()
    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Unauthenticated -> navController.navigate("login")
            else -> Unit
        }
    }

    LaunchedEffect(Unit) {
        communityViewModel.fetchCommunityById(communityId)
        userViewModel.fetchPostsForCommunity(communityId)
    }

    val currentCommunity = communityViewModel.currentCommunity.value
    val communityPosts = userViewModel.postsForCommunity


    Scaffold(
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
            Text(
                text = "Progress Posts",
                fontSize = 24.sp,
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                textAlign = TextAlign.Center
            )

            Text(
                text = "${currentCommunity?.name}",
                fontSize = 18.sp,
                color = Color(0xFF9D19A9),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                textAlign = TextAlign.Center
            )


            // Divider
            Divider(
                color = Color.DarkGray,
                thickness = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            // Progress pictures in grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 8.dp, end = 8.dp),
                modifier = Modifier.fillMaxSize()
            ) {

                println("There are ${communityPosts.size} posts")
                items(communityPosts.size) { index ->
                    println("Passing in: ${communityPosts[index].postId}")
                    ProgressItemView(communityPosts[index])
                }
            }
        }
    }
}

@Composable
fun ProgressItemView(post: Post) {

    println("Examining ${post.postId}")

    var postImage by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(post.imageURL) {
        postImage = com.example.lockedin.store.presentation.community_posts.loadImageFromUrl(post.imageURL)
    }


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .padding(horizontal = 8.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(Color(0xFF333333)),
    ) {
        // Image part of the post
        postImage?.let { image ->
            Image(
                bitmap = image,
                contentDescription = "Post Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(225.dp)
            )
        }

        val customColour = Color(0xFF5E5858)
        Box(


            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .background(
                    color = customColour,
                    shape = RoundedCornerShape(
                        topStart = 0.dp,
                        topEnd = 0.dp,
                        bottomStart = 32.dp,
                        bottomEnd = 32.dp,
                    )
                )
        ) {
            Box(
                contentAlignment = Alignment.BottomCenter,
                modifier = Modifier.padding(top = 16.dp, start = 40.dp)
            ) {
                Text(
                    text = convertTimestampToDate(post.timePosted.toString()),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

        }
    }



}


fun convertTimestampToDate(timestampString: String): String {
    val timestamp = timestampString.toLong()
    val date = Date(timestamp)
    val formatter = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
    return formatter.format(date)
}