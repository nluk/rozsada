package me.nluk.rozsada1.ui.composable

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.ChangeCircle
import androidx.compose.ui.graphics.vector.ImageVector
import me.nluk.rozsada1.R

sealed class ScreenNavigation(val route: String, @StringRes val resourceId: Int, val icon : ImageVector) {

    companion object{
        val ALL_SCREEN_NAVIGATIONS : List<ScreenNavigation> = listOf(Barter, Buy, Account)
    }

    object Barter : ScreenNavigation("barter", R.string.barter, Icons.Filled.ChangeCircle)
    object Buy : ScreenNavigation("buy", R.string.buy, Icons.Filled.AttachMoney)
    object Account : ScreenNavigation("account", R.string.account, Icons.Filled.AccountCircle)
}