package com.example.lockedin.store.presentation.progress_screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import com.example.lockedin.store.presentation.util.components.LoadingDialog
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.lockedin.R

@Composable
fun ProgressScreen(
    state: ProgressViewState = ProgressViewState()
) {
    Box(
        modifier = Modifier.size(200.dp),
    ){
        Image(
            painter = painterResource(id = R.drawable.sampledumbbell),
            contentDescription = "Progress Image",
            modifier = Modifier.fillMaxSize(),
        )
    }
}