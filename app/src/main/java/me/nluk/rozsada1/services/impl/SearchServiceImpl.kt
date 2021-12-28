package me.nluk.rozsada1.services.impl

import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import me.nluk.rozsada1.model.*
import me.nluk.rozsada1.services.OffersService
import me.nluk.rozsada1.services.SearchApiService
import me.nluk.rozsada1.services.SearchService
import javax.inject.Inject

@ExperimentalCoroutinesApi
class SearchServiceImpl @Inject constructor(
    val firestore : FirebaseFirestore,
    val searchApiService: SearchApiService,
    val offersService  : OffersService
 ) : SearchService {

    override suspend fun getSearchCategories(): Flow<List<SearchCategory>> = firestore.collection("search")
        .document("categories")
        .snapshots
        .mapLatest { (it.data() as SearchCategoryData).categories }

    override suspend fun getOffers(search: Flow<SearchInput>): Flow<List<Offer>> {
        val idsFlow = search.mapLatest { searchApiService.searchOffers(it.text, it.categoryId) }
            .map { offersSearchResult -> offersSearchResult.map(OfferSearchResult::firebaseId).take(10) }
        return offersService.getOffersByIds(idsFlow)
    }
}