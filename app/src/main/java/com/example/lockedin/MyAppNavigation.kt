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
import com.example.lockedin.models.UserViewModel
import com.example.lockedin.store.presentation.community_feed.CommunityFeed
import com.example.lockedin.store.presentation.community_posts.CommunityPosts
import com.example.lockedin.store.presentation.dashboard.DashboardScreen
import com.example.lockedin.store.presentation.my_progress_screen.MyProgressScreen
import com.example.lockedin.store.presentation.create_community_screen.CreateCommunityScreen
import com.example.lockedin.store.presentation.login_screen.LoginScreen
//import com.example.lockedin.store.presentation.progress_screen.ProgressScreen
import com.example.lockedin.store.presentation.signup_screen.SignupScreen
import com.example.lockedin.store.presentation.upload_post.UploadPost
import com.example.lockedin.store.presentation.view_community.viewCommunity

//import com.example.lockedin.store.presentation.search_screen.SearchScreen


@Composable
fun MyAppNavigation(modifier: Modifier = Modifier, authViewModel: AuthViewModel, communityViewModel: CommunityViewModel, userViewModel: UserViewModel) {
    val navController = rememberNavController()
    Scaffold(
    ) {
        NavHost(navController, startDestination = "dashboard", builder = {
            composable("login") {
                LoginScreen(modifier, navController, authViewModel)
            }
            composable("signup") {
                SignupScreen(modifier, navController, authViewModel, userViewModel)
            }
            composable("dashboard") {
                DashboardScreen(modifier, navController, userViewModel)
            }
            composable("community_screen") {
                CommunityFeed(modifier, navController, authViewModel, userViewModel)
            }
            composable("my_progress_screen"){
                MyProgressScreen(modifier, navController, authViewModel, userViewModel)
            }
            composable("profile_screen"){
                ProfileScreen(modifier, navController, authViewModel)
            }
//            composable("search_screen") {
//                SearchScreen(modifier, navController, authViewModel)
//}
            composable("CreateCommunityScreen"){
                CreateCommunityScreen(modifier, navController, authViewModel, communityViewModel, userViewModel)
            }
            composable("CommunityPosts/{communityId}"){ backStackEntry ->
                val communityId = backStackEntry.arguments?.getString("communityId")
                if (communityId != null) {
                    CommunityPosts(modifier, navController, authViewModel, communityViewModel, communityId = communityId)
                }
            }
            composable("UploadPost/{communityId}"){ backStackEntry ->
                val communityId = backStackEntry.arguments?.getString("communityId")
                if (communityId != null) {
                    UploadPost(modifier, navController, authViewModel, userViewModel, communityId = communityId)
                }
            }
            composable("viewCommunity/{communityId}"){ backStackEntry ->
                val communityId = backStackEntry.arguments?.getString("communityId")
                if (communityId != null) {
                    viewCommunity(modifier, navController, authViewModel, communityViewModel, communityId = communityId)
                }
            }
        })
    }

}