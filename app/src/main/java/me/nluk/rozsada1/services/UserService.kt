package me.nluk.rozsada1.services

import android.content.Context
import android.net.Uri
import dev.gitlive.firebase.firestore.DocumentReference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import me.nluk.rozsada1.model.User

interface UserService {
    fun currentUser() : StateFlow<User?>
    fun user(userId : String) : DocumentReference
    fun changeAvatar(context : Context, avatarUri: Uri)
}