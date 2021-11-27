package me.nluk.rozsada1.services.impl

import dev.gitlive.firebase.firestore.FieldValue
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.where
import kotlinx.coroutines.flow.*
import me.nluk.rozsada1.lib.DOCUMENT_ID
import me.nluk.rozsada1.model.Offer
import me.nluk.rozsada1.model.User
import me.nluk.rozsada1.services.AuthService
import me.nluk.rozsada1.services.FavouritesService
import javax.inject.Inject

class FavouritesServiceImpl @Inject constructor(
    val authService: AuthService,
    val firestore : FirebaseFirestore
) :  FavouritesService {

    override suspend fun markFavourite(offerId: String) = authService.runAuthenticated { userId ->
        val userDoc =  firestore.collection("users").document(userId)
        userDoc.update("favourites" to FieldValue.arrayUnion(offerId))
    }

    override suspend fun removeFavourite(offerId: String)  = authService.runAuthenticated { userId ->
        val userDoc =  firestore.collection("users").document(userId)
        userDoc.update("favourites" to FieldValue.arrayRemove(offerId))
    }

    override suspend fun favouriteOffers(): Flow<List<Offer>> = favouriteOfferIdsFlow().flatMapLatest { ids ->
        if(ids.isEmpty()){
            return@flatMapLatest flowOf(emptyList())
        }
        firestore.collection("offers")
            .where(path = DOCUMENT_ID, inArray = ids)
            .snapshots
            .map { querySnapshot -> querySnapshot.documents }
            .map { offerDocuments -> offerDocuments.map { document -> document.data<Offer>().copy(id = document.id) } }
    }

    private fun favouriteOfferIdsFlow() : Flow<List<String>> = authService.authUserFlow().map{ user -> user?.uid}.flatMapLatest { userId ->
        if(userId == null){
            return@flatMapLatest flowOf(emptyList())
        }
        (firestore.collection("users").document(userId)).snapshots.map {
            println("Updating favourites to $it")
            it.data<User>().favourites
        }
    }

    override suspend fun favouriteOfferIds() : Flow<Set<String>> = favouriteOfferIdsFlow().map { it.toSet() }
}