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

class UserViewModel : ViewModel() {


    /// Firebase Firestore instance
    private val db = FirebaseFirestore.getInstance()

    // Firebase Auth instance (to get current user)
    private val auth = FirebaseAuth.getInstance()

    // List to hold the fetched community data
    val communityList = mutableStateListOf<Community>()

    val userList = mutableStateListOf<User>()

    // List to hold posts for a specific community
    val postsForCommunity = mutableStateListOf<Post>()

    val currentCommunity = mutableStateOf<Community?>(null)


    // LOGIC FOR PUSHING USER DATA TO DATABASE
    fun createUser(username: String) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userID = currentUser.uid  // Generate a unique community ID

            // Create a community object to store in Firestore
            val user = hashMapOf(
                "username" to username
            )

            // Store the community in Firestore
            db.collection("users").document(userID)
                .set(user, SetOptions.merge())
                .addOnFailureListener { e ->
                    println("Error storing user: $e")
                }
        } else {
            println("User not authenticated!")
        }
    }

    fun fetchCommunities() {
        viewModelScope.launch {
            try{
                val db = FirebaseFirestore.getInstance()
                println("Attempting to fetch communities...")
                val communitySnapshot = db.collection("communities").get().await()
                println("Communities fetched: ${communitySnapshot.documents.size}")

                communityList.clear()

                for (communityDoc in communitySnapshot.documents) {
                    val community = communityDoc.toObject(Community::class.java)
                    if (community != null) {
                        community.id = communityDoc.id

                        println("Community found: ${community.name}")

                        // Fetch members for each community
                        val membersSnapshot = communityDoc.reference.collection("members").get().await()
                        val members = membersSnapshot.toObjects(Member::class.java)
                        println("Members found: ${members.size}")

                        // Fetch posts for each community
                        val postsSnapshot = communityDoc.reference.collection("posts").get().await()
                        val posts = postsSnapshot.toObjects(Post::class.java)
                        println("Posts found: ${posts.size}")

                        // Assign the subcollection data
                        community.memberId = members
                        community.posts = posts

                        communityList.add(community)
                    }
                }
            } catch (e: Exception){
                println("Error fetching communities: ${e.message}")
            }
        }

    }

    // Function to fetch posts for a specific community by ID
    fun fetchPostsForCommunity(communityId: String) {
        viewModelScope.launch {
            try {
                // Clear previous posts
                postsForCommunity.clear()

                // Fetch posts for the specific community
                val postsSnapshot = db.collection("communities")
                    .document(communityId)
                    .collection("posts")
                    .get()
                    .await()

                // Convert and add posts to the list
                val posts = postsSnapshot.toObjects(Post::class.java)
                postsForCommunity.addAll(posts)

                println("Posts fetched for community ID $communityId: ${posts.size}")

            } catch (e: Exception) {
                println("Error fetching posts for community ID $communityId: ${e.message}")
            }
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

}