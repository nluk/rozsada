package me.nluk.rozsada1.services

import me.nluk.rozsada1.model.BarterOffer
import retrofit2.http.GET

interface BarterService {
    @GET("plants.json")
    suspend fun getBarterOffers() : List<BarterOffer>
}