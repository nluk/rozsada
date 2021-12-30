package me.nluk.rozsada1.ui.composable

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.nluk.rozsada1.R
import me.nluk.rozsada1.lib.ComposeStack
import me.nluk.rozsada1.lib.isEven
import me.nluk.rozsada1.model.Offer
import me.nluk.rozsada1.services.AuthService
import me.nluk.rozsada1.services.FavouritesService
import me.nluk.rozsada1.services.impl.FavouritesServiceImpl
import me.nluk.rozsada1.ui.theme.grey
import javax.inject.Inject


@ExperimentalCoroutinesApi
@ExperimentalPagerApi
@ExperimentalFoundationApi
@Composable
fun FavouritesScreen(
    model : FavouritesScreenViewModel = hiltViewModel()
) =  RequireLogin(model.authenticated.collectAsState().value) {
    val favourites = model.favourites.collectAsState().value
    val stack by remember { mutableStateOf(ComposeStack()) }
    val showDetails : (offerId : String) -> Unit = {
        stack.put {  OfferDetailsScreen(it, stack::clear) }
    }
    stack.root.value = {
        FavouritesScreenContent(favourites, model::removeFavourite, showDetails)
    }
    stack.RenderTop()
}

@ExperimentalFoundationApi
@Composable
fun FavouritesScreenContent(favourites : List<Offer>, removeFavourite: (String) -> Unit, showDetails : (String) -> Unit){
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TitleText(text = listOf(stringResource(id = R.string.your), stringResource(id = R.string.favourites)).joinToString(" "))
        if(favourites.isNullOrEmpty()){
            Column(modifier = Modifier
                .padding(top = 16.dp, bottom = 16.dp)
                .fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text(text = stringResource(R.string.nothing_here), color = grey, fontSize = 16.sp)
                Image(
                    painterResource(R.drawable.ic_plant_in_pot),
                    "Plant in a pot",
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        else{
            FavouriteOffersGrid(offers = favourites, removeFavourite = removeFavourite, offerClick = showDetails)
        }
    }
}

@ExperimentalFoundationApi
@Composable
fun FavouriteOffersGrid(
    offers: List<Offer>,
    removeFavourite: (String) -> Unit,
    offerClick : (String) -> Unit
){
    LazyVerticalGrid(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .fillMaxSize(),
        cells = GridCells.Fixed(2)
    ){
        items(offers){
            OfferCard(it, true, removeFavourite, { offerClick(it) })
        }
        for (i in 0..(if (offers.size.isEven) 1 else 2)) {
            item {
                Spacer(modifier = Modifier.padding(vertical = 25.dp))
            }
        }
    }
}


@HiltViewModel
class FavouritesScreenViewModel @Inject constructor(
    private val favouritesService: FavouritesService,
    authService: AuthService
) : ViewModel() {

    val favourites : MutableStateFlow<List<Offer>> = MutableStateFlow(emptyList())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            favouritesService.favouriteOffers().flowOn(Dispatchers.IO).collect {
                favourites.value = it
            }
        }
    }

    val authenticated = authService.authenticationStatusFlow()

    fun removeFavourite(offerId : String) = viewModelScope.launch(Dispatchers.IO) {
        Log.e("Favourites", "Removing offer $offerId")
        favouritesService.removeFavourite(offerId)
    }
}