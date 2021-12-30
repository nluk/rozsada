package me.nluk.rozsada1.ui.composable

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.rememberImagePainter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.nluk.rozsada1.R
import me.nluk.rozsada1.services.AuthService
import me.nluk.rozsada1.services.UserService
import me.nluk.rozsada1.ui.theme.Teal200
import javax.inject.Inject

@Composable
fun Account(
    model: AccountScreenViewModel = hiltViewModel()
) = RequireLogin(model.authenticated.collectAsState().value) {
    val user = model.user.collectAsState().value
    val context = LocalContext.current
    val addImagesLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.also {
            model.changeAvatar(context, it)
        }
    }
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        ColumnCard(horizontalArrangement = Alignment.CenterHorizontally) {
            TitleText(text = stringResource(R.string.your_account))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                val imagePainter = user?.avatar?.let {
                    rememberImagePainter(
                        data = it,
                        builder = {
                            crossfade(true)
                            placeholder(R.drawable.offer_image_placeholder)
                        }
                    )
                } ?: rememberImagePainter(R.drawable.offer_image_placeholder)
                Image(
                    painter = imagePainter,
                    contentDescription = "avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(128.dp)
                        .clip(CircleShape)
                        .border(2.dp, Teal200, CircleShape)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = {
                                    addImagesLauncher.launch("image/*")
                                }
                            )
                        }
                )
            }
            user?.run {
                Text(text = stringResource(R.string.hi) + " ${openid?.givenName ?: email}!")
            }
        }
        user?.run {
            ColumnCard(horizontalArrangement = Alignment.CenterHorizontally) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = stringResource(R.string.active_offers))
                    Text(text = offerStats.inProgress.toString())
                }
            }
            ColumnCard(horizontalArrangement = Alignment.CenterHorizontally) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = stringResource(R.string.finished_offers))
                    Text(text = offerStats.finished.toString())
                }
            }
        }

        Button(onClick = model::logOut) {
            Text(text = "Log out")
        }
    }
}

@HiltViewModel
class AccountScreenViewModel @Inject constructor(
    private val userService: UserService,
    private val authService: AuthService
) : ViewModel() {
    val user = userService.currentUser()
    val authenticated = authService.authenticationStatusFlow()

    fun logOut() = viewModelScope.launch(Dispatchers.IO) {
        authService.logout()
    }

    fun changeAvatar(context : Context, uri : Uri){
        userService.changeAvatar(context, uri)
    }
}