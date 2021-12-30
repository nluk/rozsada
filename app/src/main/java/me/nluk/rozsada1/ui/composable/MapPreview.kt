package me.nluk.rozsada1.ui.composable

import android.content.Context
import android.graphics.Color
import android.location.Address
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.libraries.maps.CameraUpdateFactory
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.MapView
import com.google.android.libraries.maps.model.CircleOptions
import com.google.android.libraries.maps.model.LatLng
import me.nluk.rozsada1.ui.theme.transparentGreen

@Composable
fun RefreshableMapPreview(mapView: MapView, address : Address?, newAddress : Boolean, mapCleared : () -> Unit){
    address?.run {
        Column(Modifier.fillMaxWidth().height(300.dp)){
            Text(modifier = Modifier.weight(1.0f, fill = false).fillMaxWidth(), text = locality)
            AndroidView(
                modifier = Modifier.weight(3.0f, fill = true).fillMaxWidth(),
                factory = { mapView }
            ) {
                mapView.getMapAsync { map ->
                    val latlng = LatLng(latitude, longitude)
                    map.uiSettings.setAllGesturesEnabled(false)
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng,  10.0f))
                    if(newAddress){
                        map.clear()
                        mapCleared()
                    }
                    map.addCircle(CircleOptions().center(latlng).radius(3000.0).fillColor(0x5500b30f).strokeColor(0x5500b30f))
                }
            }
        }
    }
}
