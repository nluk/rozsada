package me.nluk.rozsada1.ui.composable

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.rememberImagePainter
import com.google.accompanist.pager.ExperimentalPagerApi
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.nluk.rozsada1.R
import javax.inject.Inject
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.libraries.maps.MapView
import com.google.android.libraries.maps.model.LatLng
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import me.nluk.rozsada1.lib.rememberMapViewWithLifecycle
import me.nluk.rozsada1.model.OfferDraft
import me.nluk.rozsada1.model.SearchCategoryData
import me.nluk.rozsada1.services.OffersService
import me.nluk.rozsada1.services.SearchService
import me.nluk.rozsada1.ui.theme.Purple500
import me.nluk.rozsada1.ui.theme.lightRed
import me.nluk.rozsada1.ui.theme.veryLightGray
import java.util.*
import kotlin.collections.ArrayList


@ExperimentalPagerApi
@Composable
fun AddOfferScreen(model : AddOfferViewModel = hiltViewModel()){
    val context = LocalContext.current
    if(ContextCompat.checkSelfPermission(LocalContext.current, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
        val locationPermissionRequest = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
          if(permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)){
              model.locationGranted()
          }
        }
        SideEffect {
            locationPermissionRequest.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }
    else{
        model.locationGranted()
    }

    val enterMaps = {
        if(model.initialLatLng.value == null){
            Toast.makeText(context, "We are checking your location", Toast.LENGTH_SHORT).show()
        }else{
            model.enterMaps()
        }
    }

    val inMaps = model.inMaps.value
    val mapView = rememberMapViewWithLifecycle()
    val searchCategoryData = model.searchCategoryData.collectAsState(initial = null)
    BackHandler(enabled = inMaps){
        model.exitMaps()
    }
    val addImagesLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        model.addImages(uris)
    }
    val addImages = { addImagesLauncher.launch("image/*") }
    if(inMaps){
        MapScreen(model::exitMaps, model::setAddress, model.initialLatLng.value!!)
    }
    else{
        AddOfferScreenContent(
            offer = model.newOffer.value,
            offerOperations = OfferOperations(model::setOfferTitle, model::setOfferDescription, model::setCategoryId, model::setPrice, model::setPoints),
            removed = model.removed.value,
            imageUris =  model.images.value,
            addImages = addImages,
            markAndRemove = model::markAndRemove,
            enterMaps = enterMaps,
            createOffer = model::createOffer,
            searchCategoryData = searchCategoryData.value,
            mapView = mapView,
            newAddress = model.newAddress.value,
            mapCleared = model::mapCleared
        )
    }
}




@Composable
fun AddOfferScreenContent(
    removed : Int?,
    offer: OfferDraft,
    searchCategoryData: SearchCategoryData?,
    offerOperations: OfferOperations,
    imageUris : List<Uri>,
    addImages : () -> Unit,
    markAndRemove : (Int) -> Unit,
    enterMaps : () -> Unit,
    createOffer : (Context) -> Unit,
    mapView: MapView,
    newAddress : Boolean,
    mapCleared : () -> Unit
){
    val context = LocalContext.current
    val rowState = rememberLazyListState()
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally) {

        TitleText(text = stringResource(R.string.create_offer))

        Card(modifier = Modifier
            .wrapContentHeight()
            .padding(12.dp)) {

            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally) {


                if (imageUris.isEmpty()) {
                    AddImageIcon {
                        addImages()
                    }
                    Text(text = stringResource(R.string.add_offer_images))
                } else {
                    LazyRow(
                        state = rowState,
                        content = {
                            item {
                                Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                            }
                            itemsIndexed(imageUris) { index, item ->
                                Image(
                                    painter = rememberImagePainter(data = item),
                                    contentDescription = "offer_image",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .width(150.dp)
                                        .aspectRatio(1.0f)
                                        .pointerInput(Unit) {
                                            detectTapGestures(
                                                onLongPress = {
                                                    markAndRemove(index)
                                                }
                                            )
                                        }
                                        .border(
                                            2.dp,
                                            if (removed == index) lightRed else Color.Transparent
                                        )
                                        .padding(horizontal = 5.dp)
                                )

                            }
                            item {
                                AddImageIcon {
                                    addImages()
                                }
                            }
                            item {
                                Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                            }
                        })
                }
                OfferData(offer = offer, offerOperations = offerOperations, categoryData = searchCategoryData)
                LabeledField(R.string.location) {
                    RefreshableMapPreview(mapView = mapView, address = offer.address, newAddress, mapCleared)
                    OutlinedButton(onClick = enterMaps) {
                        Text(text = stringResource(R.string.set_location))
                    }
                }
                Spacer(modifier = Modifier.padding(vertical = 20.dp))
                Button(onClick = {createOffer(context)}) {
                    Text(text = stringResource(R.string.create))
                }
            }
        }
    }
}


