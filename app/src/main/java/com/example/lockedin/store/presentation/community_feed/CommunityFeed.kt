package com.example.lockedin.store.presentation.community_feed

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.lockedin.MyApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import com.example.lockedin.R
import com.example.lockedin.store.presentation.progress_screen.ProgressItem
import com.example.lockedin.store.presentation.progress_screen.ProgressItemView
import com.example.lockedin.store.presentation.util.components.LoadingDialog
import java.lang.reflect.Modifier


@Composable
fun CommunityFeed() {

    Row(
        modifier = androidx.compose.ui.Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Text(
            text = "<",
            fontSize = 42.sp,
            color = Color.Black,
            modifier = androidx.compose.ui.Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            textAlign = TextAlign.Left
        )


    }

    //Spacer(modifier = androidx.compose.ui.Modifier.height(40.dp))

    Column(
        modifier = androidx.compose.ui.Modifier
            .fillMaxSize()
            .padding(28.dp)
    ) {
        // Title
        Text(
            text = "\nFind Your",

            fontSize = 42.sp,
            color = Color.Black,
            modifier = androidx.compose.ui.Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            textAlign = TextAlign.Left
        )

        // Title
        Text(
            text = "Community",
            fontSize = 54.sp,
            color = Color.Black,
            modifier = androidx.compose.ui.Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            textAlign = TextAlign.Left
        )


        Row(

            modifier = androidx.compose.ui.Modifier
                .fillMaxWidth()

        ) {

            Button(
                onClick = {
                    // Handle button click action here
                },
                modifier = androidx.compose.ui.Modifier
                    .padding(8.dp),

                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.LightGray.copy(alpha = 0.5f),  // Customize the button background
                    contentColor = Color.White    // Customize the text color
                )
            ) {
                Text(
                    text = "Join Community"
                )  // The button label
            }

            Button(
                onClick = {
                    // Handle button click action here
                },
                modifier = androidx.compose.ui.Modifier
                    .padding(8.dp),

                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,  // Customize the button background
                    contentColor = Color.White    // Customize the text color
                )
            ) {
                Text(
                    text = "Your Communities"
                )  // The button label
            }

        }


        Spacer(modifier = androidx.compose.ui.Modifier.height(40.dp))

        val progressItems = listOf(
            ProgressItem("Swimming", R.drawable.samplecommunity1),
            ProgressItem("Tennis", R.drawable.samplecommunity2),
            ProgressItem("Working Out", R.drawable.samplecommunity3)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(1),
            modifier = androidx.compose.ui.Modifier.fillMaxSize()
        ) {
            items(progressItems.size) { index ->
                ProgressItemView(progressItems[index])
            }
        }
    }
}

@Composable
fun ProgressItemView(item: ProgressItem) {
    Column(
        modifier = androidx.compose.ui.Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.LightGray.copy(alpha = 0.5f)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(

            painter = painterResource(item.imageRes),
            contentDescription = null,

            modifier = androidx.compose.ui.Modifier
                .fillMaxWidth()
                .aspectRatio(2f)
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Fit
        )

        Spacer(modifier = androidx.compose.ui.Modifier.height(8.dp))

        Text(
            text = item.date,
            fontSize = 32.sp,
            color = Color.Black,
            textAlign = TextAlign.Center
        )
    }
}


data class ProgressItem(val date: String, val imageRes: Int)

