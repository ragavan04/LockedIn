package com.example.lockedin.store.presentation.owner_settings

import android.app.TimePickerDialog
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.lockedin.R
import com.example.lockedin.models.AuthState
import com.example.lockedin.models.AuthViewModel
import com.example.lockedin.models.CommunityViewModel
import com.example.lockedin.store.presentation.upload_post.uploadImageToFirebase
import java.util.*
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerSettings(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    communityViewModel: CommunityViewModel,
    communityId: String
) {
    val context = LocalContext.current
    val authState by authViewModel.authState.observeAsState()
    val currentCommunity = communityViewModel.currentCommunity.value

    var communityName by remember { mutableStateOf("") }
    var communityDescription by remember { mutableStateOf("") }
    var notificationTime by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val maxDescriptionLength = 100

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    // Fetch the community when the composable loads
    LaunchedEffect(Unit) {
        communityViewModel.fetchCommunityById(communityId)
    }

    // Populate the editable fields when the community is loaded
    LaunchedEffect(currentCommunity) {
        currentCommunity?.let {
            communityName = it.name
            communityDescription = it.description
            notificationTime = it.notificationTime
            imageUri = if (it.communityImage.isNotBlank()) Uri.parse(it.communityImage) else null
        }
    }

    LaunchedEffect(authState) {
        if (authState is AuthState.Unauthenticated) {
            navController.navigate("login")
        }
    }

    Scaffold(
        modifier = Modifier.background(Color(0xFF131313)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF131313))
                .padding(top = 40.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Edit your",
                fontSize = 50.sp,
                fontWeight = FontWeight.Light,
                color = Color.White,
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "community",
                fontSize = 50.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Community Name
            TextField(
                value = communityName,
                onValueChange = { communityName = it },
                label = { Text("Community Name") },
                modifier = Modifier
                    .width(370.dp)
                    .height(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFFFFFF)),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color(0xFF333333),
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Community Description
            TextField(
                value = communityDescription,
                onValueChange = {
                    if (it.length <= maxDescriptionLength) {
                        communityDescription = it
                    }
                },
                label = { Text("Description") },
                modifier = Modifier
                    .width(370.dp)
                    .height(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFFFFFF)),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color(0xFF333333),
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Choose Image
            Button(
                onClick = {
                    launcher.launch("image/*")
                },
                modifier = Modifier
                    .width(200.dp)
                    .height(50.dp)
                    .clip(RoundedCornerShape(30.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF333333),
                )
            ) {
                Text("Choose Image")
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Display Selected Image
            AsyncImage(
                model = imageUri,
                contentDescription = "Community Image",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(2.dp, Color.Gray, CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Notification Time Picker
            Button(
                onClick = {
                    val calendar = Calendar.getInstance()
                    TimePickerDialog(
                        navController.context,
                        { _, hourOfDay, minute ->
                            notificationTime = String.format("%02d:%02d", hourOfDay, minute)
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        true
                    ).show()
                },
                modifier = Modifier
                    .width(200.dp)
                    .height(50.dp)
                    .clip(RoundedCornerShape(30.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF333333),
                )
            ) {
                Text(
                    text = if (notificationTime.isEmpty()) "Set Notification Time" else "Time: $notificationTime",
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Update Button
            Button(
                onClick = {
                    if (communityName.isBlank() || communityDescription.isBlank() || imageUri == null || notificationTime.isBlank()) {
                        Toast.makeText(context, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    // Upload image and update community details
                    uploadImageToFirebase(imageUri, context, { imageUrl ->
                        communityViewModel.updateCommunity(
                            communityId = communityId,
                            name = communityName,
                            description = communityDescription,
                            notificationTime = notificationTime,
                            communityImage = imageUrl
                        )
                        navController.navigateUp()
                    })
                },
                modifier = Modifier
                    .width(200.dp)
                    .height(50.dp)
                    .clip(RoundedCornerShape(30.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF333333),
                )
            ) {
                Text("Update")
            }
        }
    }
}

