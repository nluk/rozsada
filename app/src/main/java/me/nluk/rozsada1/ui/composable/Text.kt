package me.nluk.rozsada1.ui.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.nluk.rozsada1.R

@Composable
fun TitleText(text : String){
    Text(
        text = text,
        fontSize = 24.sp,
        fontWeight = FontWeight.Light,
        modifier = Modifier
            .defaultMinSize(minHeight = 48.dp)
            .fillMaxWidth()
            .background(Color.White)
            .padding(25.dp),
        textAlign = TextAlign.Center
    )
}