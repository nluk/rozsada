package me.nluk.rozsada1.ui.composable

import android.text.format.DateUtils
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.rememberImagePainter
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.placeholder.PlaceholderHighlight
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.nluk.rozsada1.R
import me.nluk.rozsada1.lib.NextPage
import me.nluk.rozsada1.lib.isEven
import me.nluk.rozsada1.model.Offer
import me.nluk.rozsada1.services.AuthService
import me.nluk.rozsada1.services.FavouritesService
import me.nluk.rozsada1.services.OffersService
import me.nluk.rozsada1.ui.theme.primaryColor
import java.time.Instant
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshState
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import me.nluk.rozsada1.lib.ComposeStack
import me.nluk.rozsada1.model.SearchInput
import me.nluk.rozsada1.services.SearchService
import me.nluk.rozsada1.ui.theme.Purple700
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@ExperimentalPagerApi
@ExperimentalCoroutinesApi
@ExperimentalTime
@ExperimentalFoundationApi
@Composable
fun OffersScreen(model: OfferScreenViewModel = hiltViewModel()) {
    val recentOffers = model.recentOffers.collectAsState().value
    val searchOffers = model.searchOffers.collectAsState().value
    val search = model.search.collectAsState().value
    val favourites = model.favourites.collectAsState(emptySet()).value
    val authenticated = model.authenticated.collectAsState().value
    val initialLoad = model.initialLoad.value
    val isRefreshing = model.isRefreshing.collectAsState()
    val listState = rememberLazyListState()
    val likeOffer: (offerId: String) -> Unit = when (authenticated) {
        true -> model::toggleFavorite
        false -> {
            val context = LocalContext.current
            val toast: (String) -> Unit = {
                Toast.makeText(context, "You need to be logged in", Toast.LENGTH_SHORT).show()
            }
            toast
        }
    }
    val stack by remember { mutableStateOf(ComposeStack()) }
    val showDetails : (offerId : String) -> Unit = {
        stack.put {  OfferDetailsScreen(it, stack::clear) }
    }
    stack.root.value = {
        OffersScreenContent(
            if(search != null) searchOffers else recentOffers,
            favourites,
            likeOffer,
            showDetails,
            model::nextPage,
            search to { s -> model.setSearchText(s)},
            initialLoad,
            rememberSwipeRefreshState(isRefreshing.value),
            model::refresh,
            listState
        )
    }
    stack.RenderTop()
}


@ExperimentalFoundationApi
@Composable
fun OffersScreenContent(
    offers: List<Offer>,
    favourites: Set<String>,
    likeOffer: (offerId: String) -> Unit,
    showDetails: (offerId: String) -> Unit,
    nextPage: (offer: Offer) -> Unit,
    offerSearch : Pair<SearchInput?, (String) -> Unit>,
    initialLoad: Boolean,
    isRefreshing : SwipeRefreshState,
    refresh : () -> Unit,
    listState: LazyListState
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Card(modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()) {
            Column {
                TextField(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    value = offerSearch.first?.text ?: "",
                    onValueChange = { value -> offerSearch.second(value) },
                    colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.Transparent),
                    placeholder = { Text(stringResource(R.string.search_offer)) }
                )
            }
        }
        SwipeRefresh(state = isRefreshing, onRefresh = refresh) {
            LazyVerticalGrid(
                modifier = Modifier.padding(horizontal = 4.dp),
                cells = GridCells.Fixed(2),
                state = listState
            ) {
                if (offerSearch.first != null) {
                    items(offers) { offer: Offer ->
                        OfferCard(offer, offer.id in favourites, likeOffer, showDetails)
                    }
                } else {
                    items(offers) { offer: Offer ->
                        nextPage(offer)
                        OfferCard(offer, offer.id in favourites, likeOffer, showDetails)
                    }
                }
                if ((initialLoad || isRefreshing.isRefreshing) && offerSearch.first == null) {
                    items(8) {
                        OfferCardPlaceholder()
                    }
                }
                items(if (offers.size.isEven) 1 else 2) {
                    Spacer(modifier = Modifier.padding(vertical = 25.dp))
                }
            }
        }
    }
}

