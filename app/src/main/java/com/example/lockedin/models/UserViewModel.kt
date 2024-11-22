package com.example.lockedin.models
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


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

    val postsForCommunity = mutableStateListOf<Post>()

    val userCommunities: LiveData<List<Community>> = _userCommunities

    val pointsForCommunity = mutableStateOf<Int?>(0)

    val streakForCommunity = mutableStateOf<Int?>(0)

    val consistencyForCommunity = mutableStateOf<Float?>(0f)

    val currentUserCommunity = mutableStateOf<UserCommunity?>(null)


    // LOGIC FOR PUSHING USER DATA TO DATABASE
    fun createUser(username: String) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            var userID = currentUser.uid

            // Create a community object to store in Firestore
            val user = hashMapOf(
                "userID" to userID,
                "username" to username
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


    fun joinUserCommunity(communityID: String) {
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
                .addOnFailureListener { e ->
                    println("Error storing user: $e")
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


        fun fetchUserCommunityById(communityId: String) {
            viewModelScope.launch {
                try {
                    val currentUser = auth.currentUser
                    if (currentUser != null) {
                        val userID = currentUser.uid

                        val document = db.collection("users").document(userID)
                            .collection("communities").document(communityId).get().await()

                        val userCommunity = document.toObject(UserCommunity::class.java)

                        if (userCommunity != null) {

                            println("User Community found: ${userCommunity.id}")

                            currentUserCommunity.value = userCommunity
                        }

                    }
                } catch (e: Exception) {
                    println("Error fetching community: ${e.message}")
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
                Firebase.firestore.collection("communities")
                    .document(id)
                    .get()
                    .addOnSuccessListener { document ->
                        val community = document.toObject(Community::class.java)
                        if (community != null) {
                            communities.add(community)
                        }
                        _userCommunities.value = communities
                    }
            }
        }

        fun fetchUserCommunities() {
            val userId = Firebase.auth.currentUser?.uid
            if (userId != null) {
                Firebase.firestore.collection("users")
                    .document(userId)
                    .collection("communities")
                    .get()
                    .addOnSuccessListener { documents ->
                        val communityIds = documents.map { it.id }
                        fetchCommunityDetails(communityIds)
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


        fun fetchPointsForCommunity(communityID: String, onResult: (Int?) -> Unit) {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val userID = currentUser.uid

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


    fun fetchConsistencyForCommunity(communityID: String, onResult: (Float?) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userID = currentUser.uid

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



    }



