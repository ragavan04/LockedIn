package com.example.lockedin.store.presentation.view_banned_users

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
fun ViewBannedUsers(
    modifier: Modifier = Modifier,
    navController: NavController,
    communityViewModel: CommunityViewModel,
    communityId: String
) {
    
    var refreshTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        communityViewModel.fetchBannedUsersForCommunity(communityId)
    }

    LaunchedEffect(refreshTrigger) {
        communityViewModel.fetchBannedUsersForCommunity(communityId)
    }

    val bannedUsers by communityViewModel.bannedUsersForCommunity.observeAsState(emptyList())

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "Banned Users",
                fontSize = 42.sp,
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                textAlign = TextAlign.Left
            )

            if (bannedUsers.isEmpty()) {
                Text(
                    text = "No banned users found.",
                    color = Color.White,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(1),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(bannedUsers) { user ->
                        BannedUserItemView(
                            user = user,
                            navController = navController,
                            communityViewModel = communityViewModel,
                            communityId = communityId,
                            refreshTrigger,
                            onRefresh = { 
                                refreshTrigger++ 
                                println("Refresh trigger: ${refreshTrigger}")   
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BannedUserItemView(
    user: UserItem,
    navController: NavController,
    communityViewModel: CommunityViewModel,
    communityId: String,
    refreshTrigger: Int,
    onRefresh: () -> Unit
) {
    Row(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1E1E1E))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = Uri.parse(user.imageRes),
            contentDescription = null,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .border(2.dp, Color.Gray, CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = user.name,
                fontSize = 18.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            androidx.compose.material3.Button(
                onClick = {
                    navController.navigate("profile_screen/${user.userId}")
                },
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF007BFF),
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier
                    .height(30.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                Text("View", fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.width(8.dp))

            androidx.compose.material3.Button(
                onClick = {
                    communityViewModel.unbanUserFromCommunity(communityId, user.userId, {
                        onRefresh()
                        println("User unbanned successfully.")
                    }, { e ->
                        println("Error: ${e.message}")
                    })
                },
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = Color.Red,
                    contentColor = Color.White
                ),
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text("Unban")
            }
        }
    }
}