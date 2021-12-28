package me.nluk.rozsada1.services

import me.nluk.rozsada1.model.UploadData
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Url

interface ImageUploadService {
    @POST("/rozsada-dev/presign-upload-url")
    suspend fun getUploadUrl() : UploadData

    @PUT
    suspend fun uploadFile(@Url uploadUrl : String, @Body body: RequestBody)
}