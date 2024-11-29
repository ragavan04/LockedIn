package com.example.lockedin.models
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import calculateNotificationDelay
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.userProfileChangeRequest
import androidx.lifecycle.observe
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import scheduleCommunityNotification
import java.time.*


class UserViewModel : ViewModel() {


    /// Firebase Firestore instance
    private val db = FirebaseFirestore.getInstance()

    // Firebase Auth instance (to get current user)
    private val auth = FirebaseAuth.getInstance()

    // List to hold the fetched community data
    val communityList = mutableStateListOf<Community>()

    val userList = mutableStateListOf<User>()

    val _userCommunities = MutableLiveData<List<Community>>()

    val UserCommunityList = mutableStateListOf<UserCommunity>()

    var currentUsername = mutableStateOf("")

    var currentUserBio = mutableStateOf("")

    var currentUserProfilePic = mutableStateOf<Uri?>(null)

    val postsForCommunity = mutableStateListOf<Post>()

    val userCommunities: LiveData<List<Community>> = _userCommunities

    val pointsForCommunity = mutableStateOf<Int?>(0)

    val streakForCommunity = mutableStateOf<Int?>(0)

    val consistencyForCommunity = mutableStateOf<Float?>(0f)

    val currentUser = mutableStateOf<User?>(null)

    val currentUserCommunity = mutableStateOf<UserCommunity?>(null)

    var totalPoints = mutableStateOf<Int?>(0)

    var averageConsistency = mutableStateOf<Float?>(0f)


    // LOGIC FOR PUSHING USER DATA TO DATABASE
    fun createUser(username: String) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            var userID = currentUser.uid


            // Create a community object to store in Firestore
            val user = hashMapOf(
                "userID" to userID,
                "username" to username,
                "bio" to "No bio yet",
            )


