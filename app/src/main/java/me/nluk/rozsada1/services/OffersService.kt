package me.nluk.rozsada1.services

import android.content.Context
import android.net.Uri
import com.google.firebase.firestore.Query
import dev.gitlive.firebase.firestore.orderBy
import kotlinx.coroutines.flow.Flow
import me.nluk.rozsada1.lib.FirestorePaging
import me.nluk.rozsada1.model.Offer
import me.nluk.rozsada1.model.OfferDraft

interface OffersService {
    suspend fun getRecentOffers(page : FirestorePaging<String>) : List<Offer>
    suspend fun getOffersByIds(ids : Flow<List<String>>) : Flow<List<Offer>>
    suspend fun getOffersByIds(ids : List<String>) : List<Offer>
    fun addOffer(context : Context, offerDraft: OfferDraft, images: List<Uri>)
}