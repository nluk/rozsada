package me.nluk.rozsada1.model

import androidx.annotation.Keep
import com.google.firebase.firestore.DocumentId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.nluk.rozsada1.lib.InstantSerializer
import java.time.Instant

@Serializable
@Keep
data class Offer(
    @SerialName("user_id")
    val userId: String,
    @DocumentId
    val id: String,
    val title: String,
    val description: String,
    val images: List<String>,
    val price: Double? = null,
    val points: Int? = null,
    @SerialName("created_at")
    @Serializable(with = InstantSerializer::class)
    val createdAt: Instant,
    val city: String
)