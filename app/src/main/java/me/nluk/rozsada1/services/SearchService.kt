package me.nluk.rozsada1.services

import kotlinx.coroutines.flow.Flow
import me.nluk.rozsada1.model.Offer
import me.nluk.rozsada1.model.SearchCategory
import me.nluk.rozsada1.model.SearchInput

interface SearchService {
    suspend fun getSearchCategories() : Flow<List<SearchCategory>>
    suspend fun getOffers(search : Flow<SearchInput>) : Flow<List<Offer>>
}