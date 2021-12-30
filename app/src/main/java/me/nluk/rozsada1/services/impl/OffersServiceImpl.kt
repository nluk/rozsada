package me.nluk.rozsada1.services.impl;

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import android.widget.Toast
import com.google.firebase.firestore.Query
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.orderBy
import dev.gitlive.firebase.firestore.where
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import me.nluk.rozsada1.BuildConfig.CLOUDFRONT_URL
import me.nluk.rozsada1.R
import me.nluk.rozsada1.lib.DOCUMENT_ID
import me.nluk.rozsada1.lib.FirestorePaging
import me.nluk.rozsada1.model.Location
import me.nluk.rozsada1.model.Offer
import me.nluk.rozsada1.model.OfferDraft
import me.nluk.rozsada1.services.ImageService
import me.nluk.rozsada1.services.ImagesAPI
import me.nluk.rozsada1.services.OffersService
import me.nluk.rozsada1.services.UserService
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.Instant
import javax.inject.Inject;

class OffersServiceImpl @Inject constructor(
    firestore : FirebaseFirestore,
    val userService: UserService,
    val imageService: ImageService
) : FirestoreService(firestore), OffersService
{
    override val COLLECTION: String = "offers"
    override suspend fun getRecentOffers(page : FirestorePaging<String>) : List<Offer>  = queryCollection(page){ collection ->
        collection.where("published", true).where("disabled", false).orderBy("created_at", Query.Direction.DESCENDING)
    }.get().documents.map {
        (it.data() as Offer).copy(id = it.id)
    }

    private val offersServiceScope = CoroutineScope(Dispatchers.IO)

    override suspend fun getOffersByIds(ids : List<String>) : List<Offer>{
        if(ids.isEmpty()) return emptyList()
        return firestore.collection(COLLECTION)
            .where(path = DOCUMENT_ID, inArray = ids)
            .where("published", true)
            .where("disabled", false)
            .get()
            .documents.map { it.data<Offer>().copy(id = it.id) }
    }

    override suspend fun getOffersByIds(ids: Flow<List<String>>): Flow<List<Offer>> = ids.flatMapLatest { idsList ->
        if(idsList.isEmpty()){
            return@flatMapLatest flowOf(emptyList())
        }
        firestore.collection(COLLECTION)
            .where(path = DOCUMENT_ID, inArray = idsList)
            .where("published", true)
            .where("disabled", false)
            .snapshots
            .map { querySnapshot -> querySnapshot.documents }
            .map { offerDocuments -> offerDocuments.map { document -> document.data<Offer>().copy(id = document.id) } }
    }

    override fun addOffer(context: Context, offerDraft: OfferDraft, images: List<Uri>){
        offersServiceScope.launch {
            val imageUrls = images.map { async{ imageService.uploadImage(context, it) } }.awaitAll()
            println(imageUrls.joinToString("\n"))
            println(offerDraft)
            val offer = offerDraft.run {
                Offer(
                    userId = userService.currentUser().value!!.id,
                    title = title!!,
                    description = description!!,
                    images = imageUrls,
                    price = if(isPoints) null else price,
                    points = if(isPoints) price?.toInt() else null,
                    createdAt = Instant.now(),
                    city = address!!.locality,
                    location = Location(address.latitude, address.longitude),
                    categoryId = categoryId ?: 0,
                )
            }
            firestore.collection("offers").add(offer).id
            withContext(Dispatchers.Main){
                Toast.makeText(context, R.string.offer_added, Toast.LENGTH_LONG).show()
            }
        }
    }
}
