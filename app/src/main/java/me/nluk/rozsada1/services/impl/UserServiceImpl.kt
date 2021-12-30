package me.nluk.rozsada1.services.impl

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.tylerthrailkill.helpers.prettyprint.pp
import dagger.Lazy
import dev.gitlive.firebase.firestore.CollectionReference
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.flow.*
import kotlinx.serialization.InternalSerializationApi
import me.nluk.rozsada1.model.User
import me.nluk.rozsada1.services.AuthService
import me.nluk.rozsada1.services.ImageService
import me.nluk.rozsada1.services.UserService
import javax.inject.Inject

@ExperimentalCoroutinesApi
class UserServiceImpl @Inject constructor(
    val firestore: FirebaseFirestore,
    val authService: Lazy<AuthService>,
    val imageService: ImageService
) : UserService{
    companion object {
        private const val COLLECTION = "users"
    }

    private val userServiceScope = CoroutineScope(Dispatchers.IO)
    val currentUser : MutableStateFlow<User?> = MutableStateFlow(null)


    @InternalSerializationApi
    private val userStateJob = userServiceScope.launch {
        authService.get().authUserFlow().collectLatest { firebaseUser ->
            if(firebaseUser?.uid == null){
                Log.d("UserServiceImpl", "Uid is null")
                currentUser.value = null
            }
            else{
                while (!user(firebaseUser.uid).get().exists){
                    delay(200L)
                    Log.d("UserServiceImpl", "No user")
                }
                Log.d("UserServiceImpl", "Got User!")
                user(firebaseUser.uid).snapshots.collectLatest {
                    currentUser.value = (it.data() as User).copy(id = it.id)
                }
            }
        }
    }

    override fun currentUser() : StateFlow<User?> = currentUser

    fun users() : CollectionReference = firestore.collection(COLLECTION)

    override fun user(userId: String): dev.gitlive.firebase.firestore.DocumentReference = users().document(userId)

    override fun changeAvatar(context : Context, avatarUri: Uri){
        userServiceScope.launch{
            val userId = currentUser.value!!.id
            val avatarUrl = imageService.uploadImage(context, avatarUri, "users/$userId")
            println("Avatar url: $avatarUrl")
            user(userId).update("avatar" to avatarUrl)
        }
    }
}

