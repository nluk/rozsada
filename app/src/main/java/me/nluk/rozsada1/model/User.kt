package me.nluk.rozsada1.model

import androidx.annotation.Keep
import com.google.firebase.firestore.PropertyName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Keep
data class User(
    val id : String = "",
    val email : String = "",
    val avatar : String? = null,
    @SerialName("offer_stats")
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
    @SerialName("in_progress")
    val inProgress : Int
)

@Serializable
@Keep
data class OpenId(
    @SerialName("family_name")
    val familyName : String,
    @SerialName("given_name")
    val givenName : String,
    @SerialName("phone_number")
    val phoneNumber : String? = null
)
