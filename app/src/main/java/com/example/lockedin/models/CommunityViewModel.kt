package com.example.lockedin.models

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import calculateNotificationDelay
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.SetOptions
import com.example.lockedin.models.UserViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import scheduleCommunityNotification
import java.util.UUID

class CommunityViewModel : ViewModel() {


    /// Firebase Firestore instance
    private val db = FirebaseFirestore.getInstance()

    // Firebase Auth instance (to get current user)
    private val auth = FirebaseAuth.getInstance()

    // List to hold the fetched community data
    val communityList = mutableStateListOf<Community>()

    // List to hold posts for a specific community
    val postsForCommunity = mutableStateListOf<Post>()

    val usersForCommunity = mutableStateListOf<Member>()

    val currentCommunity = mutableStateOf<Community?>(null)

    val isLoading = mutableStateOf(false)


    // LOGIC FOR PUSHING DATA TO DB
    fun createCommunity(name: String, description: String, communityPicture: String, notificationTime: String, userViewModel: UserViewModel, context: Context) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val communityId = UUID.randomUUID().toString()  // Generate a unique community ID


            userViewModel.joinUserCommunity(communityId, context)

            // Create a community object to store in Firestore
            val community = hashMapOf(
                "name" to name,
                "description" to description,
                "communityImage" to communityPicture,
                "ownerId" to currentUser.uid,  // Store the ID of the user who created the community
                "createdAt" to System.currentTimeMillis(),
                "id" to communityId,
                "notificationTime" to notificationTime,
            )

