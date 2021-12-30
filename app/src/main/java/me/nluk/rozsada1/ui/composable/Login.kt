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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
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
import me.nluk.rozsada1.ui.theme.veryLightGray
import java.lang.Exception
import java.util.*
import javax.inject.Inject


@Composable fun RequireLogin(isLoggedIn : Boolean, composable : @Composable () -> Unit){
    if(isLoggedIn){
        composable()
    }
    else{
        LoginRegisterScreen()
    }
}

@Composable
fun LoginRegisterScreen(model : LoginScreenViewModel = hiltViewModel()){
    var tabState by remember { mutableStateOf(0) }
    val loginError = model.loginError.collectAsState().value
    val signupError = model.signupError.collectAsState().value
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
            modifier = Modifier.padding(top = 10.dp, bottom = 16.dp)
        )
        Divider(color = Purple700, thickness = 4.dp)
        val tabs = listOf(
            AuthTab(R.string.log_in){ AuthScreen(
                error = loginError,
                clearError =  model::clearLoginError,
                onAuthInputSubmit = { model.login(it as LoginInput) },
                buttonTextStringResId = R.string.log_in,
                isRegister = false
            ) },
            AuthTab(R.string.sign_up){ AuthScreen(
                error = signupError,
                clearError = model::clearSignupError,
                onAuthInputSubmit = { model.signUp(it as RegistrationInput)},
                buttonTextStringResId = R.string.sign_up,
                isRegister = true
            )}
        )
        TabRow(selectedTabIndex = tabState) {
            tabs.forEachIndexed { index, tab ->
                val selected = tabState == index
                Tab(
                    modifier = Modifier.background(color = if(selected) Purple500 else Purple700),
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

sealed class AuthInput

data class LoginInput(val username: String, val password: String) : AuthInput()

data class RegistrationInput(val username: String, val password: String, val firstName : String, val lastName : String) : AuthInput()

typealias OnAuthInputSubmit = (authInput : AuthInput) -> Unit

@Composable
fun AuthScreen(
    isRegister : Boolean,
    error: AuthError?,
    clearError : ()->Unit,
    onAuthInputSubmit: OnAuthInputSubmit,
    buttonTextStringResId : Int
){
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    TextField(
        modifier =Modifier.padding(start = 25.dp, end = 25.dp, top = 25.dp) ,
        value = username,
        label = { Text(stringResource(R.string.email)) },
        placeholder = { Text(stringResource(R.string.email)) },
        onValueChange = {
            clearError()
            username = it
        },
        colors = TextFieldDefaults.textFieldColors(backgroundColor = veryLightGray)
    )
    Spacer(modifier = Modifier.padding(vertical = 10.dp))
    TextField(
        value = password,
        singleLine = true,
        label = { Text(stringResource(R.string.password)) },
        placeholder = { Text(stringResource(R.string.password)) },
        visualTransformation = PasswordVisualTransformation(),
        onValueChange = {
            clearError()
            password = it
        },
        colors = TextFieldDefaults.textFieldColors(backgroundColor = veryLightGray)
    )

    if(isRegister){
        Spacer(modifier = Modifier.padding(vertical = 10.dp))
        TextField(
            value = firstName,
            singleLine = true,
            label = { Text(stringResource(R.string.first_name)) },
            placeholder = { Text(stringResource(R.string.first_name)) },
            onValueChange = {
                firstName = it
            },
            colors = TextFieldDefaults.textFieldColors(backgroundColor = veryLightGray)
            )
        Spacer(modifier = Modifier.padding(vertical = 10.dp))
        TextField(
            value = lastName,
            singleLine = true,
            label = { Text(stringResource(R.string.last_name)) },
            placeholder = { Text(stringResource(R.string.last_name)) },
            onValueChange = {
                lastName = it
            },
            colors = TextFieldDefaults.textFieldColors(backgroundColor = veryLightGray)
            )
    }
    Spacer(modifier = Modifier.padding(vertical = 10.dp))
    Button(
        enabled = username.isNotBlank() && password.isNotBlank() && (!isRegister || (firstName.isNotBlank() && lastName.isNotBlank())),
        onClick = { onAuthInputSubmit(if(isRegister) RegistrationInput(username, password, firstName, lastName) else LoginInput(username, password)) }
    ) {
        Text(text = stringResource(buttonTextStringResId))
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


@HiltViewModel
class LoginScreenViewModel @Inject constructor(
    private val authService: AuthService
) : ViewModel(){

    val loginError = MutableStateFlow<AuthError?>(null)
    val signupError = MutableStateFlow<AuthError?>(null)

    fun login(loginInput: LoginInput) = viewModelScope.launch(Dispatchers.IO) {
        loginError.value = authService.login(loginInput.username, loginInput.password)
    }

    fun signUp(registrationInput: RegistrationInput) = viewModelScope.launch(Dispatchers.IO) {
        with(registrationInput){
            signupError.value = authService.signUp(username, password, firstName, lastName)
        }
    }

    fun clearLoginError(){
        loginError.value = null
    }

    fun clearSignupError(){
        signupError.value = null
    }
}