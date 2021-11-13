package me.nluk.rozsada1.ui.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import me.nluk.rozsada1.model.BarterOffer
import me.nluk.rozsada1.services.BarterService
import java.time.Instant
import java.util.*
import javax.inject.Inject

@Composable
fun BarterScreen(model : BarterScreenModel = hiltViewModel()) {
    when (val state = model.stateFlow.collectAsState().value) {
        is BarterScreenModel.State.Loading -> {
            Text("Loading")
        }
        is BarterScreenModel.State.BarterOffersState -> {
            BarterScreenContent(state.barterOffers)
        }
    }
}

@Composable
fun BarterScreenContent(barterOffers: List<BarterOffer>){
    LazyColumn{
        items(barterOffers){
            BarterOfferCard(it)
        }
    }
}


@Composable
@Preview
fun BarterOfferCardPreview(){
    BarterOfferCard(
        BarterOffer(UUID.randomUUID(), UUID.randomUUID(), "Rapunzel", "Devil's ivy",
        listOf("https://res.cloudinary.com/patch-gardens/image/upload/c_fill,f_auto,h_400,q_auto:good,w_400/v1618417886/o9s5jk0fkxdm0zpp7edh.jpg",
            "https://res.cloudinary.com/patch-gardens/image/upload/c_fill,f_auto,h_400,q_auto:good,w_400/v1564152528/products/devils-ivy-12cfff.jpg")
            , 15.70, Instant.now())
    )
}

@Composable
fun BarterOfferCard(barterOffer: BarterOffer){
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(3.dp),
        elevation = 12.dp
    ){
        Column {
            Text(
                text = barterOffer.title
            )
            barterOffer.subtitle?.let {
                Text(
                    text = it
                )
            }
        }
    }
}


@HiltViewModel
class BarterScreenModel @Inject constructor(
    private val barterService: BarterService
) : ViewModel() {
    sealed class State {
        object Loading: State()
        data class BarterOffersState(val barterOffers: List<BarterOffer>): State()
    }

    private var _state = MutableStateFlow<State>(State.Loading)
    val stateFlow = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val data = barterService.getBarterOffers()
            _state.value = State.BarterOffersState(data)
        }
    }
}