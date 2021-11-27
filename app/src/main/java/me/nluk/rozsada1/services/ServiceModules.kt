package me.nluk.rozsada1.services

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
import me.nluk.rozsada1.services.impl.AuthServiceImpl
import me.nluk.rozsada1.services.impl.FavouritesServiceImpl
import me.nluk.rozsada1.services.impl.OffersServiceImpl
import me.nluk.rozsada1.services.impl.UserServiceImpl

@Module
@InstallIn(ViewModelComponent::class)
object FirebaseAuthModule{
    @Provides
    fun provideFirebaseAuth() : FirebaseAuth {
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