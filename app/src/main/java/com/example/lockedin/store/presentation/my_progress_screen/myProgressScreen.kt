package com.example.lockedin.store.presentation.my_progress_screen

import android.text.format.DateUtils
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lockedin.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.ImeAction
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lockedin.BottomNavigationBar
import com.example.lockedin.models.*
import com.example.lockedin.store.presentation.progress_screen.ProgressItem
import com.example.lockedin.store.presentation.progress_screen.ProgressItemView
import com.example.lockedin.models.CommunityViewModel
import com.example.lockedin.store.presentation.community_feed.CommunityItem
import com.example.lockedin.store.presentation.community_feed.CommunityItemView
import com.example.lockedin.models.UserViewModel
import com.example.lockedin.ui.theme.poppinsFontFamily

@Composable
fun MyProgressScreen(modifier: Modifier = Modifier, navController: NavController, authViewModel: AuthViewModel, userViewModel: UserViewModel, communityViewModel: CommunityViewModel = viewModel()) {

    val CommunityItems = remember{ mutableListOf<CommunityItem>() }

    val authState = authViewModel.authState.observeAsState()
    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Unauthenticated -> navController.navigate("login")
            else -> Unit
        }
    }

    LaunchedEffect(Unit) {
        communityViewModel.fetchCommunities()
    }

    androidx.compose.material.Scaffold(
        bottomBar = { BottomNavigationBar(navController) },
    ) {



        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF131313))
                .padding(16.dp)
        ) {
            // Title
            androidx.compose.material.Text(
                text = "PROGRESS",
                fontSize = 24.sp,
                color = Color.White,
                fontFamily = poppinsFontFamily,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                textAlign = TextAlign.Center
            )

            // Divider
            androidx.compose.material.Divider(
                color = Color.White,
                thickness = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )


            LazyVerticalGrid(
                columns = GridCells.Fixed(1),
                modifier = Modifier.fillMaxSize()
            ) {
                items(CommunityItems.size) { index ->
                    CommunityItemView(CommunityItems[index], navController, userViewModel)
                }
            }

        }
    }
}