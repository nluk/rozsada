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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.google.android.libraries.maps.SupportMapFragment
import com.google.android.libraries.maps.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import me.nluk.rozsada1.R
import me.nluk.rozsada1.lib.rememberMapViewWithLifecycle
import me.nluk.rozsada1.ui.theme.Purple200
import me.nluk.rozsada1.ui.theme.lightGrey
import me.nluk.rozsada1.ui.theme.veryLightGray
import java.util.*
import javax.inject.Inject


@Composable
fun MapScreen(exit : () -> Unit, setAddress : (Address) -> Unit, viewModel : MapViewModel = hiltViewModel()){
    Surface(color = veryLightGray) {
        val mapView = rememberMapViewWithLifecycle()
        val context = LocalContext.current

        Column(Modifier.fillMaxWidth()) {
            Card(modifier = Modifier.weight(1.0f).padding(4.dp)) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    OutlinedButton(shape = RoundedCornerShape(100), modifier = Modifier.weight(1.0f), onClick = exit) {
                        Text(text = "X")
                    }
                    TextField(
                        value = viewModel.addressText.value,
                        onValueChange = { value -> viewModel.onTextChanged(context, value) },
                        modifier = Modifier.weight(8.0f),
                        colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.Transparent),
                        placeholder = { Text(stringResource(R.string.enter_address)) }
                    )
                }
            }
            Card(modifier = Modifier.weight(1.0f).padding(4.dp)) {

            }
            Box(modifier = Modifier
                .fillMaxWidth()
                .weight(8.0f)){
                MapViewContainer(context, setAddress, mapView, viewModel.latlng.value, viewModel::setupMarker)
            }
        }
    }
}

@Composable
private fun MapViewContainer(
    context: Context,
    setAddress: (Address) -> Unit,
    mapView: MapView,
    latLng: LatLng,
    setupMarker : (Context, GoogleMap, (Address) -> Unit) -> Unit
) {
    AndroidView(
        factory = { mapView }
    ) {
        mapView.getMapAsync { map ->
            map.uiSettings.setAllGesturesEnabled(true)
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,  map.cameraPosition.zoom))
            setupMarker(context, map, setAddress)
        }
    }
}

@HiltViewModel
class MapViewModel @Inject constructor() : ViewModel(), GoogleMap.OnMapClickListener {
    var latlng = mutableStateOf(LatLng(50.0, 50.0))
    var marker : Marker? = null
    val addressText = mutableStateOf("")
    var timer: CountDownTimer? = null

    fun setupMarker(context: Context, map : GoogleMap, addressSelected: (Address) -> Unit){
        if(marker == null){
            val markerStart = latlng.value
            marker = map.addMarker(MarkerOptions().position(markerStart).draggable(false))
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(markerStart,  25.0f))
            map.setOnMapClickListener{
                getAddressFromLatLng(context, it)?.also(addressSelected)
                onMapClick(it)
            }
        }
    }

    fun updateLatLng(newLatLng: LatLng?){
        newLatLng?.also {
            latlng.value = it
            marker?.position = it
        }
    }

    fun onTextChanged(context: Context, text: String?){
        addressText.value = text ?: ""
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

}