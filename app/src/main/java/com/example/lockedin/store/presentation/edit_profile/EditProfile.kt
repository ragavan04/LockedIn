package com.example.lockedin.store.presentation.edit_profile

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
import com.example.lockedin.store.presentation.signup_screen.uploadProfileImageToFirebase
import com.example.lockedin.ui.theme.poppinsFontFamily
import kotlinx.coroutines.Dispatchers
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.withContext
import java.net.URL


@Composable
fun EditProfile(
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    communityViewModel: CommunityViewModel,
    userViewModel: UserViewModel
) {


    val username = userViewModel.currentUsername
    var description = ""
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
            value = description,
            onValueChange = { description = it },
            label = { Text("Username") },
            modifier = Modifier
                .height(200.dp)
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
                    userViewModel.updateUsername(username.value)
                    if (!removeProfilePic &&  imageUri != null) {
                        uploadProfileImageToFirebase(imageUri, context) { pfpUrl ->
                            userViewModel.updateProfilePicture(pfpUrl)
                        }
                    }

                    if(removeProfilePic) {
                        imageUri = null
                        uploadProfileImageToFirebase(imageUri, context) { pfpUrl ->
                            userViewModel.updateProfilePicture(pfpUrl)
                        }
                    }

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

