package me.nluk.rozsada1.lib

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import java.util.*

typealias FCompose =  @Composable () -> Unit

class ComposeStack() {
    var root  = mutableStateOf<FCompose?>(null)
    val stack = mutableStateListOf<FCompose>()

    @Composable
    fun RenderTop(){
        if(stack.isEmpty()){
            root.value!!()
        }
        else{
            stack.last()()
        }
    }

    fun clear() = stack.clear()

    fun put(compose : FCompose) = stack.add(compose)

    fun pop() = stack.removeLast()

    fun isEmpty() = stack.isEmpty()
}