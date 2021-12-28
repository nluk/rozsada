package me.nluk.rozsada1.ui.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
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
    accountScreenViewModel: AccountScreenViewModel = hiltViewModel()
) = RequireLogin(accountScreenViewModel.authenticated.collectAsState().value) {
    val user = accountScreenViewModel.user.collectAsState().value

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
                        placeholder(R.drawable.preview_plant)
                    }
                )
            } ?: rememberImagePainter(R.drawable.offer_image_placeholder)
            Image(
                painter = imagePainter,
                contentDescription = "avatar",
                contentScale = ContentScale.Crop,            // crop the image if it's not a square
                modifier = Modifier
                    .size(128.dp)
                    .clip(CircleShape)                       // clip to the circle shape
                    .border(2.dp, Teal200, CircleShape)   // add a border (optional)
            )
        }
        user?.run {
            Text(text = "Hi ${openid?.givenName ?: email}!")
        }
        Button(onClick = accountScreenViewModel::logOut) {
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
}