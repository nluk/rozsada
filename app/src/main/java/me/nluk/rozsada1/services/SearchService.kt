package me.nluk.rozsada1.services

import kotlinx.coroutines.flow.Flow
import me.nluk.rozsada1.model.Offer
import me.nluk.rozsada1.model.SearchCategory
import me.nluk.rozsada1.model.SearchCategoryData
import me.nluk.rozsada1.model.SearchInput

interface SearchService {
    fun getSearchCategories() : Flow<SearchCategoryData>
    suspend fun getOffers(search : Flow<SearchInput>) : Flow<List<Offer>>
    suspend fun getOffers(search : SearchInput) : List<Offer>
}