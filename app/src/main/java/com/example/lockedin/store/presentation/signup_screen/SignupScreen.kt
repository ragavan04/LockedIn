package com.example.lockedin.store.presentation.signup_screen

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
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
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.example.lockedin.components.AlertDialogCustom
import com.example.lockedin.models.AuthState
import com.example.lockedin.models.AuthViewModel
import com.example.lockedin.models.UserViewModel
import com.example.lockedin.store.presentation.upload_post.saveImageUrlToCommunityPosts
import com.example.lockedin.store.presentation.upload_post.uploadImageToFirebase
import com.example.lockedin.store.presentation.upload_post.uploadImageToFirebaseStorage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.util.*



val LightBlue = Color(0xFF007BFF)
val LightPurple = Color(0xFF8A2BE2)
val BackgroundColor = Color.Black

@Composable
fun SignupScreen(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel, userViewModel: UserViewModel) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    var dialogMessage by remember { mutableStateOf("") }

    val openAlertDialog = remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    val launcher  = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) {
        uri: Uri? -> imageUri = uri
    }

    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current

    LaunchedEffect(authState.value) {
        when(authState.value){
            is AuthState.Authenticated -> {
                userViewModel.createUser(username)
                navController.navigate("welcome")
            }
            is AuthState.Error -> Toast.makeText(context, (authState.value as AuthState.Error).message, Toast.LENGTH_SHORT).show()
            else -> Unit
        }
    }

    Column (
        modifier = modifier.fillMaxSize().background(BackgroundColor),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Sign Up", fontSize = 32.sp, color = Color.White)

        Spacer(modifier = Modifier.height(16.dp))

        TextField(value = email, onValueChange = {email = it}, label = { Text("Email") },
            modifier = Modifier
                .width(260.dp)
                .height(60.dp)
                .clip(RoundedCornerShape(12.dp)) // Rounded corners
                .background(Color(0xFFFFFFFF)), // Darker TextField background
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color(0xFF333333),
                textColor = Color.White,
                unfocusedIndicatorColor = Color.Transparent,
                unfocusedLabelColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                focusedLabelColor = Color.White,
            ))

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = password,
            onValueChange = {password = it},
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            label = { Text("Password") },
            modifier = Modifier
                .width(260.dp)
                .height(60.dp)
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

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = username,
            onValueChange = {username = it},
            label = { Text("Username") },
            modifier = Modifier
                .width(260.dp)
                .height(60.dp)
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

        Spacer(modifier = Modifier.height(8.dp))



        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            launcher.launch("image/*")
        }, colors = ButtonDefaults.buttonColors(backgroundColor = LightBlue, contentColor = Color.White)) {

            Text("Choose Image")
        }

        Spacer(modifier = Modifier.height(16.dp))

        AsyncImage(
            model = imageUri,
            contentDescription = "Profile Image",
            modifier = Modifier
                .size(120.dp) // Increased size to make the profile image larger
                .clip(CircleShape)
                .border(2.dp, Color.Gray, CircleShape),
            contentScale = ContentScale.Crop
        )


        Spacer(modifier = Modifier.height(16.dp))

        if (!loading){

            Button(
                onClick = {
                    loading = true
                    if (email == "" || password == "" || username == "" || imageUri == null) {
                        openAlertDialog.value =  true
                        dialogMessage = "Please make sure all the fields are completed and none are blank."
                    } else {
                        openAlertDialog.value = false
                        uploadImageToFirebase(imageUri, context, {pfpUrl ->

                            authViewModel.signup(email, password, username, pfpUrl, userViewModel, {success ->
                                if (!success) {
                                    openAlertDialog.value = true
                                    dialogMessage = "Oops! Something went wrong, please try again later."
                                }
                            })
                        })
                    }


            },
                colors = ButtonDefaults.buttonColors(backgroundColor = LightBlue, contentColor = Color.White),
                enabled = !loading
                ) {
                Text("Register")
            }
        } else {
            CircularProgressIndicator(
                modifier = Modifier.width(45.dp),
                color = com.example.lockedin.store.presentation.login_screen.LightBlue,
            )
        }

        when {
            openAlertDialog.value -> {
                AlertDialogCustom(
                    onDismissRequest = {
                        openAlertDialog.value = false
                        loading = false
                       },
                    onConfirmation = {
                        openAlertDialog.value = false
                        loading = false
                    },
                    dialogTitle = "Oops! Something Went Wrong",
                    dialogText = dialogMessage,
                )
            }

        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = {
            navController.navigate("login")

        }) {
            Text(text = "Already have an account? Sign in!", color = Color.White)
        }
    }

}



