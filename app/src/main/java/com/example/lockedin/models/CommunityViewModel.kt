package com.example.lockedin.models

import androidx.compose.runtime.mutableStateListOf
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

    // LOGIC FOR PUSHING DATA TO DB
    fun createCommunity(name: String, description: String, communityPicture: String) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val communityId = UUID.randomUUID().toString()  // Generate a unique community ID

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
            try{
                val db = FirebaseFirestore.getInstance()
                println("Attempting to fetch communities...")
                val communitySnapshot = db.collection("communities").get().await()
                println("Communities fetched: ${communitySnapshot.documents.size}")

                communityList.clear()

                for (communityDoc in communitySnapshot.documents) {
                    val community = communityDoc.toObject(Community::class.java)
                    if (community != null) {

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
}