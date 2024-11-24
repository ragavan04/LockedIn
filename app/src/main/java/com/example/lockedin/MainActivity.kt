package com.example.lockedin

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import android.Manifest
import android.app.NotificationManager
import androidx.compose.material3.Scaffold
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel

import com.example.lockedin.models.AuthViewModel
import com.example.lockedin.models.CommunityViewModel
import com.example.lockedin.models.UserViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel(this)
        val db = Firebase.firestore


        setContent {

//            val context = LocalContext.current
//            var hasNotificationPermission by remember {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
//                    mutableStateOf(
//                        ContextCompat.checkSelfPermission(
//                            context,
//                            Manifest.permission.POST_NOTIFICATIONS
//                        ) == PackageManager.PERMISSION_GRANTED
//                    )
//                } else mutableStateOf(true)
//            }

            MyApp()
        }
    }


//    private fun showNotificaiton(){
//        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
//        val notification = NotificationCompat.Builder(applicationContext, "channel_id")
//            .setContentText("It's time to post for community blah blah blah!")
//            .setContentTitle("Time to lock in")
//            .setSmallIcon(R.drawable.ic_launcher_foreground)
//            .build()
//        notificationManager.notify(1, notification)
//    }

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


