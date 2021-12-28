package me.nluk.rozsada1.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UploadData(
    @SerialName("upload_url")
    val uploadUrl : String,
    val key : String
)
