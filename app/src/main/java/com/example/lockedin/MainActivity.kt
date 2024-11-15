package com.example.lockedin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Scaffold
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.lockedin.models.AuthViewModel
import com.example.lockedin.models.CommunityViewModel
import com.example.lockedin.models.UserViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = Firebase.firestore

        setContent {
            MyApp()
        }
    }
}

@Composable
fun MyApp() {
    val authViewModel: AuthViewModel = viewModel()
    val communityViewModel: CommunityViewModel = viewModel()
    val userViewModel: UserViewModel = viewModel()
    Scaffold (modifier = Modifier.fillMaxSize()) {
        innerPadding -> MyAppNavigation(authViewModel = authViewModel, modifier = Modifier.padding(innerPadding), communityViewModel = communityViewModel, userViewModel = userViewModel)
    }
}


