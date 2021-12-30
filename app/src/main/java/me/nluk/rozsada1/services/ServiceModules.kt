package me.nluk.rozsada1.services

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore
import kotlinx.serialization.json.Json
import me.nluk.rozsada1.services.impl.*
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit

@Module
@InstallIn(ViewModelComponent::class)
object FirebaseAuthModule{
    @Provides
    fun provideFirebaseAuth() : FirebaseAuth {
//        GlobalScope.launch(Dispatchers.IO){
//            Firebase.auth.signOut()
//        }
        return Firebase.auth
    }
}

@Module
@InstallIn(ViewModelComponent::class)
object FirestoreModule{
    @Provides
    fun provideFirebaseFirestore() : FirebaseFirestore {
        return Firebase.firestore
    }
}

@Module
@InstallIn(ViewModelComponent::class)
object SearchApiServiceModule{
    @Provides
    fun provideSearchApiService() = retrofit("http://3.122.242.6")
        .create(SearchApiService::class.java)
}

@Module
@InstallIn(ViewModelComponent::class)
object ImageUploadServiceModule{
    @Provides
    fun provideImageUploadService() = retrofit("https://t36df3t5cl.execute-api.eu-central-1.amazonaws.com/rozsada-dev/")
        .create(ImagesAPI::class.java)
}

@Module
@InstallIn(ViewModelComponent::class)
abstract class AuthServiceModule{
    @Binds
    abstract fun bindAuthService(authServiceImpl: AuthServiceImpl) : AuthService
}

@Module
@InstallIn(ViewModelComponent::class)
abstract class FavouritesServiceModule{
    @Binds
    abstract fun bindFavouritesService(favouritesServiceImpl: FavouritesServiceImpl) : FavouritesService
}

@Module
@InstallIn(ViewModelComponent::class)
abstract class OffersServiceModule{
    @Binds
    abstract fun bindOffersService(offersServiceImpl: OffersServiceImpl): OffersService
}

@Module
@InstallIn(ViewModelComponent::class)
abstract class UserServiceModule{
    @Binds
    abstract fun bindUserService(userService: UserServiceImpl): UserService
}

@Module
@InstallIn(ViewModelComponent::class)
abstract class SearchServiceModule{
    @Binds
    abstract fun bindSearchService(searchService: SearchServiceImpl): SearchService
}

@Module
@InstallIn(ViewModelComponent::class)
abstract class ImageServiceModule{
    @Binds
    abstract fun bindImageService(imageService: ImageServiceImpl): ImageService
}


private fun retrofit(baseUrl : String) = Retrofit.Builder()
    .baseUrl(baseUrl)
    .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
    .build()