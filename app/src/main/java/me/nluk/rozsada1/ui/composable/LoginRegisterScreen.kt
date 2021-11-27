package me.nluk.rozsada1.ui.composable


import androidx.compose.runtime.*
data class AuthTab(
    val titleId : Int,
    val content : @Composable () -> Unit
)