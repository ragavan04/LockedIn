package com.example.lockedin

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color.Companion.DarkGray
import androidx.compose.ui.graphics.Color.Companion.LightGray
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import coil3.compose.AsyncImage
//import coil3.compose.AsyncImage
import com.example.lockedin.models.AuthState
import com.example.lockedin.models.AuthViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

// Define the colors used in the UI
val Purple500 = Color(0xFF6200EE)
val BackgroundColor = Color.Black

@Composable
fun ProfileScreen(modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier, navController: NavController, authViewModel: AuthViewModel) {
    val authState = authViewModel.authState.observeAsState()
    val user = Firebase.auth.currentUser



    LaunchedEffect(authState.value) {
        when(authState.value){
            is AuthState.Unauthenticated -> navController.navigate("login")
            else -> Unit
        }
    }
    user?.let {
        val username = it.displayName
        val email = it.email
        val photoUrl = it.photoUrl
        val uid = it.uid

        if (username != null) {
            Log.d("FROM PROFILE SCREEN", username)
        }


        Scaffold(

            bottomBar = { BottomNavigationBar(navController) }
        ) {


            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundColor)
                    .padding(top = 40.dp), // Added more padding to move content downwards
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
//              Profile Header with Image
                if (username != null) {
                    ProfileHeader(
                        name = username,
                        image = photoUrl // Replace with your image resource
                    )
                }
                Spacer(modifier = Modifier.height(24.dp)) // Added more space after profile image

                // Edit Profile Button
                EditProfileButton()

                Spacer(modifier = Modifier.height(24.dp)) // Increased spacing for better alignment

                // Bio Section
                UserBio(
                    bio = "Here to accomplish my fitness goals.\nIf anyone goes to GoodLife, send me a message!"
                )

                Button(onClick = { authViewModel.signout() }) { Text("Sign out") }

                Spacer(modifier = Modifier.height(32.dp)) // Increased spacing to match design

                // Communities and Points Section with a larger rounded box
                StatsSection(communities = 4, points = 219)
            }
        }
    }
}

@Composable
fun ProfileHeader(name: String, image: Uri?) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Circular profile picture
        AsyncImage(
            model = image,
            contentDescription = "Profile Image",
            modifier = Modifier
                .size(120.dp) // Increased size to make the profile image larger
                .clip(CircleShape)
                .border(2.dp, Color.Gray, CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(12.dp)) // Increased space between image and name

        // Name (split across two lines)
        Text(text = name, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
//        Text(text = "Simpson", fontSize = 20.sp, fontWeight = FontWeight.Normal, color = Color.White)
    }
}

@Composable
fun EditProfileButton() {
    Button(
        onClick = { /* Handle edit profile click */ },
        colors = ButtonDefaults.buttonColors(DarkGray),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .height(40.dp)
            .width(180.dp)
    ) {
        Text(text = "Edit Profile", color = Color.White, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun UserBio(bio: String) {
    Text(
        text = bio,
        fontSize = 16.sp,
        color = Color.White,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 32.dp)
    )
}

@Composable
fun StatsSection(communities: Int, points: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
//            .background(Color(0xFFF1F1F1), shape = RoundedCornerShape(24.dp)) // Made box larger and more rounded
            .background(Color.DarkGray, shape = RoundedCornerShape(24.dp)) // Made box larger and more rounded
            .padding(24.dp), // Increased padding for a bigger box
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Communities Text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Communities", fontSize = 16.sp, fontWeight = FontWeight.Normal, color = Color.White)
            Text(text = "$communities", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        // Divider between Communities and Points
        Box(
            modifier = Modifier
                .height(40.dp)
                .width(1.dp)
                .background(Color.Gray)
        )

        // Points Text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Total Points", fontSize = 16.sp, fontWeight = FontWeight.Normal, color = Color.White)
            Text(text = "$points", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun ProfileScreenPreview() {
//    ProfileScreen()
//}
