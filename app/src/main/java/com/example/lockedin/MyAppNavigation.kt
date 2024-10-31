package com.example.lockedin

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.lockedin.models.AuthViewModel
import com.example.lockedin.models.CommunityViewModel
import com.example.lockedin.store.presentation.community_feed.CommunityFeed
import com.example.lockedin.store.presentation.community_posts.CommunityPosts
import com.example.lockedin.store.presentation.create_community_screen.CreateCommunityScreen
import com.example.lockedin.store.presentation.login_screen.LoginScreen
import com.example.lockedin.store.presentation.progress_screen.ProgressScreen
import com.example.lockedin.store.presentation.signup_screen.SignupScreen


@Composable
fun MyAppNavigation(modifier: Modifier = Modifier, authViewModel: AuthViewModel, communityViewModel: CommunityViewModel) {
    val navController = rememberNavController()
    Scaffold(
    ) {
        NavHost(navController, startDestination = "login", builder = {
            composable("login") {
                LoginScreen(modifier, navController, authViewModel)
            }
            composable("signup") {
                SignupScreen(modifier, navController, authViewModel)
            }
            composable("community_screen") {
                CommunityFeed(modifier, navController, authViewModel)
            }
            composable("progress_screen"){
                ProgressScreen(modifier, navController, authViewModel)
            }
            composable("profile_screen"){
                ProfileScreen(modifier, navController, authViewModel)
            }
            composable("CreateCommunityScreen"){
                CreateCommunityScreen(modifier, navController, authViewModel, communityViewModel)
            }
            composable("CommunityPosts/{communityId}"){ backStackEntry ->
                val communityId = backStackEntry.arguments?.getString("communityId")
                if (communityId != null) {
                    CommunityPosts(modifier, navController, authViewModel, communityViewModel, communityId = communityId)
                }
            }

        })
    }

}