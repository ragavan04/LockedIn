package com.example.lockedin.models

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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

    // List to hold posts for a specific community

    private val _userCommunities = MutableLiveData<List<Community>>()
    val userCommunities: LiveData<List<Community>> = _userCommunities


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
            "streak" to 0
        )

        db.collection("users").document(userID)
            .collection("communities").document(communityID).set(userCommunity, SetOptions.merge())
            .addOnFailureListener { e ->
                println("Error storing user: $e")
            }

        }
    }

    fun fetchUsers() {
        viewModelScope.launch {
            try{
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
            } catch (e: Exception){
                println("Error fetching users: ${e.message}")
            }
        }

    }

    fun fetchUserCommunityById(communityId: String) {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                if(currentUser != null) {
                    val userID = currentUser.uid

                    // Fetch the UserCommunity documents within the User Data Structure
                    val document = db.collection("users").document(userID)
                        .collection("communities").document(communityId).get().await()

                    val userCommunity = document.toObject(UserCommunity::class.java)

                    if(userCommunity != null) {

                        println("User Community found: ${userCommunity.id}")

                        currentUserCommunity.value = userCommunity
                    }

                }
            } catch (e: Exception) {
                println("Error fetching community: ${e.message}")
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

    private fun fetchCommunityDetails(communityIds: List<String>) {
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
}