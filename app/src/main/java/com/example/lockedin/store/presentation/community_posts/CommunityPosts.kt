package com.example.lockedin.store.presentation.community_posts
import androidx.compose.runtime.remember
import com.google.accompanist.permissions.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.format.DateUtils
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.TabRowDefaults.Divider
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import com.example.lockedin.models.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL


@Composable
fun CommunityPosts(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel, communityViewModel: CommunityViewModel, communityId: String) {
    val authState = authViewModel.authState.observeAsState()
    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Unauthenticated -> navController.navigate("login")
            else -> Unit
        }
    }

    // Fetch and observe posts for the specific community
    LaunchedEffect(communityId) {
        communityViewModel.fetchCommunityById(communityId)
        communityViewModel.fetchPostsForCommunity(communityId)
    }

    val community = communityViewModel.currentCommunity.value
    val communityPosts = communityViewModel.postsForCommunity

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF131313))
    ) {
        community?.let {
            CommunityHeader(community, navController, communityId)
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(1),
            modifier = Modifier.padding(top = 16.dp)
        ) {
            items(communityPosts.size) { index ->
                PostItem(post = communityPosts[index], communityId, communityViewModel)
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CommunityHeader(
    community: Community, navController: NavController, communityId: String
) {

    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 25.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Box for back button, title, and action buttons
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
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

            // Row for right-aligned buttons (Progress and Create Post)
            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                verticalAlignment = Alignment.CenterVertically
            ) {

                IconButton(
                    onClick = { navController.navigate("ViewUsers/$communityId") },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.,
                        contentDescription = "Search",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    // Image(
                    //     painter = painterResource(id = R.drawable.view_icon),
                    //     contentDescription = "Progress",
                    //     modifier = Modifier.size(24.dp)
                    // )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Statistics IconButton
                IconButton(
                    onClick = { /* Leave blank for navigation */ },
                    modifier = Modifier.size(36.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_progress),
                        contentDescription = "Progress",
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Create Post IconButton
                IconButton(
                    onClick = {
                        if (cameraPermissionState.status.isGranted) {
                            navController.navigate("UploadPost/$communityId")
                        } else {
                            cameraPermissionState.launchPermissionRequest()
                        }
                              },
                    modifier = Modifier.size(36.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_newpost),
                        contentDescription = "Create Post",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(5.dp))

        // Show feedback if the permission is denied and rationale should be shown
        if (cameraPermissionState.status.shouldShowRationale) {
            Text(
                text = "Camera permission is required to create a post. Please grant the permission.",
                color = Color.Red,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }

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


@Composable
fun PostItem(post: Post, communityId: String, communityViewModel: CommunityViewModel) {
    var postImage by remember { mutableStateOf<ImageBitmap?>(null) }
    var profileImage by remember { mutableStateOf<ImageBitmap?>(null) }
    var voting by remember { mutableStateOf(0) }


    // Load the post image in the background
    LaunchedEffect(post.imageURL) {
        postImage = loadImageFromUrl(post.imageURL)
    }

    // Fetch the latest points from Firestore when the post is displayed
    LaunchedEffect(post.postId) {
        communityViewModel.fetchPostPoints(communityId, post.postId) { updatedPoints ->
            if (updatedPoints != null) {
                voting = updatedPoints
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
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
                    .height(300.dp)
            )
        }

        // Post metadata section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // User profile picture
            Image(
                painter = painterResource(id = R.drawable.temppfp), // Replace with user's profile picture
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
            )
            Spacer(modifier = Modifier.width(8.dp))

            // User info and timestamp
            Column {
                Text(
                    text = post.username, // Display the user's name
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = formatEpochToRelativeTime(post.timePosted),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                IconButton(
                    onClick = {
                        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@IconButton
                        communityViewModel.userCanVote(
                            communityId = communityId,
                            postId = post.postId,
                            userId = userId
                        ) { canVote ->
                            if (canVote) {
                                val newPoints = voting + 1
                                communityViewModel.updatePoints(
                                    communityId = communityId,
                                    postId = post.postId,
                                    newPoints = newPoints,
                                    onSuccess = {
                                        voting = newPoints
                                        communityViewModel.recordVote(
                                            communityId = communityId,
                                            postId = post.postId,
                                            userId = userId,
                                            onSuccess = { println("Vote recorded") },
                                            onFailure = { exception -> println("Error recording vote: ${exception.message}") }
                                        )
                                    },
                                    onFailure = { exception -> println("Error updating points: ${exception.message}") }
                                )
                            } else {
                                println("User has already voted on this post")
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Upvote",
                        tint = Color.White
                    )
                }


                // Display the current vote count
                Text(
                    text = voting.toString(),
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                IconButton(
                    onClick = {
                        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@IconButton
                        communityViewModel.userCanVote(
                            communityId = communityId,
                            postId = post.postId,
                            userId = userId
                        ) { canVote ->
                            if (canVote) {
                                val newPoints = voting - 1
                                communityViewModel.updatePoints(
                                    communityId = communityId,
                                    postId = post.postId,
                                    newPoints = newPoints,
                                    onSuccess = {
                                        voting = newPoints
                                        communityViewModel.recordVote(
                                            communityId = communityId,
                                            postId = post.postId,
                                            userId = userId,
                                            onSuccess = { println("Vote recorded") },
                                            onFailure = { exception -> println("Error recording vote: ${exception.message}") }
                                        )
                                    },
                                    onFailure = { exception -> println("Error updating points: ${exception.message}") }
                                )
                            } else {
                                println("User has already voted on this post")
                            }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Downvote",
                        tint = Color.White
                    )
                }

            }

        }
    }

}

// Helper function to load an image from URL
suspend fun loadImageFromUrl(url: String): ImageBitmap? {
    return withContext(Dispatchers.IO) {
        try {
            val connection = URL(url).openConnection()
            connection.doInput = true
            connection.connect()
            val inputStream = connection.getInputStream()
            val options = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888 // Use higher-quality color config
                inScaled = false // Disable scaling down
            }
            BitmapFactory.decodeStream(inputStream, null, options)?.asImageBitmap()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}


fun formatEpochToRelativeTime(epochMillis: Long): String {
    return DateUtils.getRelativeTimeSpanString(
        epochMillis,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS
    ).toString()
}