            // Store the user in Firestore
            db.collection("users").document(userID)
                .set(user, SetOptions.merge())
                .addOnSuccessListener {
                    println("User sucessfully a7777dded!")
                }
                .addOnFailureListener { e ->
                    println("Error storing use777r: $e")
                }

        } else {
            println("User not authenticated777!")
        }
    }



    fun joinUserCommunity(communityID: String, context: Context) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userID = currentUser.uid

            val userCommunity = hashMapOf(
                "communityID" to communityID,
                "points" to 0,
                "streak" to 0,
                "consistency" to 0
            )

            //updatePoints(communityID, 0)

            db.collection("users").document(userID)
                .collection("communities").document(communityID).set(userCommunity, SetOptions.merge())
                .addOnSuccessListener {
                    // Add user to the community's members sub-collection
                    val member = hashMapOf(
                        "userId" to userID,
                        "username" to currentUser.displayName, // Doesn't make sense to include name here as it can change
                        "profilePic" to currentUser.photoUrl,
                        "role" to "member",
                        "joinedAt" to System.currentTimeMillis()
                    )
                    db.collection("communities").document(communityID)
                        .collection("members").document(userID)
                        .set(member)
                        .addOnSuccessListener {
                            println("User added to community members.")
                            fetchCommunityTime(communityID) { notificationTime, communityName ->
                                if (notificationTime != null && communityName != null) {
                                    val delay = calculateNotificationDelay(notificationTime)
                                    if (delay > 0) {
                                        scheduleCommunityNotification(
                                            context = context,
                                            delayMillis = delay,
                                            communityName = communityName,
                                        )
                                    }
                                }
                            }
                        }
                        .addOnFailureListener { e ->
                            println("Error adding user to community members: $e")
                            // Rollback: Remove the community from the user's list
                            db.collection("users").document(userID)
                                .collection("communities").document(communityID)
                                .delete()
                                .addOnSuccessListener {
                                    println("Rolled back: Community removed from user's list.")
                                }
                                .addOnFailureListener { rollbackError ->
                                    println("Error during rollback: $rollbackError")
                                }
                        }
                }
                .addOnFailureListener { e ->
                    println("Error storing user: $e")
                }

        }
    }

    fun addUsernameToMembers(communityId: String, userId: String) {
        viewModelScope.launch {
            try {
                var currentUser = auth.currentUser
                if (currentUser != null) {
                    val userCommunitySnapshot = db.collection("users").document(userId).get().await()

                    if(userCommunitySnapshot != null) {
                        val username = userCommunitySnapshot.getString("username")

                        println("I WILL BE ADDING THE USERNAME ${username} TO MEMBERS")

                        if(username != null) {

                            db.collection("communities").document(communityId)
                                .collection("members").document(userId).update("username", username)

                        }
                    }
                }
            } catch (e: Exception) {
                println("Error fetching username: ${e.message}")
            }
        }
    }


    fun updateProfilePicToMembers(communityId: String, userId: String, imageUri: String?) {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val userID = currentUser.uid

                    // Check if the imageUri is valid, otherwise set to an empty string
                    val profilePicUri = if (imageUri.isNullOrBlank() || imageUri == "error") "" else imageUri

                    db.collection("communities").document(communityId).collection("members").document(userId).update("profilePic", profilePicUri)
                        .addOnSuccessListener {
                            println("ProfilePic updated successfully: $profilePicUri")
                        }
                        .addOnFailureListener { e ->
                            println("Error updating profilePic in Firestore: ${e.message}")
                        }
                }
            } catch (e: Exception) {
                println("Error updating profilePic: ${e.message}")
            }
        }
    }

    fun addProfilePicToMembers(communityId: String, userId: String) {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    // Get the photoUrl and ensure a valid value is set
                    val photoUrl = currentUser.photoUrl
                    val photoUrlString = photoUrl?.toString() ?: ""

                    if (photoUrlString.isBlank() || photoUrlString == "error") {
                        println("Invalid photoUrl found, defaulting to an empty string.")
                    } else {
                        println("Valid photoUrl found: $photoUrlString")
                    }

                    val profilePicUri = if (photoUrlString.isBlank() || photoUrlString == "error") "" else photoUrlString

                    // Update the Firestore document with the validated profilePicUri
                    db.collection("communities").document(communityId)
                        .collection("members").document(userId)
                        .update("profilePic", profilePicUri)
                        .addOnSuccessListener {
                            println("Profile picture updated successfully for user: $userId")
                        }
                        .addOnFailureListener { e ->
                            println("Error updating profile picture in Firestore: ${e.message}")
                        }
                }
            } catch (e: Exception) {
                println("Error in addProfilePicToMembers: ${e.message}")
            }
        }
    }


    private fun fetchCommunityTime(communityID: String, callback: (String?, String?) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        db.collection("communities").document(communityID)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val notificationTime = document.getString("notificationTime")
                    val communityName = document.getString("name")
                    callback(notificationTime, communityName)
                } else {
                    callback("No time found", "no community name found")
                }
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                callback("No time found", "no community name found")
                Log.d("USER VIEW MODEL", "ERROR WHEN CREATING NOTIF CHANNEL FOR USER JOINING")

            }
    }

    fun fetchUserBio() {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val userID = currentUser.uid
                    val document = db.collection("users").document(userID).get().await()
                    
                    val bio = document.getString("bio") ?: ""
                    currentUserBio.value = bio
                    println("Fetched bio: $bio")
                }
            } catch (e: Exception) {
                println("Error fetching bio: ${e.message}")
            }
        }
    }

    fun fetchUserById(userId: String) {
        viewModelScope.launch {
            try {
                val document = db.collection("users").document(userId).get().await()
                val user = document.toObject(User::class.java)
                currentUser.value = user
            } catch (e: Exception) {
                println("Error fetching community: ${e.message}")
            }
        }
    }


    fun fetchUsers() {
        viewModelScope.launch {
            try {
                val db = FirebaseFirestore.getInstance()
                println("Attempting to fetch users...")
                val userSnapshot = db.collection("users").get().await()
                println("Users fetched: ${userSnapshot.documents.size}")

                userList.clear()

                for (usersDoc in userSnapshot.documents) {
                    val users = usersDoc.toObject(User::class.java)
                    val currentUser = auth.currentUser
                    if (users != null) {
                        users.userID = usersDoc.id

                        println("Users found: ${users.username}")

                        userList.add(users)
                    }
                }
            } catch (e: Exception) {
                println("Error fetching users: ${e.message}")
            }
        }

    }

    fun fetchUsername() {
        viewModelScope.launch {

            try {
                var currentUser = auth.currentUser
                if (currentUser != null) {
                    val userID = currentUser.uid

                    val userCommunitySnapshot = db.collection("users").document(userID).get().await()

                    if(userCommunitySnapshot != null) {
                        val username = userCommunitySnapshot.getString("username")

                        if(username != null) {
                            currentUsername.value = username
                        }
                    }

                }

            } catch (e: Exception) {
                println("Error fetching username: ${e.message}")
            }
        }
    }

    fun fetchUserCommunityList(communityId: String) {
        viewModelScope.launch {

            try {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val userID = currentUser.uid

                    val userCommunitySnapshot = db.collection("users").document(userID).collection("communities").get().await()

                    val fetchedUserCommunities = mutableListOf<UserCommunity>()
                    for (userCommunityDoc in userCommunitySnapshot.documents) {
                        val userCommunity = userCommunityDoc.toObject(UserCommunity::class.java)
                        if (userCommunity != null) {
                            userCommunity.id = userCommunityDoc.id
                            fetchedUserCommunities.add(userCommunity)
                        }
                    }

                    UserCommunityList.clear()
                    UserCommunityList.addAll(fetchedUserCommunities.distinctBy { it.id })
                }

            } catch (e: Exception) {
                println("Error fetching communities: ${e.message}")
            }
        }
    }


    fun fetchUserCommunityById(communityId: String, userId: String) {
        viewModelScope.launch {
            try {
                var userID: String = ""

                if(userId == "") {
                    userID = auth.currentUser?.uid.toString()
                } else {
                    userID = userId
                }

                val document = db.collection("users").document(userID)
                    .collection("communities").document(communityId).get().await()

                val userCommunity = document.toObject(UserCommunity::class.java)

                if (userCommunity != null) {

                    println("User Community found: ${userCommunity.id}")

                    currentUserCommunity.value = userCommunity
                }

            } catch (e: Exception) {
                println("Error fetching community: ${e.message}")
            }
        }
    }

    // fun updateUsername(updatedUsername: String) {

    //     viewModelScope.launch {

    //         try {
    //             val currentUser = auth.currentUser
    //             if (currentUser != null) {
    //                 val userID = currentUser.uid

    //                 db.collection("users").document(userID).update("username", updatedUsername)
    //                 println("User updated: $updatedUsername")

    //             }

    //         } catch (e: Exception) {
    //             println("Error updating username: ${e.message}")
    //         }
    //     }

    // }

    fun updateUsername(updatedUsername: String) {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val userID = currentUser.uid
                    
                    // First update user document
                    db.collection("users").document(userID)
                        .update("username", updatedUsername)
                        .await() // Wait for update to complete
                    
                    // Fetch communities
                    val communities= db.collection("users")
                        .document(userID)
                        .collection("communities")
                        .get()
                        .await()
                    
                    println("The # of communities the user is in is: ${communities.size()}")
                    
                    // Update username in all communities
                    for (community in communities) {
                        db.collection("communities")
                            .document(community.id)
                            .collection("members")
                            .document(userID)
                            .update("username", updatedUsername)
                            .await() // Wait for each update to complete
                    }
                    
                    // Update Auth Profile
                    val profileUpdates = userProfileChangeRequest {
                        displayName = updatedUsername
                    }
                    currentUser.updateProfile(profileUpdates).await()
                    
                    println("Username update completed for all communities")
                }
            } catch (e: Exception) {
                println("Error updating username: ${e.message}")
            }
        }
    }

    fun updateUserBio(bio: String) {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val userID = currentUser.uid
                    
                    // Update Firestore
                    db.collection("users").document(userID)
                        .update("bio", bio)
                        .addOnSuccessListener {
                            println("Bio updated successfully: $bio")
                        }
                        .addOnFailureListener { e ->
                            println("Error updating bio in Firestore: ${e.message}")
                        }.await()
                }
            } catch (e: Exception) {
                println("Error updating bio: ${e.message}")
            }
        }
    }

    fun updateProfilePic(imageUri: String?) {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val userID = currentUser.uid
                    
                    // Check if the imageUri is valid, otherwise set to empty string
                    val profilePicUri = if (imageUri.isNullOrBlank() || imageUri == "error") "" else imageUri
                    
                    // First update user document
                    db.collection("users").document(userID)
                        .update("profilePic", profilePicUri)
                        .await()
                    
                    // Fetch communities
                    val communitiesSnapshot = db.collection("users")
                        .document(userID)
                        .collection("communities")
                        .get()
                        .await()
                    
                    val communities = communitiesSnapshot.documents.map { doc -> doc.id }
                    println("The # of communities the user is in is: ${communities.size}")
                    
                    // Update profile pic in all communities
                    for (communityId in communities) {
                        db.collection("communities")
                            .document(communityId)
                            .collection("members")
                            .document(userID)
                            .update("profilePic", profilePicUri)
                            .await()
                    }
                    
                    // Update Auth Profile
                    val profileUpdates = userProfileChangeRequest {
                        photoUri = if (profilePicUri.isEmpty()) null else Uri.parse(profilePicUri)
                    }
                    currentUser.updateProfile(profileUpdates).await()
                    
                    println("Profile picture update completed for all communities")
                }
            } catch (e: Exception) {
                println("Error updating profile picture: ${e.message}")
            }
        }
    }

    fun updateProfilePictureForCommunities(userId: String, imageUri: String?) {
        val communities = _userCommunities.value ?: emptyList()
        var totalPointsLocal = 0 // Use a local variable to accumulate points
        var processedCount = 0 // Track processed communities

        println("The # of communities the user is in is!!!!: ${communities.size}")

        fetchUserCommunities(userId)
        var imageUriString: String = ""

        for (community in communities) {

            println("CHECKING FOR THE ID ${community.id}")
            if(imageUri != "error") {
                if (imageUri != null) {
                    imageUriString = imageUri
                }
            }

            println("PASSING IN THE URL ${imageUriString}")
            val userCommunity = fetchUserCommunityById(community.id, userId)

            if (userCommunity != null) {
                updateProfilePicToMembers(community.id,userId, imageUriString)
            }
        }
    }

    fun updateTotalPoints(userId: String) {
        val communities = _userCommunities.value ?: emptyList()
        var totalPointsLocal = 0 // Use a local variable to accumulate points
        var processedCount = 0 // Track processed communities

        println("The # of communities the user is in is: ${communities.size}")

        for (community in communities) {
            val userCommunity = fetchUserCommunityById(community.id, userId)

            if (userCommunity != null) {
                fetchPointsForCommunity(community.id, userId) { points ->
                    if (points != null) {
                        totalPointsLocal += points
                    }

                    processedCount++

                    if (processedCount == communities.size) {
                        // Update totalPoints only once after processing all communities
                        totalPoints.value = totalPointsLocal
                        println("Total points updated: ${totalPoints.value}")
                    }
                }
            } else {
                processedCount++

                if (processedCount == communities.size) {
                    // Update totalPoints if no user communities found
                    totalPoints.value = totalPointsLocal
                    println("Total points updated: ${totalPoints.value}")
                }
            }
        }
    }

    fun updateAverageConsistency(userId: String) {

        val communities = _userCommunities.value ?: emptyList()

        for (community in communities) {
            // Access each community here
            println("COMMUNITY NAME FROM CONSISTENCY: ${community.name}")
        }

        println("THIS USER IS FOUND IN ${communities.size} COMMUNITIES FOR USER ${userId}")


        if (communities.isEmpty()) {
            averageConsistency.value = 0f
            println("Average consistency is ${averageConsistency.value}")
            return
        }

        var totalConsistency = 0f
        var processedCount = 0

        for (community in communities) {
            val userCommunity = fetchUserCommunityById(community.id, userId)

            println("Checking for the community: ${community.id}")

            if (userCommunity != null) {
                fetchConsistencyForCommunity(community.id, userId) { consistency ->
                    if (consistency != null) {
                        totalConsistency += consistency
                    }
                    println("Obtained consistency for ${community.id}: $consistency")

                    processedCount++

                    if (processedCount == communities.size) {
                        averageConsistency.value = totalConsistency / communities.size.toFloat()
                        println("Average consistency is ${averageConsistency.value}")
                    }
                }
            } else {

                processedCount++
                if (processedCount == communities.size) {
                    averageConsistency.value = totalConsistency / communities.size.toFloat()
                    println("Average consistency is ${averageConsistency.value}")
                }
            }
        }

    }



    fun updateProfilePicture(profilePicUrl: String) {

        viewModelScope.launch {
            try {

                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val userID = currentUser.uid

                    val document = db.collection("users").document(userID).get().await()


                    val profileUpdates = userProfileChangeRequest {
                        displayName = document.getString("username")
                        photoUri = Uri.parse(profilePicUrl)
                    }

                    auth.currentUser!!.updateProfile(profileUpdates)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d("USER PROFILE", "User profile updated.")
                            }
                        }
                }

            } catch (e: Exception) {
                println("Error updating username: ${e.message}")
            }
        }


    }

    fun fetchUsernameByUserId(userId: String, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val document = db.collection("users").document(userId).get().await()
                if (document.exists()) {
                    val username = document.getString("username")
                    onResult(username) // Return the username
                } else {
                    onResult(null) // User does not exist
                }
            } catch (e: Exception) {
                println("Error fetching username: ${e.message}")
                onResult(null) // Error occurred
            }
        }
    }

    fun fetchCommunityDetails(communityIds: List<String>) {
        val communities = mutableListOf<Community>()
        for (id in communityIds) {
            println("ID PART 2: ${id}")
            Firebase.firestore.collection("communities")
                .document(id)
                .get()
                .addOnSuccessListener { document ->
                    val community = document.toObject(Community::class.java)
                    println("STORING THE ID: ${community?.id}")
                    if (community != null) {
                        communities.add(community)
                    }
                    _userCommunities.value = emptyList()
                    _userCommunities.value = communities

                    _userCommunities.value?.let { communities ->
                        for (community in communities) {
                            // Access each community here
                            println("Community Name: ${community.name}")
                        }
                    }


                }
        }
        println("User is a part of ${communities.size} communities")
    }

    fun fetchUserCommunities(userId: String) {

        _userCommunities.value = emptyList()

        var userID: String = ""

        if(userId == "") {
            userID = auth.currentUser?.uid.toString()
        } else {
            userID = userId
            println("User ID has been assigned to ${userID}")
        }

        println("Fetching information for ${userID}")

        if (userID != null) {
            Firebase.firestore.collection("users")
                .document(userID)
                .collection("communities")
                .get()
                .addOnSuccessListener { documents ->
                    val communityIds = documents.map { it.id }
                    fetchCommunityDetails(communityIds)
                    println("NUMBER OF IDS FOUND IS ${communityIds.size}")
                    for(id in communityIds) {
                        println("ID: ${id}")
                    }
                }
        }

    }



    // Updating Points field for each community
