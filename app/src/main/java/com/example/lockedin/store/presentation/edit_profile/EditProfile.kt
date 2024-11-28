package com.example.lockedin.store.presentation.edit_profile

import android.net.Uri
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.lockedin.models.AuthState
import com.example.lockedin.models.AuthViewModel
import com.example.lockedin.models.CommunityViewModel
import com.example.lockedin.models.UserViewModel
import com.example.lockedin.store.presentation.upload_post.uploadImageToFirebase
import com.example.lockedin.ui.theme.poppinsFontFamily


@Composable
fun EditProfile(
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    communityViewModel: CommunityViewModel,
    userViewModel: UserViewModel
) {


    val username = userViewModel.currentUsername
    val bio = userViewModel.currentUserBio
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    var removeProfilePic = false;


    val authState = authViewModel.authState.observeAsState()
    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Unauthenticated -> navController.navigate("login")
            else -> Unit
        }
    }


    LaunchedEffect(Unit) {
        userViewModel.fetchUsername()
    }

    val launcher  = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) {
            uri: Uri? -> imageUri = uri
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {

        Box(
            modifier = Modifier
                .padding(16.dp)
        ) {

            Text(
                text = "Edit Profile",
                fontSize = 36.sp,
                color = Color.White,
                fontFamily = poppinsFontFamily,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                textAlign = TextAlign.Center
            )
        }

        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,

            ) {
            AsyncImage(
                model = imageUri,
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(120.dp) // Increased size to make the profile image larger
                    .clip(CircleShape)
                    .border(2.dp, Color.Gray, CircleShape),
                contentScale = ContentScale.Crop
            )
        }

        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,

            ) {

            Spacer(Modifier.height(8.dp))

            Button(onClick = {
                launcher.launch("image/*")
            },
                modifier = Modifier.padding(8.dp)
                ) {
                Text(
                    text = "Change Profile Picture",
                    fontSize = 11.sp,

                    )
            }

            Spacer(Modifier.width(8.dp))

            Button(onClick = {
                removeProfilePic = true;
            },
                modifier = Modifier.padding(8.dp)
                ) {
                Text(
                        text = "Remove Profile Picture",
                        fontSize = 11.sp,
                    )
            }

        }

        TextField(
            value = username.value,
            onValueChange = { username.value = it },
            label = { Text("Username") },
            modifier = Modifier
                .height(100.dp)
                .fillMaxWidth()
                .padding(12.dp)
                .clip(RoundedCornerShape(12.dp)) // Rounded corners
                .background(Color(0xFFFFFFFF)), // Background
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color(0xFF333333),
                textColor = Color.White,
                unfocusedIndicatorColor = Color.Transparent,
                unfocusedLabelColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                focusedLabelColor = Color.White,
            ),
            textStyle = TextStyle(
                color = Color.White,
                fontSize = 20.sp,
            )
        )

        Spacer(modifier = Modifier.height(16.dp))


        TextField(
            value = bio.value,
            onValueChange = { bio.value = it },
            label = { Text("Bio") },
            modifier = Modifier
                .height(150.dp)
                .fillMaxWidth()
                .padding(12.dp)
                .clip(RoundedCornerShape(12.dp)) // Rounded corners
                .background(Color(0xFFFFFFFF)), // Background
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color(0xFF333333),
                textColor = Color.White,
                unfocusedIndicatorColor = Color.Transparent,
                unfocusedLabelColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                focusedLabelColor = Color.White,
            ),
            textStyle = TextStyle(
                color = Color.White,
                fontSize = 20.sp,
            )
        )

        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {


            Button(
                onClick = {

                    if (!removeProfilePic &&  imageUri != null) {
                        uploadImageToFirebase(imageUri, context) { pfpUrl ->
                            userViewModel.updateProfilePic(pfpUrl)
                        }
                    } else if (removeProfilePic) {
                        imageUri = null
                        uploadImageToFirebase(imageUri, context) { pfpUrl ->
                            userViewModel.updateProfilePic(pfpUrl)
                        }
                    }

                    if (username.value != "") {
                        userViewModel.updateUsername(username.value)
                    }
                    userViewModel.updateUserBio(bio.value)
                    navController.navigate("profile_screen")
                },
                modifier = Modifier
                    .width(100.dp)
                    .height(50.dp)
                    .clip(RoundedCornerShape(30.dp)),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.White,
                    contentColor = Color(0xFF333333),
                )

            ) {
                Text("Save")
            }
        }

    }

}

