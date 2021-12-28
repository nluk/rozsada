package me.nluk.rozsada1.ui.composable

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.pager.ExperimentalPagerApi
import me.nluk.rozsada1.ui.theme.*

@ExperimentalPagerApi
@ExperimentalFoundationApi
@Composable
fun RozsadaApp(){
    val navController = rememberNavController()
    AppTheme{
        Scaffold(
            bottomBar = { BottomNavigationBar(navController) },
            content = { innerPadding: PaddingValues ->
                NavHostContent(innerPadding, navController)
            }
        )
    }
}

@ExperimentalPagerApi
@ExperimentalFoundationApi
@Composable
fun NavHostContent(innerPadding: PaddingValues, navController: NavHostController){
    NavHost(
        navController,
        startDestination = ScreenNavigation.Offers.route,
        Modifier
            .padding(innerPadding)
            .background(veryLightGray)
    ) {
        composable(ScreenNavigation.Offers.route) { OffersScreen() }
        composable(ScreenNavigation.CreateOffer.route) { AddOfferScreen() }
        composable(ScreenNavigation.Chats.route) { ChatScreen() }
        composable(ScreenNavigation.Favourites.route) { FavouritesScreen() }
        composable(ScreenNavigation.Account.route) { Account() }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController){
    BottomNavigation {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val isRouteSelected : (String) -> Boolean = { route ->
            navBackStackEntry?.destination?.hierarchy?.any { it.route == route } == true
        }
        ScreenNavigation.ALL_SCREEN_NAVIGATIONS.forEach { screen ->
            val selected = isRouteSelected(screen.route)
            val selectionBasedColor = if(selected) primaryDarkColor else grey
            BottomNavigationItem(
                modifier = Modifier.background(Color.White),
                icon = {
                    Icon(
                        imageVector = screen.icon(selected),
                        contentDescription = null,
                        tint = selectionBasedColor
                    )
                },
                label = {
                    Text(
                        maxLines = 1,
                        text = stringResource(screen.resourceId),
                        color =selectionBasedColor,
                        fontSize = 10.sp,
                    )
                },
                selected = selected,
                onClick = {
                    navController.navigate(screen.route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        // on the back stack as users select items
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Avoid multiple copies of the same destination when
                        // reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                }
            )
        }
    }
}

