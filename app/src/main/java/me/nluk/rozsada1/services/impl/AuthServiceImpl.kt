package me.nluk.rozsada1.services.impl

import android.util.Log

import com.tylerthrailkill.helpers.prettyprint.pp
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.FirebaseAuthException
import dev.gitlive.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import me.nluk.rozsada1.services.AuthError
import me.nluk.rozsada1.services.AuthService
import javax.inject.Inject

class AuthServiceImpl @Inject constructor(val firebaseAuth : FirebaseAuth) : AuthService{

    private val authServiceScope = CoroutineScope(Dispatchers.IO)
    private val currentUserState = MutableStateFlow(firebaseAuth.currentUser)
    private val loginState = MutableStateFlow(firebaseAuth.currentUser != null)

    @InternalSerializationApi
    private val authStateFlowJob = authServiceScope.launch {
        firebaseAuth.authStateChanged.flowOn(Dispatchers.IO).collectLatest {
            it.pp()
            currentUserState.value = it
            loginState.value = it != null
            println("User state is now [${loginState.value}]")
        }
    }

    override fun authenticationStatusFlow(): StateFlow<Boolean> = loginState

    override fun authUserFlow(): StateFlow<FirebaseUser?> = currentUserState

    override fun isAuthenticated() : Boolean = firebaseAuth.currentUser != null

    override suspend fun login(username : String, password : String): AuthError? {
        try {
            firebaseAuth.signInWithEmailAndPassword(email = username, password)
        }catch (e : FirebaseAuthException){
            return AuthError(e.message ?: "Unknown Auth Error")
        }
        return null
    }

    override suspend fun signUp(username: String, password: String): AuthError? {
        try {
            firebaseAuth.createUserWithEmailAndPassword(email = username, password)
        }catch (e : FirebaseAuthException){
            return AuthError(e.message ?: "Unknown Auth Error")
        }
        return null
    }

    override suspend fun logout() {
        firebaseAuth.signOut()
    }

    override suspend fun <T> callAuthenticated(f : suspend (userId : String)-> T?) : T? {
        val userId = firebaseAuth.currentUser?.uid
        if(userId != null){
            Log.e("AuthService", "Callin authenticated with user $userId")
            return f(userId)
        }
        else{
            Log.e("AuthService", "User is not authenticated")
            return null
        }
    }

    override suspend fun runAuthenticated(f : suspend (userId : String)-> Unit){
        val userId = firebaseAuth.currentUser?.uid
        if(userId != null){
            f(userId)
        }
        else{
            Log.e("AuthService", "User is not authenticated")
        }
    }
}