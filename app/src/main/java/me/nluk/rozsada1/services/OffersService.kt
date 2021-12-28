package me.nluk.rozsada1.services

import com.google.firebase.firestore.Query
import dev.gitlive.firebase.firestore.orderBy
import kotlinx.coroutines.flow.Flow
import me.nluk.rozsada1.lib.FirestorePaging
import me.nluk.rozsada1.model.Offer

interface OffersService {
    suspend fun getRecentOffers(page : FirestorePaging<String>) : List<Offer>
    suspend fun getOffersByIds(ids : Flow<List<String>>) : Flow<List<Offer>>
}