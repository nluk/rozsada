package me.nluk.rozsada1.model

import com.google.firebase.firestore.DocumentId
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.nluk.rozsada1.lib.InstantSerializer
import java.time.Instant

data class OfferDraft(
    val title: String? = null,
    val description: String? = null,
    val images: List<String> = emptyList(),
    val price: Double? = null,
    val city: String? = null,
    val location: Location? = null,
    val categoryId : Int? = null
)
