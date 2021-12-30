package me.nluk.rozsada1.services

import me.nluk.rozsada1.model.UploadData
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ImagesAPI {
    @POST("/rozsada-dev/presign-upload-url")
    suspend fun getUploadData(@Query("folder") folder : String? = null) : UploadData

    @PUT
    suspend fun uploadFile(@Url uploadUrl : String, @Body body: RequestBody)
}