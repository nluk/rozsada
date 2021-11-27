package me.nluk.rozsada1.services.impl;

import com.google.firebase.firestore.Query
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.orderBy
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import me.nluk.rozsada1.lib.FirestorePaging
import me.nluk.rozsada1.model.Offer
import me.nluk.rozsada1.services.OffersService
import javax.inject.Inject;

class OffersServiceImpl @Inject constructor(firestore : FirebaseFirestore) : FirestoreService(firestore), OffersService
{
    override val COLLECTION: String = "offers"
    override suspend fun getRecentOffers(page : FirestorePaging<String>) : List<Offer>  = queryCollection(page){ collection ->
        collection.orderBy("created_at", Query.Direction.DESCENDING)
    }.get().documents.map { (it.data() as Offer).copy(id = it.id) }
}
