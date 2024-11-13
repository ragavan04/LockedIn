package com.example.lockedin.store.presentation.upload_post

import android.graphics.Bitmap
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.lockedin.models.AuthState
import com.example.lockedin.models.AuthViewModel
import com.example.lockedin.models.CommunityViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream
import java.util.*

private val db = FirebaseFirestore.getInstance()
@Composable
fun UploadPost(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel,
    communityViewModel: CommunityViewModel = viewModel(),
    communityId: String
) {
    val authState = authViewModel.authState.observeAsState()

    LaunchedEffect(authState.value) {
        when(authState.value) {
            is AuthState.Unauthenticated -> navController.navigate("login")
            else -> Unit
        }
    }

    var capturedImage by remember { mutableStateOf<Bitmap?>(null) }
    var showSuccessMessage by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),  // Set screen background to black
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        // Camera Capture
        CameraCapture(onImageCaptured = { bitmap ->
            capturedImage = bitmap
        })

        Spacer(modifier = Modifier.height(16.dp))

        // Display the captured image if available
        capturedImage?.let { image ->
            Image(
                bitmap = image.asImageBitmap(),
                contentDescription = "Captured Image",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.Gray)
                    .padding(16.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Upload Button
            Button(
                onClick = {
                    uploadImageToFirebaseStorage(image, communityId) { success ->
                        if (success) {
                            showSuccessMessage = true
                            // Show success message and navigate
                            navController.navigate("CommunityPosts/$communityId") {
                                popUpTo("UploadPost/$communityId") { inclusive = true }
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.White,  // Button background color
                    contentColor = Color.Black      // Button text color
                )
            ) {
                Text("Upload")
            }
        }

        // Display success message
        if (showSuccessMessage) {
            Text(
                text = "Uploaded Successfully!",
                color = Color.White,
                modifier = Modifier.padding(top = 16.dp),
                fontSize = 18.sp
            )
        }
    }
}

@Composable
fun CameraCapture(onImageCaptured: (Bitmap?) -> Unit) {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        onImageCaptured(bitmap)
    }

    Button(
        onClick = { launcher.launch() },
        colors = ButtonDefaults.buttonColors(
            backgroundColor = Color.White,  // Button background color
            contentColor = Color.Black      // Button text color
        )
    ) {
        Text("Open Camera")
    }
}

fun uploadImageToFirebaseStorage(
    bitmap: Bitmap,
    communityId: String,
    onComplete: (Boolean) -> Unit
) {
    val storage = Firebase.storage
    val storageRef = storage.reference
    val uniqueID = UUID.randomUUID().toString() // Generate a unique ID for each image
    val imagesRef = storageRef.child("images/$uniqueID.jpg")

    // Convert the bitmap to a byte array
    val baos = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
    val data = baos.toByteArray()

    imagesRef.putBytes(data)
        .addOnSuccessListener {
            imagesRef.downloadUrl.addOnSuccessListener { uri ->
                val imageUrl = uri.toString()
                saveImageUrlToCommunityPosts(imageUrl, communityId, onComplete)
            }
        }
        .addOnFailureListener {
            onComplete(false)
        }
}

fun saveImageUrlToCommunityPosts(
    imageURL: String,
    communityId: String,
    onComplete: (Boolean) -> Unit
) {
    val currentUser = FirebaseAuth.getInstance().currentUser
    val userId = currentUser?.uid ?: return

    val post = hashMapOf(
        "imageURL" to imageURL,
        "timePosted" to System.currentTimeMillis(),
        "userId" to userId
    )

    db.collection("communities")
        .document(communityId)
        .collection("posts")
        .add(post)
        .addOnSuccessListener {
            onComplete(true)
        }
        .addOnFailureListener {
            onComplete(false)
        }
}
