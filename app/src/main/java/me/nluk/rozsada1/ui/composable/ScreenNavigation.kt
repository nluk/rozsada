package me.nluk.rozsada1.ui.composable

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector
import me.nluk.rozsada1.R

sealed class ScreenNavigation(
    val route: String,
    @StringRes val resourceId: Int,
    private val selectedIcon : ImageVector,
    private val icon : ImageVector
) {

    companion object{
        val ALL_SCREEN_NAVIGATIONS : List<ScreenNavigation> = listOf(Offers, Favourites, CreateOffer, Chats, Account)
    }

    object Offers : ScreenNavigation("offers", R.string.offers, Icons.Rounded.Yard, Icons.Outlined.Yard)
    object Account : ScreenNavigation("account", R.string.account, Icons.Rounded.Person, Icons.Outlined.PersonOutline)
    object Favourites : ScreenNavigation("favourites", R.string.favourites, Icons.Rounded.Favorite, Icons.Outlined.FavoriteBorder)
    object CreateOffer : ScreenNavigation("create_offer", R.string.create_offer, Icons.Rounded.AddCircle, Icons.Outlined.AddCircleOutline)
    object Chats : ScreenNavigation("chats", R.string.chats, Icons.Rounded.ChatBubble, Icons.Outlined.ChatBubbleOutline)

    fun icon(selected : Boolean) = if(selected) selectedIcon else icon
}