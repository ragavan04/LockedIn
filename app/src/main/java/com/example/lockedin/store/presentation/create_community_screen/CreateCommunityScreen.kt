package com.example.lockedin.store.presentation.create_community_screen

import android.app.TimePickerDialog
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.lockedin.BottomNavigationBar
import com.example.lockedin.models.AuthState
import com.example.lockedin.models.AuthViewModel
import com.example.lockedin.models.CommunityViewModel
import com.example.lockedin.models.UserViewModel
import com.example.lockedin.store.presentation.upload_post.uploadImageToFirebase
import java.util.*


@Composable
fun CreateCommunityScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    communityViewModel: CommunityViewModel,
    userViewModel: UserViewModel
) {

    val context = LocalContext.current
    var communityName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var notificationTime by remember { mutableStateOf("") }
    val maxDescriptionLength = 100

    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }


    val authState = authViewModel.authState.observeAsState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Unauthenticated -> navController.navigate("login")
            else -> Unit
        }

    }
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(top = 40.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Create your",
                fontSize = 50.sp,
                fontWeight = FontWeight.Light,
                color = Color.White,
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "community",
                    fontSize = 50.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )

                Spacer(modifier = Modifier.height(20.dp))

                TextField(
                    value = communityName,
                    onValueChange = { communityName = it },
                    label = { Text("Community Name") },
                    modifier = Modifier
                        .width(370.dp)
                        .height(80.dp)
                        .clip(RoundedCornerShape(12.dp)) // Rounded corners
                        .background(Color(0xFFFFFFFF)), // Darker TextField background
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color(0xFF333333),
                        textColor = Color.White,
                        unfocusedIndicatorColor = Color.Transparent,
                        unfocusedLabelColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        focusedLabelColor = Color.White,
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                TextField(
                    value = description,
                    onValueChange = {
                        if (it.length <= maxDescriptionLength) {
                            description = it
                        }
                    },
                    label = { Text("Description") },
                    modifier = Modifier
                        .width(370.dp)
                        .height(80.dp)
                        .clip(RoundedCornerShape(12.dp)) // Rounded corners
                        .background(Color(0xFFFFFFFF)), // Darker TextField background
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color(0xFF333333),
                        textColor = Color.White,
                        unfocusedIndicatorColor = Color.Transparent,
                        unfocusedLabelColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        focusedLabelColor = Color.White,
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        launcher.launch("image/*")
                    },
                    modifier = Modifier
                        .width(200.dp)
                        .height(50.dp)
                        .clip(RoundedCornerShape(30.dp)),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color.White,
                        contentColor = Color(0xFF333333),
                    )
                ) {

                    Text("Choose Image")
                }

                Spacer(modifier = Modifier.height(20.dp))

                AsyncImage(
                    model = imageUri,
                    contentDescription = "Profile Image",
                    modifier = Modifier
                        .size(120.dp) // Increased size to make the profile image larger
                        .clip(CircleShape)
                        .border(2.dp, Color.Gray, CircleShape),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Time Picker Section
                Button(
                    onClick = {
                        val calendar = Calendar.getInstance()
                        TimePickerDialog(
                            navController.context,
                            { _, hourOfDay, minute ->
                                notificationTime = String.format("%02d:%02d", hourOfDay, minute)
                                Toast.makeText(
                                    navController.context,
                                    "Notification Time Set: $notificationTime",
                                    Toast.LENGTH_SHORT
                                ).show()
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
                        backgroundColor = Color.White,
                        contentColor = Color(0xFF333333),
                    )
                ) {
                    Text(
                        text = if (notificationTime.isEmpty()) "Set Notification Time" else "Time: $notificationTime",
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))


                Button(
                    onClick = {
                        // Validation checks
                        if (communityName.isBlank()) {
                            Toast.makeText(context, "Please enter a community name.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (description.isBlank()) {
                            Toast.makeText(context, "Please enter a description.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (imageUri == null) {
                            Toast.makeText(context, "Please choose an image.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (notificationTime.isBlank()) {
                            Toast.makeText(context, "Please set a notification time.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        // If all fields are complete, proceed with community creation
                        uploadImageToFirebase(imageUri, context, { communityPicture ->
                            communityViewModel.createCommunity(
                                communityName,
                                description,
                                communityPicture,
                                notificationTime,
                                userViewModel,
                                context
                            )
                            navController.navigate("community_screen")
                        })
                    },
                    modifier = Modifier
                        .width(100.dp)
                        .height(50.dp)
                        .clip(RoundedCornerShape(30.dp)),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFF007BFF),
                        contentColor = Color.White,
                    )
                ) {
                    Text("Create")
                }


            }
        }

    }

}