@Composable
fun OfferCardPlaceholder(){
    Card(
        Modifier
            .fillMaxWidth()
            .padding(start = 3.dp, end = 3.dp, top = 4.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = 12.dp
    ) {
        Column {
            Image(
                painter = painterResource(id = R.drawable.offer_image_placeholder),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(128.dp),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier.padding(5.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        modifier = Modifier
                            .weight(4.0f)
                            .placeholder(
                                visible = true,
                                highlight = PlaceholderHighlight.shimmer(),
                            ),
                        fontSize = 14.sp,
                        text = "PLACEHOLDER",
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    IconButton(
                        modifier = Modifier.then(Modifier.size(24.dp)),
                        onClick = { }
                    ) {
                        Icon(
                            Icons.Filled.FavoriteBorder,
                            null,
                            modifier = Modifier.weight(1.0f),
                        )
                    }
                }
                Text(
                    modifier = Modifier.placeholder(
                        visible = true,
                        highlight = PlaceholderHighlight.shimmer()
                    ),
                    text = "PLACEHOLDER",
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.padding(vertical = 10.dp))
                Text(
                    text = "",
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    fontWeight = FontWeight.Light
                )
                Text(
                    modifier = Modifier.placeholder(
                        visible = true,
                        highlight = PlaceholderHighlight.shimmer()
                    ),
                    text = "PLACEHOLDER",
                    fontSize = 12.sp,
                    color = Color.DarkGray,
                    fontWeight = FontWeight.Light
                )
            }
        }
    }
}

@Composable
fun OfferCard(
    offer: Offer,
    userFavourite: Boolean,
    likeOffer: (offerId: String) -> Unit,
    showDetails : (offerId : String) -> Unit
) {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(start = 3.dp, end = 3.dp, top = 4.dp)
            .clickable { showDetails(offer.id!!) },
        shape = RoundedCornerShape(12.dp),
        elevation = 12.dp
    ) {
        Column {
            OfferImage(dataUrl = offer.images.first())
            Column(
                modifier = Modifier.padding(5.dp)
            ) {
                OfferHeader(
                    offer.id!!,
                    title = offer.title,
                    favorite = userFavourite,
                    likeOffer = likeOffer
                )
                if (offer.points != null) {
                    OfferPoints(offer.points)
                } else {
                    OfferPrice(price = offer.price!!, Currency.getInstance("PLN"))
                }
                Spacer(modifier = Modifier.padding(vertical = 10.dp))
                OfferTimeLoc(created = offer.createdAt, location = offer.city)
            }
        }
    }
}

@Composable
fun OfferPrice(price: Double, currency: Currency) {
    Text(
        text = "%.2f ${currency.currencyCode}".format(price),
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun OfferPoints(points: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = points.toString(),
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.padding(end = 5.dp))
        Text(
            text = stringResource(id = R.string.leaf_points),
            fontWeight = FontWeight.Bold,
            color = primaryColor
        )
        Spacer(modifier = Modifier.padding(end = 5.dp))
        Image(
            painter = painterResource(R.drawable.ic_leaf),
            contentDescription = "Points leaf icon",
            modifier = Modifier.fillMaxHeight()
        )
    }
}

@Composable
fun OfferImage(dataUrl: String, height : Dp = 128.dp) {
    Image(
        painter = rememberImagePainter(
            data = dataUrl,
            builder = {
                crossfade(true)
                placeholder(R.drawable.offer_image_placeholder)
            }
        ),
        contentDescription = null,
        modifier = Modifier
            .fillMaxWidth()
            .height(height),
        contentScale = ContentScale.Crop
    )
}

@Composable
fun OfferHeader(
    id: String,
    title: String,
    favorite: Boolean,
    likeOffer: (offerId: String) -> Unit,
    horizontalPadding : Dp = 0.dp
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = horizontalPadding),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            modifier = Modifier.weight(4.0f),
            fontSize = 14.sp,
            text = title,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        IconButton(
            modifier = Modifier.then(Modifier.size(24.dp)),
            onClick = { likeOffer(id) }
        ) {
            Icon(
                if (favorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                null,
                modifier = Modifier.weight(1.0f),
                tint = Purple700
            )
        }
    }
}