            // Store the community in Firestore
            db.collection("communities").document(communityId)
                .set(community, SetOptions.merge())
                .addOnSuccessListener {
                    println("Community successfully created with ID: $communityId")

//                    val delay = calculateNotificationDelay(notificationTime)
//                    if (delay > 0){
//                        scheduleCommunityNotification(
//                            context = context,
//                            delayMillis = delay,
//                            communityName = name,
//                        )
//                    }

                    // Add the creator to the `members` sub-collection and set them as the owner
                    val member = hashMapOf(
                        "userId" to currentUser.uid,
                        "role" to "owner",  // Define them as the owner
                        "username" to "",
                        "profilePic" to "",
                        "joinedAt" to System.currentTimeMillis()
                    )

                    db.collection("communities").document(communityId)
                        .collection("members").document(currentUser.uid)
                        .set(member)
                        .addOnSuccessListener {
                            println("Community owner added to members.")
                        }
                        .addOnFailureListener { e ->
                            println("Error adding community owner: $e")
                        }

                    userViewModel.addUsernameToMembers(communityId,currentUser.uid)
                    userViewModel.addProfilePicToMembers(communityId, currentUser.uid)






                }
                .addOnFailureListener { e ->
                    println("Error creating community: $e")
                }
        } else {
            println("User not authenticated!")
        }
    }

    fun fetchCommunities() {
        viewModelScope.launch {
            isLoading.value = true
            try {
                val communitySnapshot = db.collection("communities").get().await()

                val fetchedCommunities = mutableListOf<Community>()
                for (communityDoc in communitySnapshot.documents) {
                    val community = communityDoc.toObject(Community::class.java)
                    if (community != null) {
                        community.id = communityDoc.id
                        fetchedCommunities.add(community)
                    }
                }

                communityList.clear()
                communityList.addAll(fetchedCommunities.distinctBy { it.id })

            } catch (e: Exception) {
                println("Error fetching communities: ${e.message}")
            } finally {
                isLoading.value = false
            }
        }
    }


    fun fetchCommunitiesForDiscover(userId: String) {
        viewModelScope.launch {
            isLoading.value = true
            try {
                // Fetch the communities the user is already a part of
                val userCommunitiesSnapshot = db.collection("users")
                    .document(userId)
                    .collection("communities")
                    .get()
                    .await()

                val userCommunityIds = userCommunitiesSnapshot.documents.map { it.id }

                // Fetch all communities
                val communitySnapshot = db.collection("communities").get().await()

                val fetchedCommunitiesForDiscover = mutableListOf<Community>()
                for (communityDoc in communitySnapshot.documents) {
                    val community = communityDoc.toObject(Community::class.java)
                    if (community != null && communityDoc.id !in userCommunityIds) {
                        community.id = communityDoc.id
                        fetchedCommunitiesForDiscover.add(community)
                    }
                }

                // Update the community list with communities the user is not in
                communityList.clear()
                communityList.addAll(fetchedCommunitiesForDiscover.distinctBy { it.id })

            } catch (e: Exception) {
                println("Error fetching communities for discover: ${e.message}")
            } finally {
                isLoading.value = false
            }
        }
    }


    // Function to fetch posts for a specific community by ID
    fun fetchPostsForCommunity(communityId: String) {


        db.collection("communities")
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


    fun fetchMembersForCommunity(communityId: String) {


        db.collection("communities")
            .document(communityId)
            .collection("members")
            .get()
            .addOnSuccessListener { snapshot ->
                usersForCommunity.clear()
                for (document in snapshot.documents) {
                    val member = document.toObject(Member::class.java)?.copy(userId = document.id)
                    if (member != null) {
                        usersForCommunity.add(member)
                    }
                }
            }
            .addOnFailureListener { exception ->
                println("Error fetching members: ${exception.message}")
            }
    }



    fun fetchCommunityById(communityId: String) {
        viewModelScope.launch {
            try {
                val document = db.collection("communities").document(communityId).get().await()
                val community = document.toObject(Community::class.java)
                currentCommunity.value = community
            } catch (e: Exception) {
                println("Error fetching community: ${e.message}")
            }
        }
    }

    fun fetchCommunityNameById(communityId: String, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val document = db.collection("communities").document(communityId).get().await()
                val community = document.toObject(Community::class.java)
                onResult(community?.name)
            } catch (e: Exception) {
                println("Error fetching community name: ${e.message}")
                onResult(null)
            }
        }
    }



    // function to search communities by name or description    
    fun searchCommunities(query: String) {
        viewModelScope.launch {
            try {
                val searchResults = mutableStateListOf<Community>()
                val queryLower = query.lowercase()
                
                println("Initiating search for communities with query: '$query'")

                // Get all communities and filter locally
                val snapshot = db.collection("communities").get().await()
                
                println("Total communities fetched for search: ${snapshot.documents.size}")
                
                for (doc in snapshot.documents) {
                    val community = doc.toObject(Community::class.java)
                    if (community != null) {
                        community.id = doc.id
                        val nameMatches = community.name.lowercase().contains(queryLower)
                        val descriptionMatches = community.description?.lowercase()?.contains(queryLower) == true

                        if (nameMatches || descriptionMatches) {
                            println("Matching community found: ${community.name} (ID: ${community.id})")
                            searchResults.add(community)
                        } else {
                            println("Community '${community.name}' did not match the query.")
                        }
                    }
                }
                
                // Update the community list with search results
                communityList.clear()
                communityList.addAll(searchResults)

                if (searchResults.isEmpty()) {
                    println("No communities matched the query: '$query'")
                } else {
                    println("Search complete. Total matching communities: ${searchResults.size}")
                }
                
            } catch (e: Exception) {
                println("Error searching communities: ${e.message}")
            }
        }
    }

    fun fetchPostPoints(
        communityId: String,
        postId: String,
        onResult: (Int?) -> Unit
    ) {
        db.collection("communities")
            .document(communityId)
            .collection("posts")
            .document(postId)
            .get()
            .addOnSuccessListener { document ->
                val points = document.getLong("points")?.toInt()
                onResult(points)
            }
            .addOnFailureListener { exception ->
                println("Error fetching points: ${exception.message}")
                onResult(null)
            }
    }

    fun updatePoints(
        communityId: String,
        postId: String,
        newPoints: Int,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("communities")
            .document(communityId)
            .collection("posts")
            .document(postId) // Use the unique Firestore-generated postId
            .update("points", newPoints)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }

    fun userCanVote(
        communityId: String,
        postId: String,
        userId: String,
        onResult: (Boolean) -> Unit
    ) {
        db.collection("communities")
            .document(communityId)
            .collection("posts")
            .document(postId)
            .collection("voters")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                onResult(!document.exists()) // User can vote if no record exists
            }
            .addOnFailureListener { exception ->
                println("Error checking voter status: ${exception.message}")
                onResult(false) // Default to not allowing a vote on error
            }
    }

    fun recordVote(
        communityId: String,
        postId: String,
        userId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val voterData = hashMapOf("votedAt" to System.currentTimeMillis())

        db.collection("communities")
            .document(communityId)
            .collection("posts")
            .document(postId)
            .collection("voters")
            .document(userId)
            .set(voterData)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onFailure(exception) }
    }

    fun updateCommunity(
        communityId: String,
        name: String,
        description: String,
        notificationTime: String,
        imageUrl: String
    ) {
        val updates = mapOf(
            "name" to name,
            "description" to description,
            "notificationTime" to notificationTime,
            "imageUrl" to imageUrl
        )

        db.collection("communities").document(communityId)
            .update(updates)
            .addOnSuccessListener {
                println("Community updated successfully")
            }
            .addOnFailureListener { e ->
                println("Error updating community: ${e.message}")
            }
    }




}