@Composable
fun OfferData(
    offer : OfferDraft, categoryData: SearchCategoryData?,
    offerOperations: OfferOperations
){
    LabeledField(R.string.category) {
        var expanded by remember { mutableStateOf(false) }
        val categories = categoryData?.getCategories(Locale.getDefault().language.toString()) ?: emptyList()
        val displayedCategory = offer.categoryId?.let{ categories.find { it.id == offer.categoryId } } ?: categories.firstOrNull()
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)) {
            Row(modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = { expanded = true })
                .background(veryLightGray),
                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically){
                Text(modifier = Modifier.padding(12.dp), text = displayedCategory?.name ?: "",textAlign = TextAlign.Center)
                Image(
                    painter = rememberImagePainter(data = displayedCategory?.imageUrl),
                    contentDescription = "offer_image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 2.dp)
                        .fillMaxHeight()
                        .aspectRatio(1.0f)
                        .clip(CircleShape)
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                categories.forEachIndexed { index, category ->
                    DropdownMenuItem(onClick = {
                        offerOperations.setCategoryId(category.id)
                        expanded = false
                    }) {
                        Row(modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically){
                            Text(text = category.name, color = if(offer.categoryId == category.id) Purple500 else Color.Black)
                            Image(
                                painter = rememberImagePainter(data = category.imageUrl),
                                contentDescription = "offer_image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .padding(horizontal = 12.dp, vertical = 2.dp)
                                    .fillMaxHeight()
                                    .aspectRatio(1.0f)
                                    .clip(CircleShape)
                            )
                        }
                    }
                }
            }
        }
    }
    LabeledField(R.string.offer_title) {
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = offer.title ?: "",
            placeholder = { Text(stringResource(R.string.ofer_title_placeholder)) },
            onValueChange = {
                offerOperations.setTitle(it)
            },
            colors = TextFieldDefaults.textFieldColors(backgroundColor = veryLightGray)
        )
    }
    LabeledField(R.string.offer_description) {
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .sizeIn(minHeight = 100.dp),
            value = offer.description ?: "",
            placeholder = { Text(stringResource(R.string.offer_description_placeholder)) },
            onValueChange = {
                offerOperations.setDescription(it)
            },
            colors = TextFieldDefaults.textFieldColors(backgroundColor = veryLightGray)
        )
    }
    LabeledField(R.string.cost) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly){
            TextField(
                modifier = Modifier
                    .weight(2.0f)
                    .padding(end = 8.dp),
                value = offer.price?.toString() ?: "",
                placeholder = { Text(stringResource(R.string.cost)) },
                onValueChange = {
                    offerOperations.setPrice(it.toDoubleOrNull())
                },
                colors = TextFieldDefaults.textFieldColors(backgroundColor = veryLightGray),
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
            )
            Text(stringResource(id = R.string.leaf_points), modifier = Modifier.weight(1.0f))
            RadioButton(selected = offer.isPoints, onClick = { offerOperations.setPoints(true) }, modifier = Modifier.weight(1.0f))
            Text("PLN", modifier = Modifier.weight(1.0f))
            RadioButton(selected = !offer.isPoints, onClick = { offerOperations.setPoints(false) }, modifier = Modifier.weight(1.0f))
        }
    }
}

@Composable
fun LabeledField(label_res : Int, field : @Composable () -> Unit){
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 25.dp, bottom = 5.dp),
        text = stringResource(label_res),
        textAlign = TextAlign.Start,
        fontWeight = FontWeight.SemiBold,
        fontFamily = FontFamily.SansSerif
    )
    field()
}

