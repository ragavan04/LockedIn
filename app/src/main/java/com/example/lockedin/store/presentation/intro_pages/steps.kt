import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.lockedin.R
import com.example.lockedin.models.AuthViewModel
import com.example.lockedin.models.UserViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState

@OptIn(ExperimentalPagerApi::class)
@Composable
fun Steps(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel,
) {
    val pagerState = rememberPagerState()

    // Data for steps
    val steps = listOf(
        StepData(
            "Step 1: Sign up and explore communities.",
            R.drawable.explore // Replace with your drawable resource
        ),
        StepData(
            "Step 2: Post daily updates to stay accountable.",
            R.drawable.dailyupdates // Replace with your drawable resource
        ),
        StepData(
            "Step 3: Engage with other users, vote, and climb the leaderboard!",
            R.drawable.climb // Replace with your drawable resource
        )
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        backgroundColor = Color.Black // Set background color
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            HorizontalPager(
                count = steps.size,
                state = pagerState,
                modifier = Modifier.weight(1f) // Take up the vertical space
            ) { page ->
                val step = steps[page]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Step Title
                    Text(
                        text = step.description.split(":")[0], // Extracts "Step 1", "Step 2", etc.
                        fontSize = 45.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 16.dp)
                    )

                    // Image
                    Image(
                        painter = painterResource(step.imageRes),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp) // Adjust size to take up most of the screen
                    )

                    // Step Description
                    Text(
                        text = step.description.split(":")[1].trim(), // Extracts the description after the colon
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    // Continue Button for Last Step
                    if (page == steps.size - 1) {
                        Button(
                            onClick = { navController.navigate("dashboard") }, // Navigate to the next screen
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF6200EE)),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .fillMaxWidth(0.6f) // Adjust button width
                                .height(50.dp)
                        ) {
                            Text(
                                text = "Continue",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.height(50.dp)) // Add spacing to balance layout
                    }
                }
            }

            // Dots Indicator
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                for (i in steps.indices) {
                    val color =
                        if (i == pagerState.currentPage) Color(0xFF6200EE) else Color.Gray // Active dot color
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .padding(4.dp)
                            .background(color, shape = CircleShape)
                    )
                }
            }
        }
    }
}

// Data class to hold step information
data class StepData(
    val description: String,
    val imageRes: Int
)
