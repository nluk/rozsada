package me.nluk.rozsada1.ui.composable

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.CountDownTimer
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.MapView
import com.google.android.libraries.maps.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import me.nluk.rozsada1.R
import me.nluk.rozsada1.lib.rememberMapViewWithLifecycle
import me.nluk.rozsada1.ui.theme.grey
import me.nluk.rozsada1.ui.theme.lightGrey
import me.nluk.rozsada1.ui.theme.veryLightGray
import java.util.*
import javax.inject.Inject


@Composable
fun MapScreen(exit : () -> Unit, setAddress : (Address) -> Unit, initialLatLng: LatLng, viewModel : MapViewModel = hiltViewModel()){
    viewModel.setInitialLatLng(initialLatLng)
    Surface(color = veryLightGray) {
        val mapView = rememberMapViewWithLifecycle()
        val context = LocalContext.current
        Column(Modifier.fillMaxWidth()) {
            Card(modifier = Modifier.weight(1.0f).padding(4.dp)) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    OutlinedButton(shape = RoundedCornerShape(100), modifier = Modifier.weight(1.0f), onClick = {
                        viewModel.marker = null
                        exit()
                    }) {
                        Text(text = "X")
                    }
                    TextField(
                        value = viewModel.addressSearchText.value,
                        onValueChange = { value -> viewModel.onTextChanged(context, value) },
                        modifier = Modifier.weight(8.0f),
                        colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.Transparent),
                        placeholder = { Text(stringResource(R.string.enter_address)) }
                    )
                }
            }
            Card(modifier = Modifier.weight(1.0f).padding(4.dp).align(Alignment.CenterHorizontally) ) {
                Column{
                    Text(modifier = Modifier.fillMaxWidth().padding(4.dp), text = stringResource(R.string.selected_address), color = grey)
                    Text(modifier = Modifier.fillMaxWidth().padding(4.dp), text = viewModel.addressText.value ?: "", color = Color.Black)
                }
            }
            val latLng = viewModel.latlng.value ?: initialLatLng
            Box(modifier = Modifier
                .fillMaxWidth()
                .weight(8.0f)){
                MapViewContainer(setAddress, mapView, latLng, viewModel::mapClicked)
            }
        }
    }
}

@Composable
private fun MapViewContainer(
    setAddress: (Address) -> Unit,
    mapView: MapView,
    latLng: LatLng,
    mapClicked : (context : Context, clickLatLng: LatLng, map : GoogleMap, setAddress: (Address) -> Unit) -> Unit
) {
    val context = LocalContext.current
    AndroidView(
        factory = { mapView }
    ) {
        mapView.getMapAsync { map ->
            map.uiSettings.setAllGesturesEnabled(true)
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,  Math.max(map.cameraPosition.zoom, 15.0f)))
            map.setOnMapClickListener{
                mapClicked(context, it, map, setAddress)
            }
        }
    }
}

@HiltViewModel
class MapViewModel @Inject constructor() : ViewModel(), GoogleMap.OnMapClickListener {
    var latlng = mutableStateOf<LatLng?>(null)
    var marker : Marker? = null
    val addressSearchText = mutableStateOf("")
    val addressText = mutableStateOf<String?>(null)
    var timer: CountDownTimer? = null
    var initialLatLngUsed = false

    fun setupMarker(map : GoogleMap){
        val markerStart = latlng.value
        if(marker == null && markerStart != null){
            marker = map.addMarker(MarkerOptions().position(markerStart).draggable(false))
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(markerStart,  map.cameraPosition.zoom))
        }
    }

    fun updateLatLng(newLatLng: LatLng?){
        newLatLng?.also {
            latlng.value = it
            marker?.position = it
        }
    }

    fun onTextChanged(context: Context, text: String?){
        addressSearchText.value = text ?: ""
        if (text.isNullOrBlank()) return
        timer?.cancel()
        timer = object : CountDownTimer(500, 1000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                updateLatLng(getLocationFromAddress(context, text)?.let { LatLng(it.latitude, it.longitude) })
            }
        }.start()
    }

    fun getLocationFromAddress(context: Context, strAddress: String) = Geocoder(context, Locale.getDefault())
        .getFromLocationName(strAddress, 1)
        .firstOrNull()?.run {
            val loc = Location("")
            loc.latitude = latitude
            loc.longitude = longitude
            loc
        }

    override fun onMapClick(latLng: LatLng) = updateLatLng(latLng)

    private fun getAddressFromLatLng(context: Context, latLng: LatLng) = Geocoder(context, Locale.getDefault())
        .getFromLocation(latLng.latitude, latLng.longitude, 1)
        .firstOrNull()

    fun setInitialLatLng(initialLatLng: LatLng){
        if(initialLatLngUsed) return
        initialLatLngUsed = true
        updateLatLng(initialLatLng)
    }

    fun mapClicked(context : Context, clickLatLng: LatLng, map : GoogleMap, setAddress: (Address) -> Unit){
        setupMarker(map)
        val address = getAddressFromLatLng(context, clickLatLng)
        address?.also {
            addressText.value = it.getAddressLine(0)
            setAddress(it)
        }
        onMapClick(clickLatLng)
    }

}