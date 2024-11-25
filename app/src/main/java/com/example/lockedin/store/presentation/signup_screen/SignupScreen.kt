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
import com.example.lockedin.models.AuthState
import com.example.lockedin.models.AuthViewModel
import com.example.lockedin.models.UserViewModel
import com.example.lockedin.store.presentation.upload_post.saveImageUrlToCommunityPosts
import com.example.lockedin.store.presentation.upload_post.uploadImageToFirebaseStorage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.util.*



val Purple500 = Color(0xFF6200EE)
val BackgroundColor = Color.Black

@Composable
fun SignupScreen(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel, userViewModel: UserViewModel) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

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
                navController.navigate("dashboard")
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
        }, colors = ButtonDefaults.buttonColors(backgroundColor = Purple500)) {

            Text("Choose Image")
        }


        AsyncImage(
            model = imageUri,
            contentDescription = "Profile Image",
            modifier = Modifier
                .size(120.dp) // Increased size to make the profile image larger
                .clip(CircleShape)
                .border(2.dp, Color.Gray, CircleShape),
            contentScale = ContentScale.Crop
        )


        Button(onClick = {
             uploadProfileImageToFirebase(imageUri, context, {pfpUrl ->

                 authViewModel.signup(email, password, username, pfpUrl, userViewModel)
             })
//            if (authState.value is AuthState.Authenticated) {
//                navController.navigate("community_feed")
//            }




        }, colors = ButtonDefaults.buttonColors(backgroundColor = Purple500)) {
            Text("Register")
        }


        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = {
            navController.navigate("login")

        }) {
            Text(text = "Already have an account? Sign in!", color = Color.White)
        }
    }

}


fun uploadProfileImageToFirebase(uri: Uri?, context: Context, onResult: (String) -> Unit){
    val storage = Firebase.storage
    val storageRef = storage.reference
    val uniqueID = UUID.randomUUID().toString()
    val imagesRef = storageRef.child("images/$uniqueID.jpg")

    var returnUrl: String = "nothing"

    val byteArray: ByteArray? = uri?.let { context.contentResolver.openInputStream(it)?.use{it.readBytes()} }

    if (byteArray != null) {
        imagesRef.putBytes(byteArray).addOnSuccessListener {
            imagesRef.downloadUrl.addOnSuccessListener { uri ->
                returnUrl  = uri.toString()
                onResult(returnUrl)
                Log.d("THIS IS WHAT ITS SUPPOSED TO BE", returnUrl)
            }
        }.addOnFailureListener({
            Log.d("ERROR FROM SIGN UP",  "IMAGE COULD NOT BE UPLOADED")
        })
    } else {
        onResult("error")
    }


}
