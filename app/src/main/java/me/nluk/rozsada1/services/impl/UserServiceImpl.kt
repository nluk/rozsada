package me.nluk.rozsada1.services.impl

import dev.gitlive.firebase.firestore.CollectionReference
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import me.nluk.rozsada1.model.User
import me.nluk.rozsada1.services.AuthService
import me.nluk.rozsada1.services.UserService
import javax.inject.Inject

@ExperimentalCoroutinesApi
class UserServiceImpl @Inject constructor(
    val firestore: FirebaseFirestore,
    val authService: AuthService
) : UserService{
    companion object {
        private const val COLLECTION = "users"
    }

    override suspend fun currentUser() : Flow<User?> = authService.authUserFlow().mapLatest { firebaseUser ->  firebaseUser?.uid }.transform {
        if(it == null){
            emit(null)
        }
        else{
            emitAll(user(it).snapshots.mapLatest { it.data() as User })
        }
    }

    private fun users() : CollectionReference = firestore.collection(COLLECTION)

    private fun user(userId: String) = users().document(userId)
}

