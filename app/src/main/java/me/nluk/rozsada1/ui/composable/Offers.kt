package me.nluk.rozsada1.ui.composable

import android.text.format.DateUtils
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.rememberImagePainter
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.fade
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
import java.util.UUID.randomUUID
import javax.inject.Inject
import kotlin.collections.ArrayList
import com.google.accompanist.placeholder.material.placeholder

@ExperimentalFoundationApi
@Composable
fun OffersScreen(model: OfferScreenViewModel = hiltViewModel()) {
    val offers = model.offers.collectAsState().value
    val favourites = model.favourites.collectAsState(emptySet()).value
    val authenticated = model.authenticated.collectAsState().value
    val initialLoad = model.initialLoad.value
    val offerClick: (offerId: String) -> Unit = when (authenticated) {
        true -> model::toggleFavorite
        false -> {
            val context = LocalContext.current
            val toast: (String) -> Unit = {
                Toast.makeText(context, "You need to be logged in", Toast.LENGTH_SHORT).show()
            }
            toast
        }
    }
    OffersScreenContent(
        offers,
        favourites,
        offerClick,
        model::nextPage,
        initialLoad
    )
}

@ExperimentalFoundationApi
@Preview(showBackground = true)
@Composable
fun OffersScreenPreview() {
    val firstId = randomUUID().toString()
    OffersScreenContent(
        offers = listOf(
            Offer(
                userId = randomUUID().toString(),
                id = firstId,
                title = "Title",
                images = listOf(
                    "https://res.cloudinary.com/patch-gardens/image/upload/c_fill,f_auto,h_400,q_auto:good,w_400/v1519993399/products/boston-fern-42361f.jpg",
                ),
                price = 12.0,
                createdAt = Instant.now(),
                description = "A plant",
                city = "Koluszki"
            ),
            Offer(
                userId = randomUUID().toString(),
                id = randomUUID().toString(),
                title = "Title",
                images = listOf(
                    "https://res.cloudinary.com/patch-gardens/image/upload/c_fill,f_auto,h_400,q_auto:good,w_400/v1565100347/products/dracaena-fragrans-8939ef.jpg",
                ),
                points = 100,
                createdAt = Instant.now(),
                description = "A plant",
                city = "Łódź"
            ),
            Offer(
                userId = randomUUID().toString(),
                id = randomUUID().toString(),
                title = "Title",
                images = listOf(
                    "https://res.cloudinary.com/patch-gardens/image/upload/c_fill,f_auto,h_400,q_auto:good,w_400/v1630681968/sisao6j1iih1krdubqou.jpg",
                ),
                price = 12.0,
                createdAt = Instant.now(),
                description = "A plant",
                city = "Zgierz"
            ),
        ),
        favourites = setOf(firstId),
        {},
        {},
        false
    )
}

@ExperimentalFoundationApi
@Composable
fun OffersScreenContent(
    offers: List<Offer>,
    favourites: Set<String>,
    offerClick: (offerId: String) -> Unit,
    nextPage: (offer: Offer) -> Unit,
    initialLoad: Boolean
) {
    LazyVerticalGrid(
        modifier = Modifier.padding(horizontal = 4.dp),
        cells = GridCells.Fixed(2)
    ) {
        items(offers) { offer: Offer ->
            nextPage(offer)
            OfferCard(offer, offer.id in favourites, offerClick)
        }
        if(initialLoad){
            items(8) {
                OfferCardPlaceholder()
            }
        }
        items(if (offers.size.isEven) 1 else 2){
            Spacer(modifier = Modifier.padding(vertical = 25.dp))
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
                                highlight = PlaceholderHighlight.fade(),
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
                        highlight = PlaceholderHighlight.fade()
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
                        highlight = PlaceholderHighlight.fade()
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
    offerClick: (offerId: String) -> Unit
) {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(start = 3.dp, end = 3.dp, top = 4.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = 12.dp
    ) {
        Column {
            OfferImage(dataUrl = offer.images.first())
            Column(
                modifier = Modifier.padding(5.dp)
            ) {
                OfferHeader(
                    offer.id,
                    title = offer.title,
                    favorite = userFavourite,
                    offerClick = offerClick
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
fun OfferImage(dataUrl: String) {
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
            .height(128.dp),
        contentScale = ContentScale.Crop
    )
}

@Composable
fun OfferHeader(
    id: String,
    title: String,
    favorite: Boolean,
    offerClick: (offerId: String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
            onClick = { offerClick(id) }
        ) {
            Icon(
                if (favorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                null,
                modifier = Modifier.weight(1.0f),
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

@HiltViewModel
class OfferScreenViewModel @Inject constructor(
    private val offersService: OffersService,
    private val favouritesService: FavouritesService,
    private val authService: AuthService
) : ViewModel() {

    private val _offers = MutableStateFlow<List<Offer>>(emptyList())
    val offers: StateFlow<List<Offer>>
        get() = _offers

    var favourites: Flow<Set<String>> = emptyFlow()

    var page = NextPage.of<String>(null, 10L)

    val initialLoad = mutableStateOf(true)

    val authenticated = authService.authenticationStatusFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            _offers.value = offersService.getRecentOffers(NextPage.of(null, 20L))
            favourites = favouritesService.favouriteOfferIds()
            initialLoad.value = false
        }
    }

    fun nextPage(offer: Offer) = viewModelScope.launch(Dispatchers.IO) {
        if (!(initialLoad.value || offer.id != offers.value.last().id)) {
            page = NextPage.of(offers.value.last().createdAt.toString(), 20L)
            val newOffers = offersService.getRecentOffers(page)
            appendOffers(newOffers)
        }
    }

    private fun appendOffers(newOffers: List<Offer>) {
        val totalOffers = ArrayList<Offer>(newOffers.size + offers.value.size)
        totalOffers.addAll(offers.value)
        totalOffers.addAll(newOffers)
        _offers.value = totalOffers
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
}