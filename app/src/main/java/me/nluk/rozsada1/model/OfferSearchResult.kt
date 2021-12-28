package me.nluk.rozsada1.model

import kotlinx.serialization.Serializable

@Serializable
data class OfferSearchResult(
    val title : String,
    val score : Double,
    val firebaseId : String
)
