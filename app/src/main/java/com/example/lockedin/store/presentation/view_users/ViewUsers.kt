package com.example.lockedin.store.presentation.view_users

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.content.contentReceiver
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material.*
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import androidx.compose.ui.text.TextStyle
import com.example.lockedin.BottomNavigationBar
import com.example.lockedin.R
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import coil3.compose.AsyncImage
import com.example.lockedin.models.*
import com.example.lockedin.store.presentation.community_feed.CommunityItem
import com.example.lockedin.store.presentation.community_posts.CommunityHeader
import com.example.lockedin.store.presentation.signup_screen.uploadProfileImageToFirebase
import com.example.lockedin.ui.theme.poppinsFontFamily
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.Dispatchers
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.withContext
import java.net.URL


@Composable
fun ViewUsers(
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    communityViewModel: CommunityViewModel,
    userViewModel: UserViewModel,
    communityId : String
) {

    val authState = authViewModel.authState.observeAsState()
    val community = communityViewModel.currentCommunity.value
    val members = communityViewModel.usersForCommunity
    val user = Firebase.auth.currentUser

    LaunchedEffect(authState.value) {
        when(authState.value){
            is AuthState.Unauthenticated -> navController.navigate("login")
            else -> Unit
        }
    }

    LaunchedEffect(Unit) {
        communityViewModel.fetchMembersForCommunity(communityId)
        communityViewModel.fetchCommunityById(communityId)
        if (user != null) {
            userViewModel.updateProfilePictureForCommunities(user.uid, user.photoUrl.toString())
        }
    }

    androidx.compose.material.Scaffold(
        bottomBar = { BottomNavigationBar(navController) },
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(innerPadding)
                .padding(16.dp)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                if (community != null) {

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "View Users",
                        fontSize = 42.sp,
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        textAlign = TextAlign.Left
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "For ${community.name}",
                        fontSize = 24.sp,
                        color = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        textAlign = TextAlign.Left
                    )
                }



                if (members.isEmpty()) {
                    Text(
                        text = "No communities found.",
                        color = Color.White,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(1),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(members.distinctBy { it.userId }) { user ->
                            println("Into the structure User, i am passing in ${user.userId}")
                            UserItemView(
                                UserItem(
                                    name = user.username,
                                    imageRes = user.profilePic,
                                    userId = user.userId
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
}


@Composable
fun UserItemView(item: UserItem, navController: NavController, userViewModel: UserViewModel) {

    val photoUri = Uri.parse(item.imageRes)

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
            model = photoUri,
            contentDescription = null,
            modifier = Modifier
                .size(64.dp) // Circular image
                .clip(CircleShape)
                .border(2.dp, Color.Gray, CircleShape),
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
                androidx.compose.material3.Button(
                    onClick = {
                        navController.navigate("profile_screen/${item.userId}")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF007BFF), // Button color
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp), // Smaller button padding
                    modifier = Modifier
                        .height(30.dp) // Smaller height for the button
                        .clip(RoundedCornerShape(8.dp)) // Rounded edges
                ) {
                    Text("View", fontSize = 12.sp) // Smaller font size
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}


data class UserItem(val name: String, val imageRes: String, val userId: String)