@Composable
fun OfferTimeLoc(created: Instant, location: String) {
    Text(
        text = location,
        fontSize = 14.sp,
        color = Color.DarkGray,
        fontWeight = FontWeight.Light
    )
    Text(
        text = DateUtils.getRelativeTimeSpanString(created.toEpochMilli()).toString(),
        fontSize = 12.sp,
        color = Color.DarkGray,
        fontWeight = FontWeight.Light
    )
}

@ExperimentalTime
@HiltViewModel
class OfferScreenViewModel @Inject constructor(
    private val offersService: OffersService,
    private val favouritesService: FavouritesService,
    private val authService: AuthService,
    private val searchService: SearchService
) : ViewModel() {

    private val _recentOffers = MutableStateFlow<List<Offer>>(emptyList())

    private val _searchOffers = MutableStateFlow<List<Offer>>(emptyList())

    val recentOffers: StateFlow<List<Offer>> = _recentOffers

    val searchOffers : StateFlow<List<Offer>> = _searchOffers

    var favourites: Flow<Set<String>> = emptyFlow()

    var page = NextPage.of<String>(null, 10L)

    val initialLoad = mutableStateOf(true)

    val search = MutableStateFlow<SearchInput?>(null)

    val authenticated = authService.authenticationStatusFlow()

    val isRefreshing = MutableStateFlow(false)

    val scrollState = mutableStateListOf(0)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _recentOffers.value = offersService.getRecentOffers(NextPage.of(null, 20L))
            favourites = favouritesService.favouriteOfferIds()
            initialLoad.value = false
            searchService.getOffers(validSearchInput()).flowOn(Dispatchers.IO).collectLatest {
                _searchOffers.value = it
            }
        }
    }

    fun refresh() = viewModelScope.launch(Dispatchers.IO){
        if(!isRefreshing.value){
            isRefreshing.value = true
            val searchValue = search.value
            if(searchValue != null){
                _searchOffers.value = searchService.getOffers(searchValue)
            }else{
                _recentOffers.value = emptyList()
                delay(1000L)
                _recentOffers.value = offersService.getRecentOffers(NextPage.of(null, 20L))
                page = NextPage.of(null, 10L)
            }
            isRefreshing.value = false
        }
    }

    fun nextPage(offer: Offer) = viewModelScope.launch(Dispatchers.IO) {
        if (!(initialLoad.value || offer.id != recentOffers.value.last().id)) {
            page = NextPage.of(recentOffers.value.last().createdAt.toString(), 20L)
            val newOffers = offersService.getRecentOffers(page)
            appendOffers(newOffers)
        }
    }

    private fun appendOffers(newOffers: List<Offer>) {
        val totalOffers = ArrayList<Offer>(newOffers.size + recentOffers.value.size)
        totalOffers.addAll(recentOffers.value)
        totalOffers.addAll(newOffers)
        _recentOffers.value = totalOffers
    }

    fun toggleFavorite(offerId: String) = viewModelScope.launch(Dispatchers.IO) {
        if (offerId in favourites.firstOrNull() ?: emptySet()) {
            Log.e("Offers", "Removing offer $offerId")
            favouritesService.removeFavourite(offerId)
        } else {
            Log.e("Offers", "Marking offer $offerId")
            favouritesService.markFavourite(offerId)
        }
    }

    @ExperimentalTime
    fun validSearchInput() = search.filterNotNull().filter { it.text.length >= 3 }.debounce(Duration.milliseconds(500))

    fun setSearchText(newSearchText : String?){
        _searchOffers.value = emptyList()
        if(newSearchText.isNullOrBlank()){
            search.value = null
        }
        else{
            search.value = search.value?.copy(text = newSearchText) ?: SearchInput(newSearchText)
        }
    }

    fun setSearchCategory(categoryId : Int?){
        search.value = search.value?.copy(categoryId = categoryId)
    }
}