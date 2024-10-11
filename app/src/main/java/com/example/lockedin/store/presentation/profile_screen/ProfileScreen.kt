package com.example.lockedin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color.Companion.Gray
import androidx.compose.ui.graphics.Color.Companion.LightGray
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview

val PrimaryColor = Color(0xFF6200EE) // Purple
val SecondaryColor = Color(0xFF03DAC5) // Teal
val BackgroundColor = Color(0xFFF5F5F5) // Light grey background
val BoxColor = Color(0xFFE0E0E0) // Light grey for stats box
val TextColor = Color(0xFF333333) // Dark text color for contrast

@Composable
fun ProfileScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(top = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Picture and Name Section
        ProfileHeader(name = "Bart Simpson")
        Spacer(modifier = Modifier.height(16.dp))

        // Edit Profile Button
        EditProfileButton()

        Spacer(modifier = Modifier.height(16.dp))

        // Bio Section
        UserBio(
            bio = "Here to accomplish my fitness goals.\nIf anyone goes to GoodLife, send me a message!"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Communities and Points Section within a rounded box
        StatsSection(communities = 4, points = 219)
    }
}

@Composable
fun ProfileHeader(name: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Circular profile picture placeholder
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .border(2.dp, Color.Gray, CircleShape)
                .background(LightGray),
            contentAlignment = Alignment.Center
        ) {
            // Replace with actual image if available
            Text(text = "Image", color = White)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Name (split across two lines)
        Text(text = "Bart", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextColor)
        Text(text = "Simpson", fontSize = 24.sp, fontWeight = FontWeight.Light, color = TextColor)
    }
}

@Composable
fun EditProfileButton() {
    Button(
        onClick = { /* Handle edit profile click */ },
//        colors = ButtonDefaults.buttonColors(backgroundColor = BoxColor),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .height(40.dp)
            .width(150.dp)
    ) {
        Text(text = "Edit Profile", color = TextColor)
    }
}

@Composable
fun UserBio(bio: String) {
    Text(
        text = bio,
        fontSize = 16.sp,
        color = TextColor,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@Composable
fun StatsSection(communities: Int, points: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .background(BoxColor, shape = RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Communities Text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Communities", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextColor)
            Text(text = "$communities", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextColor)
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
            Text(text = "Total Points", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextColor)
            Text(text = "$points", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextColor)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreen()
}
