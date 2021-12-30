package me.nluk.rozsada1.services.impl

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import me.nluk.rozsada1.BuildConfig
import me.nluk.rozsada1.services.ImageService
import me.nluk.rozsada1.services.ImagesAPI
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

class ImageServiceImpl @Inject constructor(
    val imagesAPI: ImagesAPI
) : ImageService{
    private fun getMimeType(context: Context, uri: Uri): String? {
        return if (ContentResolver.SCHEME_CONTENT == uri.scheme) {
            context.contentResolver.getType(uri)
        } else {
            val fileExtension = MimeTypeMap.getFileExtensionFromUrl(
                uri.toString()
            )
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                fileExtension.lowercase()
            )
        }
    }

    override suspend fun uploadImage(context: Context, image : Uri, folder : String?) : String {
        val contentBytes = context.contentResolver.openInputStream(image)?.readBytes()!!
        val uploadData = imagesAPI.getUploadData(folder)
        println("Upload data: $uploadData")
        val body = contentBytes.toRequestBody(getMimeType(context, image)!!.toMediaType(), 0, contentBytes.size)
        imagesAPI.uploadFile(uploadData.uploadUrl, body)
        println("Uploaded: $image")
        return "${BuildConfig.CLOUDFRONT_URL}/${uploadData.key}"
    }
}