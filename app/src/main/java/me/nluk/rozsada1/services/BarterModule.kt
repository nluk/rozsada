package me.nluk.rozsada1.services

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.ViewModelComponent
import me.nluk.rozsada1.model.BarterOffer
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import com.google.gson.GsonBuilder

import com.google.gson.Gson
import com.google.gson.JsonParseException

import com.google.gson.JsonDeserializationContext

import com.google.gson.JsonElement

import com.google.gson.JsonPrimitive

import com.google.gson.JsonSerializationContext

import com.google.gson.JsonDeserializer

import com.google.gson.JsonSerializer
import java.lang.reflect.Type
import java.time.Instant


@Module
@InstallIn(ViewModelComponent::class)
object BarterModule {

    @Provides
    fun provideBarterService(): BarterService {
        val gson = GsonBuilder()
            .registerTypeAdapter(Instant::class.java, INSTANT_DESERIALIZER)
            .create()
        return Retrofit.Builder()
            .baseUrl("https://cdn.nluk.me/files/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(BarterService::class.java)
    }
}


private val INSTANT_DESERIALIZER = JsonDeserializer {
        json: JsonElement,
        type: Type?,
        context: JsonDeserializationContext? ->
    Instant.parse(json.asString)
}
