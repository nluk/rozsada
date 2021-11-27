package me.nluk.rozsada1.ui.composable

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import me.nluk.rozsada1.R
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.gitlive.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import me.nluk.rozsada1.services.AuthError
import me.nluk.rozsada1.services.AuthService
import me.nluk.rozsada1.services.impl.AuthServiceImpl
import me.nluk.rozsada1.ui.theme.Purple200
import me.nluk.rozsada1.ui.theme.Purple500
import me.nluk.rozsada1.ui.theme.Purple700
import java.lang.Exception
import java.util.*
import javax.inject.Inject

@Composable
fun LoginRegisterScreen(model : LoginScreenViewModel = hiltViewModel()){
    var tabState by remember { mutableStateOf(0) }
    Surface(
        modifier = Modifier
            .padding(vertical = 10.dp, horizontal = 10.dp)
            .fillMaxSize()
            .shadow(elevation = 16.dp, shape = RoundedCornerShape(12.dp))
            .background(Color.White, shape = RoundedCornerShape(12.dp))
            .padding(12.dp)
    ){
    Column(
        modifier = Modifier
            .background(color = Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
    ){

        Image(
            painterResource(R.drawable.rozsada_logo),
            "Rozsada logo",
            modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)
        )
        Divider(color = Purple700, thickness = 4.dp)
        val tabs = listOf(
            AuthTab(R.string.log_in){ LoginScreen(model) },
            AuthTab(R.string.sign_up){ SignUp() }
        )
        TabRow(selectedTabIndex = tabState) {
            tabs.forEachIndexed { index, tab ->
                val selected = tabState == index
                Tab(
                    modifier = Modifier.background(color = if(selected) Purple200 else Purple500),
                    text = {
                        Text(stringResource(id = tab.titleId))
                    },
                    selected = selected,
                    onClick = { tabState = index }
                )
            }
        }
        tabs[tabState].content()
    }
    }
}

@Composable
fun LoginScreen(model : LoginScreenViewModel){
    val error = model.error.collectAsState().value
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    TextField(
        modifier =Modifier.padding(start = 25.dp, end = 25.dp, top = 25.dp) ,
        value = username,
        onValueChange = {
            model.clearError()
            username = it
        }
    )
    Spacer(modifier = Modifier.padding(vertical = 10.dp))
    TextField(value = password, onValueChange = {
        model.clearError()
        password = it
    })
    Spacer(modifier = Modifier.padding(vertical = 10.dp))
    LoginButton(enabled = username.isNotBlank() && password.isNotBlank()) {
        model.login(username, password)
    }
    error?.let {
        ErrorInfo(error)
    }
}

@Composable
fun ErrorInfo(authError: AuthError){
    Surface(
        modifier = Modifier
            .padding(10.dp)
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(4.dp))
            .background(Color.White, shape = RoundedCornerShape(12.dp))
            //.border(width = 1.dp, color = Color.Red, shape = RectangleShape)
            .padding(6.dp)

    ) {
        Text(
            text = authError.error,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            fontSize = 12.sp
        )
    }
}



@Preview
@Composable
fun ErrorInfoPreview(){
    ErrorInfo(authError = AuthError("Some error"))
}

@Composable
fun LoginButton(enabled : Boolean, onClick : () -> Unit){
    Button(
        enabled = enabled,
        onClick = onClick
    ) {
        Text(text = stringResource(R.string.log_in))
    }
}

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
@Preview
fun LoginScreenPreview(){

    val authService = object : AuthService{
        override fun authenticationStatusFlow(): StateFlow<Boolean> {
            TODO("Not yet implemented")
        }

        override fun authUserFlow(): StateFlow<FirebaseUser?> {
            TODO("Not yet implemented")
        }

        override fun isAuthenticated(): Boolean {
            TODO("Not yet implemented")
        }

        override suspend fun login(username: String, password: String): AuthError? {
            TODO("Not yet implemented")
        }

        override suspend fun logout() {
            TODO("Not yet implemented")
        }

        override suspend fun <T> callAuthenticated(f: suspend (userId: String) -> T?): T? {
            TODO("Not yet implemented")
        }

        override suspend fun runAuthenticated(f: suspend (userId: String) -> Unit) {
            TODO("Not yet implemented")
        }
    }
    val model = LoginScreenViewModel(authService)
    model.error.value = AuthError("Some error ********* ******** **** ***")
    LoginScreen(model)
}


@HiltViewModel
class LoginScreenViewModel @Inject constructor(
    private val authService: AuthService
) : ViewModel(){

    val error = MutableStateFlow<AuthError?>(null)

    fun login(username : String, password : String) = viewModelScope.launch(Dispatchers.IO) {
        error.value = authService.login(username, password)
    }

    fun clearError(){
        error.value = null
    }
}