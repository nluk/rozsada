package me.nluk.rozsada1.model

import androidx.annotation.Keep
import com.google.firebase.firestore.DocumentId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import me.nluk.rozsada1.lib.InstantSerializer
import java.time.Instant

@Serializable
@Keep
data class Offer(
    @SerialName("user_id")
    val userId: String,
    @Transient
    val id: String? = null,
    val title: String,
    val description: String,
    val images: List<String>,
    val price: Double? = null,
    val points: Int? = null,
    @SerialName("created_at")
    @Serializable(with = InstantSerializer::class)
    val createdAt: Instant,
    val city: String,
    val location: Location?,
    @SerialName("category_id")
    val categoryId : Int
)

@Serializable
data class Location(
    val lat : Double,
    val lon : Double
)