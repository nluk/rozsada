package me.nluk.rozsada1.services

import dev.gitlive.firebase.firestore.FieldValue
import dev.gitlive.firebase.firestore.where
import kotlinx.coroutines.flow.*
import me.nluk.rozsada1.lib.DOCUMENT_ID
import me.nluk.rozsada1.model.Offer
import me.nluk.rozsada1.model.User

interface FavouritesService{
    suspend fun markFavourite(offerId: String)
    suspend fun removeFavourite(offerId: String)
    suspend fun favouriteOffers(): Flow<List<Offer>>
    suspend fun favouriteOfferIds() : Flow<Set<String>>
}