package me.nluk.rozsada1.model

import androidx.annotation.Keep
import com.google.firebase.firestore.PropertyName
import kotlinx.serialization.Serializable

@Serializable
@Keep
data class User(
    val id : String = "",
    val email : String = "",
    val avatar : String? = null,
    @PropertyName("offer_stats")
    var offerStats: OfferStats = OfferStats(
        finished = 0,
        inProgress = 0
    ),
    val openid : OpenId? = null,
    val favourites : List<String> = emptyList()
)

@Serializable
@Keep
data class OfferStats(
    val finished : Int,
    @PropertyName("in_progress")
    val inProgress : Int
)

@Serializable
@Keep
data class OpenId(
    @PropertyName("family_name")
    val familyName : String,
    @PropertyName("given_name")
    val givenName : String,
    @PropertyName("phone_number")
    val phoneNumber : String
)