//    fun updatePoints(communityID: String, increase: Int) {
//        val currentUser = auth.currentUser
//
//        if(currentUser != null) {
//            val userID = currentUser.uid
//
//            viewModelScope.launch {
//                try {
//
//                    val document = db.collection("users").document(userID)
//                        .collection("communities").document(communityID).get().await()
//
//
//                    val currentPoints = document.getLong("points")?.toInt() ?: 0
//
//
//                    val updatedPoints = currentPoints + increase
//
//                    db.collection("users").document(userID)
//                        .collection("communities").document(communityID)
//                        .update("points", updatedPoints)
//                        .addOnSuccessListener {
//                            println("Points updated successfully: $updatedPoints")
//                        }
//                        .addOnFailureListener { e ->
//                            println("Error updating points: $e")
//                        }
//                } catch (e: Exception) {
//                    println("Error updating points: ${e.message}")
//                }
//            }
//        }
//    }


    fun fetchPointsForCommunity(communityID: String, userId: String, onResult: (Int?) -> Unit) {

        var userID: String = ""

        if(userId == "") {
            userID = auth.currentUser?.uid.toString()
        } else {
            userID = userId
        }

        println("Checking points for user ${userID}")

        viewModelScope.launch {
            try {
                println("Fetching points for community: $communityID")

                val document = db.collection("users").document(userID)
                    .collection("communities").document(communityID).get().await()

                val userCommunity = document.toObject(UserCommunity::class.java)

                val points = userCommunity?.points
                pointsForCommunity.value = points

                println("Fetched points: $points")
                onResult(points)
            } catch (e: Exception) {
                println("Error fetching points: ${e.message}")
                pointsForCommunity.value = null
                onResult(null)
            }
        }
    }

    fun fetchStreakForCommunity(communityID: String, onResult: (Int?) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userID = currentUser.uid

            viewModelScope.launch {
                try {
                    println("Fetching streaks for community: $communityID")

                    val document = db.collection("users").document(userID)
                        .collection("communities").document(communityID).get().await()

                    val userCommunity = document.toObject(UserCommunity::class.java)

                    val streak = userCommunity?.streak
                    streakForCommunity.value = streak

                    println("Fetched streak: $streak")
                    onResult(streak)
                } catch (e: Exception) {
                    println("Error fetching streak: ${e.message}")
                    streakForCommunity.value = null
                    onResult(null)
                }
            }
        }
    }


    fun fetchConsistencyForCommunity(communityID: String, userId: String, onResult: (Float?) -> Unit) {
        var userID: String = ""

        if(userId == "") {
            userID = auth.currentUser?.uid.toString()
        } else {
            userID = userId
        }


        viewModelScope.launch {
            try {
                println("Fetching consistency rating for community: $communityID")

                val document = db.collection("users").document(userID)
                    .collection("communities").document(communityID).get().await()

                val userCommunity = document.toObject(UserCommunity::class.java)

                val consistency = userCommunity?.consistency
                consistencyForCommunity.value = consistency

                println("Fetched streak: $consistency")
                onResult(consistency)
            } catch (e: Exception) {
                println("Error fetching streak: ${e.message}")
                streakForCommunity.value = null
                onResult(null)
            }
        }
    }




    // Function to fetch posts for a specific community by ID
    fun fetchPostsForCommunity(communityId: String) {

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userID = currentUser.uid

            db.collection("users")
                .document(userID)
                .collection("communities")
                .document(communityId)
                .collection("posts")
                .get()
                .addOnSuccessListener { snapshot ->
                    postsForCommunity.clear()
                    for (document in snapshot.documents) {
                        val post = document.toObject(Post::class.java)?.copy(postId = document.id)
                        if (post != null) {
                            postsForCommunity.add(post)
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    println("Error fetching posts: ${exception.message}")
                }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun convertNotificationTimeToMillis(notificationTime: String): Long {
        // Normalize to ensure HH:mm format
        val normalizedTime = notificationTime.split(":").let {
            String.format("%02d:%02d", it[0].toInt(), it[1].toInt())
        }

        // Parse the normalized time
        val localTime = LocalTime.parse(normalizedTime)

        // Get midnight of today in EST
        val estZoneId = ZoneId.of("America/Toronto")
        val todayMidnightInEST = ZonedDateTime.now(estZoneId).toLocalDate().atStartOfDay(estZoneId)

        // Return the full milliseconds for the notification time in EST
        return todayMidnightInEST.toInstant().toEpochMilli() + localTime.toSecondOfDay() * 1000
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun convertToEST(currentTimeMillis: Long): Long {
        // Correct time zone for Toronto
        val torontoZoneId = ZoneId.of("America/Toronto")
        // Convert to Instant
        val utcInstant = Instant.ofEpochMilli(currentTimeMillis)
        // Convert to ZonedDateTime in Toronto
        val torontoZonedDateTime = utcInstant.atZone(torontoZoneId)

        return torontoZonedDateTime.toInstant().toEpochMilli()
    }

    fun timeDifference(postTime: Long, notificationTimeMillis: Long): Int {
        val differenceMillis = postTime - notificationTimeMillis
        return kotlin.math.abs((differenceMillis / (1000 * 60)).toInt()) // Convert millis to minutes
    }


    fun pointsGained(timeDifference: Int): Int {

        var points: Int = 0

        if(timeDifference < 30) {
            points = 100
        } else if(30 <= timeDifference && timeDifference <= 60) {
            points = 75
        } else if(60 <= timeDifference && timeDifference <= 90) {
            points = 50
        } else if(90 <= timeDifference && timeDifference <= 120) {
            points = 25
        } else {
            points = 0
        }

        return points

    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun addPointsForPost(postId: String, communityId: String, userId: String) {
        viewModelScope.launch {
            try {
                val community = db.collection("communities").document(communityId).get().await()
                val post = db.collection("users").document(userId).collection("communities")
                    .document(communityId).collection("posts").document(postId).get().await()

                fetchPointsForCommunity(communityId, userId) { points ->
                    if (points == null) {
                        println("Points not found, defaulting to 0")
                        pointsForCommunity.value = 0
                    }

                    val localPoints = points ?: 0 // Use the fetched points or default to 0
                    println("Points obtained: $localPoints")

                    // Continue with notification and time calculations
                    val notificationTime = community.getString("notificationTime").toString()
                    val postTime = post.getLong("timePosted") ?: 0L

                    val notificationTimeInMillis = convertNotificationTimeToMillis(notificationTime)
                    val postTimeEST = convertToEST(postTime)
                    val notificationTimeEST = convertToEST(notificationTimeInMillis)

                    val timeDifference = timeDifference(notificationTimeEST, postTimeEST)
                    val pointsGained = pointsGained(timeDifference)

                    println("The difference in time is ${timeDifference}")
                    println("The points gained is ${pointsGained}")
                    println("The previous amount of points is ${localPoints}")

                    val totalPoints = localPoints + pointsGained
                    println("Total points: $totalPoints")

                    // Update Firestore with the new points total
                    db.collection("users").document(userId).collection("communities")
                        .document(communityId).update("points", totalPoints)

                    addConsistencyForPost(postId, communityId, userId, totalPoints)
                }
            } catch (e: Exception) {
                println("Error adding points: ${e.message}")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun addStreakForPost(postId: String, communityId: String, userId: String) {
        viewModelScope.launch {
            try {
                val community = db.collection("communities").document(communityId).get().await()
                val post = db.collection("users").document(userId).collection("communities")
                    .document(communityId).collection("posts").document(postId).get().await()

                fetchStreakForCommunity(communityId) { streak ->
                    if (streak == null) {
                        println("Points not found, defaulting to 0")
                        streakForCommunity.value = 0
                    }

                    val localStreak = streak ?: 0 // Use the fetched points or default to 0
                    println("Streak obtained: $localStreak")

                    // Continue with notification and time calculations
                    val notificationTime = community.getString("notificationTime").toString()
                    val postTime = post.getLong("timePosted") ?: 0L

                    val notificationTimeInMillis = convertNotificationTimeToMillis(notificationTime)
                    val postTimeEST = convertToEST(postTime)
                    val notificationTimeEST = convertToEST(notificationTimeInMillis)

                    val timeDifference = timeDifference(notificationTimeEST, postTimeEST)
                    val pointsGained = pointsGained(timeDifference)

                    println("The difference in time is ${timeDifference}")
                    println("The points gained is ${pointsGained}")
                    println("The previous streak was ${localStreak}")

                    var totalStreak: Int = 0

                    if(pointsGained == 100) {
                        totalStreak = localStreak + 1
                    } else {
                        totalStreak = 0
                    }


                    println("New Streak: $totalStreak")

                    // Update Firestore with the new points total
                    db.collection("users").document(userId).collection("communities")
                        .document(communityId).update("streak", totalStreak)
                }
            } catch (e: Exception) {
                println("Error adding streak: ${e.message}")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun addConsistencyForPost(postId: String, communityId: String, userId: String, points: Int) {
        viewModelScope.launch {
            try {
                val member = db.collection("communities").document(communityId).collection("members")
                    .document(userId).get().await()

                    val joinedTime = member.getLong("joinedAt") ?: 0L
                    val currentTime = System.currentTimeMillis()


                    val joinedAtEST = convertToEST(joinedTime)
                    val currentTimeEST = convertToEST(currentTime)

                    var daysJoined = (timeDifference(joinedAtEST, currentTimeEST) / (60 * 24))

                    if(daysJoined == 0) {
                        daysJoined = 1
                    }

                    var localPoints = points;

                    println("The user joined at ${joinedAtEST}")
                    println("The current time is ${currentTimeEST}")
                    println("The number of days the user joined is ${daysJoined}")



                    val maxPoints = daysJoined * 100
                    val consistencyRating = (localPoints.toFloat() / maxPoints) * 100



                    println("The local points is ${localPoints.toInt()}")
                    println("The max points is ${maxPoints}")



                    println("New rating: ${consistencyRating.toInt()}%")

                    // Update Firestore with the new points total
                    db.collection("users").document(userId).collection("communities")
                        .document(communityId).update("consistency", consistencyRating.toInt())

            } catch (e: Exception) {
                println("Error adding points: ${e.message}")
            }
        }
    }







}




