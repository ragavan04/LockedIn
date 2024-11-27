package com.example.lockedin.store.presentation.login_screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lockedin.components.AlertDialogCustom
import com.example.lockedin.models.AuthState
import com.example.lockedin.models.AuthViewModel
import com.google.firebase.annotations.concurrent.Background


val LightBlue = Color(0xFF007BFF)
val BackgroundColor = Color.Black

@Composable
fun LoginScreen(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    val openAlertDialog = remember { mutableStateOf(false) }

    val authState = authViewModel.authState.observeAsState()
    val context = LocalContext.current

    LaunchedEffect(authState.value) {
        when(authState.value){
            is AuthState.Authenticated -> navController.navigate("dashboard")
            is AuthState.Error -> Toast.makeText(context, (authState.value as AuthState.Error).message, Toast.LENGTH_SHORT).show()
            else -> Unit
        }
    }

    Column (
        modifier = modifier.fillMaxSize().background(BackgroundColor),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Sign In", fontSize = 32.sp, color = Color.White)

        Spacer(modifier = Modifier.height(16.dp))

        TextField(value = email, onValueChange = {email = it}, label = {Text("Email")},
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
                focusedLabelColor = Color.White))

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = password,
            onValueChange = {password = it},
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            label = {Text("Password")},
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

        Spacer(modifier = Modifier.height(16.dp))

        if (!loading){
            Button(onClick = {
                loading = true
                authViewModel.login(email, password, {success ->
                    loading = false
                    if (!success) openAlertDialog.value = true
                })

            },
                colors = ButtonDefaults.buttonColors(backgroundColor = LightBlue, contentColor = Color.White),
                enabled = !loading

            ){
                Text("Login")
            }
        } else {
            CircularProgressIndicator(
                modifier = Modifier.width(45.dp),
                color = LightBlue,
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
                    dialogText = "Either your password or email is incorrect, please try again",
                )
            }

        }




        Spacer(modifier = Modifier.height(8.dp))

        TextButton(onClick = {
            navController.navigate("signup")
        }){
            Text(text = "Don't have an account? Sign Up!", color = Color.White)
        }
    }

}