package me.nluk.rozsada1.ui.composable

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import me.nluk.rozsada1.R

@Composable
fun SignUp(){
    Text(text = stringResource(id = R.string.sign_up))
}