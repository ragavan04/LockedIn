package com.example.lockedin.models

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

class CommunityViewModel : ViewModel() {


    /// Firebase Firestore instance
    private val db = FirebaseFirestore.getInstance()

    // Firebase Auth instance (to get current user)
    private val auth = FirebaseAuth.getInstance()

    // List to hold the fetched community data
    val communityList = mutableStateListOf<Community>()

    // List to hold posts for a specific community
    val postsForCommunity = mutableStateListOf<Post>()

    val currentCommunity = mutableStateOf<Community?>(null)

    val isLoading = mutableStateOf(false)


    // LOGIC FOR PUSHING DATA TO DB
    fun createCommunity(name: String, description: String, communityPicture: String, userViewModel: UserViewModel) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val communityId = UUID.randomUUID().toString()  // Generate a unique community ID


            userViewModel.joinUserCommunity(communityId)

            // Create a community object to store in Firestore
            val community = hashMapOf(
                "name" to name,
                "description" to description,
                "communityImage" to communityPicture,
                "ownerId" to currentUser.uid,  // Store the ID of the user who created the community
                "createdAt" to System.currentTimeMillis()
            )

            // Store the community in Firestore
            db.collection("communities").document(communityId)
                .set(community, SetOptions.merge())
                .addOnSuccessListener {
                    println("Community successfully created with ID: $communityId")

                    // Create `posts` sub-collection (empty at the start)
                    val defaultPost = hashMapOf(
                        "userId" to currentUser.uid,
                        "imageURL" to "deafultdummyURL",
                        "timePosted" to System.currentTimeMillis()
                    )
                    db.collection("communities").document(communityId)
                        .collection("posts").document("defaultPost")
                        .set(defaultPost)
                        .addOnSuccessListener {
                            println("Default post successfully added.")
                        }
                        .addOnFailureListener { e ->
                            println("Error adding default post: $e")
                        }


                    // Add the creator to the `members` sub-collection and set them as the owner
                    val member = hashMapOf(
                        "userId" to currentUser.uid,
                        "role" to "owner",  // Define them as the owner
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



}