package com.example.lockedin.models

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.auth.userProfileChangeRequest


class AuthViewModel : ViewModel(){

    private val auth : FirebaseAuth = FirebaseAuth.getInstance()

    private val _authState = MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    fun checkAuthStatus(){
        if(auth.currentUser == null){
            _authState.value = AuthState.Unauthenticated
        } else {
            _authState.value = AuthState.Authenticated
        }
    }

    fun login(email: String, password: String){
        if (email.isEmpty() || password.isEmpty()){
            _authState.value = AuthState.Error("Email or password can't be empty")
        }
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener{ task ->
                if (task.isSuccessful){
                    _authState.value = AuthState.Authenticated
                } else {
                    _authState.value = AuthState.Error(task.exception?.message?: "Something went wrong")
                }
            }
    }

    fun signup(email: String, password: String, username: String, profilePicUrl: String, userViewModel: UserViewModel){
        Log.d("username", username)
        Log.d("profilepic", profilePicUrl)


        if (email.isEmpty() || password.isEmpty()){
            _authState.value = AuthState.Error("Email or password can't be empty")
        }
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener{ task ->
                if (task.isSuccessful){
                    _authState.value = AuthState.Authenticated

                    val profileUpdates = userProfileChangeRequest {
                        displayName = username
                        photoUri = Uri.parse(profilePicUrl)
                    }

                    auth.currentUser!!.updateProfile(profileUpdates)
                        .addOnCompleteListener{ task ->
                            if (task.isSuccessful) {
                                Log.d("USER PROFILE", "User profile updated.")
                            }
                    }

                } else {
                    _authState.value = AuthState.Error(task.exception?.message?: "Something went wrong")
                }
            }

    }

    fun signout(){
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }



}


sealed class AuthState{
    object Authenticated: AuthState()
    object Unauthenticated: AuthState()
    object Loading: AuthState()
    data class Error(val message: String) : AuthState()
}