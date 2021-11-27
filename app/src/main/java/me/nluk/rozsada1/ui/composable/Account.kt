package me.nluk.rozsada1.ui.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.nluk.rozsada1.R
import me.nluk.rozsada1.model.User
import me.nluk.rozsada1.services.AuthService
import me.nluk.rozsada1.services.UserService
import me.nluk.rozsada1.ui.theme.Teal200
import javax.inject.Inject

@Composable
fun Account(
    accountScreenViewModel: AccountScreenViewModel = hiltViewModel()
){
    val user = accountScreenViewModel.user.collectAsState().value
    val authenticated = accountScreenViewModel.authenticated.collectAsState().value
    if(authenticated){
        Column(
            modifier = Modifier.fillMaxSize()
        ){
            TitleText(text = stringResource(R.string.your_account))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.Center
            ){
                Image(
                    painter = rememberImagePainter(
                        data = user?.avatar,
                        builder = {
                            crossfade(true)
                            placeholder(R.drawable.preview_plant)
                        }
                    ),
                    contentDescription = "avatar",
                    contentScale = ContentScale.Crop,            // crop the image if it's not a square
                    modifier = Modifier
                        .size(128.dp)
                        .clip(CircleShape)                       // clip to the circle shape
                        .border(2.dp, Teal200, CircleShape)   // add a border (optional)
                )
            }
            Button(onClick = accountScreenViewModel::logOut ) {
                Text(text = "Log out")
            }
        }
    }else{
        LoginRegisterScreen()
    }
}

@HiltViewModel
class AccountScreenViewModel @Inject constructor(
    private val userService : UserService,
    private val authService: AuthService
) : ViewModel() {
    val user : MutableStateFlow<User?> = MutableStateFlow(null)
    init {
        viewModelScope.launch(Dispatchers.IO) {
            userService.currentUser().flowOn(Dispatchers.IO).collect{
                user.value = it
            }
        }
    }

    val authenticated = authService.authenticationStatusFlow()

    fun logOut() = viewModelScope.launch(Dispatchers.IO) {
        authService.logout()
    }
}