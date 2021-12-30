package me.nluk.rozsada1.services

import android.content.Context
import android.net.Uri

interface ImageService {
    suspend fun uploadImage(context: Context, image : Uri, folder : String? = null) : String
}