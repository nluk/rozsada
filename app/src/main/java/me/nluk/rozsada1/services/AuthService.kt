package me.nluk.rozsada1.services;

import dev.gitlive.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

interface AuthService {
    fun authenticationStatusFlow() : StateFlow<Boolean>
    fun authUserFlow() : StateFlow<FirebaseUser?>
    fun isAuthenticated() : Boolean
    suspend fun login(username : String, password : String) : AuthError?
    suspend fun signUp(username : String, password : String) : AuthError?
    suspend fun logout()
    suspend fun <T> callAuthenticated(f : suspend (userId : String)-> T?) : T?
    suspend fun runAuthenticated(f : suspend (userId : String)-> Unit)
}

@JvmInline
value class AuthError(val error : String)