@Composable
fun AddImageIcon(onClick : () -> Unit){
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .border(2.dp, Color.LightGray, RoundedCornerShape(6.dp))
            .width(150.dp)
            .aspectRatio(1.0f)
    ) {
        Icon(
            imageVector = Icons.Filled.AddPhotoAlternate, "add a photo", tint = Color.LightGray, modifier = Modifier.fillMaxSize()
        )
    }
}


@HiltViewModel
class AddOfferViewModel @Inject constructor(
    private val searchService: SearchService,
    private val offersService: OffersService,
    @ApplicationContext private val context: Context
) : ViewModel() {

    var initialLatLng = mutableStateOf<LatLng?>(null)
    var inMaps = mutableStateOf(false)
    var hasPermission = mutableStateOf(false)
    var images = mutableStateOf(listOf<Uri>())
    var removed =  mutableStateOf<Int?>(null)
    var newOffer = mutableStateOf(OfferDraft())
    val searchCategoryData = searchService.getSearchCategories().flowOn(Dispatchers.IO)
    val newAddress = mutableStateOf(false)

    init {
        viewModelScope.launch(Dispatchers.IO){
            searchCategoryData.filterNotNull().take(1).collect {
                if(newOffer.value.categoryId == null){
                    newOffer.value = newOffer.value.copy(categoryId = it.getCategories(Locale.getDefault().language.toString())!!.first().id)
                    println("Set category id to default")
                }
                this.cancel()
            }
        }
    }

    fun markAndRemove(index : Int) =  viewModelScope.launch(Dispatchers.IO) {
        removed.value = index
        delay(200L)
        val new = ArrayList(images.value)
        new.removeAt(index)
        removed.value = null
        images.value = new
    }

    fun addImages(newImages : List<Uri>){
        images.value = ArrayList(images.value) + newImages
    }

    fun exitMaps(){
        inMaps.value = false
    }

    fun enterMaps(){
        if(hasPermission.value){
            inMaps.value = true
        }
    }

    fun setOfferTitle(title : String){
        newOffer.value = newOffer.value.copy(title = title)
    }

    fun setOfferDescription(description : String){
        newOffer.value = newOffer.value.copy(description =  description)
    }

    fun setAddress(address : Address){
        newAddress.value = newOffer.value.address?.locality != address.locality
        newOffer.value = newOffer.value.copy(address = address)
    }

    fun setCategoryId(categoryId : Int){
        newOffer.value = newOffer.value.copy(categoryId = categoryId)
    }

    fun mapCleared(){
        newAddress.value = false
    }

    fun createOffer(context: Context){
        offersService.addOffer(context, newOffer.value, images.value)
        images.value = emptyList()
        newOffer.value = OfferDraft()
    }

    fun setPoints(isPoints : Boolean){
        newOffer.value = newOffer.value.copy(isPoints =  isPoints)
    }

    fun setPrice(price : Double?){
        newOffer.value = newOffer.value.copy(price = price)
    }

    @SuppressLint("MissingPermission")
    fun locationGranted(){
        hasPermission.value = true
        viewModelScope.launch(Dispatchers.IO){
            val loc = LocationServices.getFusedLocationProviderClient(context).getCurrentLocation(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY, CancellationTokenSource().token)
            loc.addOnSuccessListener {
                if(it == null){
                    locationGranted()
                    return@addOnSuccessListener
                }
                //Toast.makeText(context, "Location: ${it.latitude} ${it.longitude}", Toast.LENGTH_LONG).show()
                initialLatLng.value = LatLng(it.latitude, it.longitude)
            }
            loc.addOnFailureListener {
                Toast.makeText(context, "Location failed: ${it.message}", Toast.LENGTH_LONG).show()
                locationGranted()
            }
        }
    }

}

data class OfferOperations(
    val setTitle: (String) -> Unit,
    val setDescription: (String) -> Unit,
    val setCategoryId: (Int) -> Unit,
    val setPrice : (Double?) -> Unit,
    val setPoints : (Boolean) -> Unit
)