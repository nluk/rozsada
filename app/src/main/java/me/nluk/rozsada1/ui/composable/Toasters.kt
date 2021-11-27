package me.nluk.rozsada1.ui.composable

import android.content.Context
import android.widget.Toast

fun toaster(context : Context) : (String) -> (() -> Unit){
    return {
        text -> {
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
        }
    }
}