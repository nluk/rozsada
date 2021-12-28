package me.nluk.rozsada1.services

import me.nluk.rozsada1.model.OfferSearchResult
import me.nluk.rozsada1.model.UploadData
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SearchApiService {
    @GET("/text/query")
    suspend fun searchOffers(@Query("title") title : String, @Query("categoryId") categoryId : Int?) : List<OfferSearchResult>
}