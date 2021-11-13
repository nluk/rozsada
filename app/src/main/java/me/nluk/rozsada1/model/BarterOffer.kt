package me.nluk.rozsada1.model

import java.time.Instant
import java.util.*

data class BarterOffer(
    val userId : UUID,
    val id : UUID,
    val title : String,
    val subtitle : String?,
    val images : List<String>,
    val price : Double,
    val createdAt : Instant
)