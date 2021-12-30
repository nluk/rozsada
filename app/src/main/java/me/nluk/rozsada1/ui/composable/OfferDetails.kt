package me.nluk.rozsada1.ui.composable

import android.text.format.DateUtils
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.rememberImagePainter
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.MapView
import com.google.android.libraries.maps.model.CircleOptions
import com.google.android.libraries.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.nluk.rozsada1.R
import me.nluk.rozsada1.lib.rememberMapViewWithLifecycle
import me.nluk.rozsada1.model.Location
import me.nluk.rozsada1.model.Offer
import me.nluk.rozsada1.model.User
import me.nluk.rozsada1.services.FavouritesService
import me.nluk.rozsada1.services.OffersService
import me.nluk.rozsada1.services.UserService
import me.nluk.rozsada1.ui.theme.*
import java.util.*
import javax.inject.Inject

@ExperimentalPagerApi
@ExperimentalCoroutinesApi
@Composable
fun OfferDetailsScreen(offerId : String, exitDetails : () -> Unit,  viewModel : OfferDetailsViewModel = hiltViewModel()){
    viewModel.initOffer(offerId)
    val offer = viewModel.offer.collectAsState().value
    val favourite = offerId in viewModel.favourites.collectAsState(emptySet()).value
    val owner = viewModel.owner.value
    val mapView = rememberMapViewWithLifecycle()
    BackHandler(enabled = true){
        exitDetails()
    }
    OfferDetailsContent(offer, favourite, viewModel::likeOffer, owner, mapView)
}


@ExperimentalPagerApi
@Composable
fun OfferDetailsContent(offer : Offer?, favourite: Boolean, likeOffer: (String) -> Unit, owner : User?, mapView: MapView){
    val scrollState = rememberScrollState()
    Column(modifier = Modifier
        .fillMaxSize()
        .verticalScroll(scrollState), horizontalAlignment = Alignment.CenterHorizontally){
        if(offer == null) OfferDetailsPlaceholder() else OfferDetails(offer, favourite, likeOffer)
        if(owner== null) UserInfoPlaceholder() else UserInfo(owner)
        offer?.run {
            MapPreview(mapView, city, location!!)
        }
    }
}

@ExperimentalPagerApi
@Composable
fun OfferDetails(offer: Offer, favourite : Boolean, likeOffer : (String) -> Unit){
    val pagerState = rememberPagerState()
    Box(modifier = Modifier.fillMaxWidth()){
        HorizontalPager(
            count = offer.images.size,
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
        ) { page ->
            OfferImage(offer.images[page], 250.dp)
        }
        HorizontalPagerIndicator(
            pagerState = pagerState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp),
            activeColor = Purple500,
            inactiveColor = veryLightGray,
            indicatorWidth = 10.dp,
            indicatorHeight = 10.dp
        )
    }

    ColumnCard{
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row{
                    Text(
                        text = "${offer.city} -",
                        fontSize = 12.sp,
                        color = Color.DarkGray,
                        fontWeight = FontWeight.Light,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = DateUtils.getRelativeTimeSpanString(offer.createdAt.toEpochMilli()).toString(),
                        fontSize = 12.sp,
                        color = Color.DarkGray,
                        fontWeight = FontWeight.Light
                    )
                }
                IconButton(
                    modifier = Modifier.then(
                        Modifier
                            .size(48.dp)),
                    onClick = { likeOffer(offer.id!!) }
                ) {
                    Icon(
                        if (favourite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        null,
                        tint = Purple700
                    )
                }
            }
            Text(
                text = offer.title,
                fontSize = 20.sp,
                color = Color.DarkGray
            )
            if (offer.points != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = offer.points.toString(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.padding(end = 5.dp))
                    Text(
                        text = stringResource(id = R.string.leaf_points),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = primaryColor
                    )
                    Spacer(modifier = Modifier.padding(end = 5.dp))
                    Image(
                        painter = painterResource(R.drawable.ic_leaf),
                        contentDescription = "Points leaf icon"
                    )
                }
            } else {
                Text(
                    text = "%.2f ${Currency.getInstance("PLN").currencyCode}".format(offer.price),
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.padding(vertical = 16.dp))
            Text(
                text = stringResource(id = R.string.offer_description),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.padding(vertical = 8.dp))
            Text(text = offer.description, modifier = Modifier.fillMaxWidth(), color = darkGrey)
    }
}

@Composable
fun OfferDetailsPlaceholder(){
    Column(modifier = Modifier.fillMaxWidth()) {
        Image(
            painter = painterResource(id = R.drawable.offer_image_placeholder),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentScale = ContentScale.Crop
        )
    }
}


@Composable
fun ColumnCard(height: Dp? = null, horizontalArrangement: Alignment.Horizontal = Alignment.Start, content: @Composable () -> Unit){
     var modifier =    Modifier.fillMaxWidth()
        .padding(8.dp)
    if(height != null){
        modifier = modifier.height(height)
    }
    Card(modifier = modifier) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = horizontalArrangement
        ) {
            content()
        }
    }
}


@Composable
fun MapPreview(mapView: MapView, city : String, location: Location){
    ColumnCard(height = 200.dp){
        Text(text = city, modifier = Modifier.padding(bottom = 8.dp), fontWeight = FontWeight.Light)
        AndroidView(
            factory = { mapView }
        ) {
            mapView.getMapAsync { map ->
                val latLng = location.run { LatLng(lat, lon) }
                map.uiSettings.setAllGesturesEnabled(false)
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,  10.0f))
                map.addCircle(CircleOptions().center(latLng).radius(3000.0).fillColor(0x5500b30f).strokeColor(0x5500b30f))
            }
        }
    }
}

@Composable
fun UserInfo(user : User){
    ColumnCard {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically){
            Image(
                painter = rememberImagePainter(data = user.avatar),
                contentDescription = "offer_image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .height(60.dp)
                    .aspectRatio(1.0f)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.padding(horizontal = 8.dp))
            Text(text = user.openid!!.let { "${it.givenName} ${it.familyName}" }, fontWeight = FontWeight.Light)
        }
    }
}

@Composable
fun UserInfoPlaceholder(){

}

@HiltViewModel
class OfferDetailsViewModel @Inject constructor(
    private val offersService: OffersService,
    private val userService: UserService,
    private val favouritesService: FavouritesService
) : ViewModel() {
    val offer = MutableStateFlow<Offer?>(null)
    val owner = mutableStateOf<User?>(null)
    var favourites: Flow<Set<String>> = emptyFlow()
    var offerInitiated = false

    init {
        viewModelScope.launch(Dispatchers.IO){
            favourites = favouritesService.favouriteOfferIds()
        }
    }

    @ExperimentalCoroutinesApi
    fun initOffer(offerId: String) = viewModelScope.launch(Dispatchers.IO){
        val currentOfferId = offer.value?.id
        if(!offerInitiated || (currentOfferId != null && currentOfferId != offerId)){
            offerInitiated = true
            offersService.getOffersByIds(flowOf(listOf(offerId))).collectLatest {
                val o = it.firstOrNull()
                if(o == null){
                    offerInitiated = false
                    return@collectLatest
                }
                offer.value = o
                userService.user(o.userId).snapshots.collectLatest { offerDoc ->
                    owner.value = (offerDoc.data() as User).copy(id = offerDoc.id)
                    println("Owner updated to ${owner.value}")
                }
            }
        }
    }

    fun likeOffer(offerId: String) = viewModelScope.launch(Dispatchers.IO){
        if (offerId in favourites.firstOrNull() ?: emptySet()) favouritesService.removeFavourite(offerId) else favouritesService.markFavourite(offerId)
    }
}