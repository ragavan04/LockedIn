package com.example.lockedin.store.presentation.create_community_screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.material3.Button
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import com.example.lockedin.BottomNavigationBar
import com.example.lockedin.R
import com.example.lockedin.models.AuthState
import com.example.lockedin.models.AuthViewModel
import com.example.lockedin.models.CommunityViewModel
import com.example.lockedin.models.UserViewModel


@Composable
fun CreateCommunityScreen(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel, communityViewModel: CommunityViewModel, userViewModel: UserViewModel) {

    var communityName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var communityPicture by remember { mutableStateOf("") }

    val authState = authViewModel.authState.observeAsState()
    LaunchedEffect(authState.value) {
        when(authState.value){
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
                .background(Color(0xFF131313))
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
                    onValueChange = { description = it },
                    label = { Text("Description") } ,
                    modifier = Modifier
                        .width(370.dp)
                        .height(180.dp)
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
                    value = communityPicture,
                    onValueChange = { communityPicture = it },
                    label = { Text("Community Picture URL") },
                    modifier = Modifier
                        .width(370.dp)
                        .height(180.dp)
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

                Spacer(modifier = Modifier.height(40.dp))

                Button(onClick = {
                    communityViewModel.createCommunity(communityName, description, communityPicture, userViewModel)
                    navController.navigate("community_screen")

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
                    Text("Create")
                }

            }
        }

    }

}