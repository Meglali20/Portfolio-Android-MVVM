package com.oussama.portfolio.di

import com.oussama.portfolio.data.DataStoreRepository
import com.oussama.portfolio.data.MainRepository
import com.oussama.portfolio.data.local.LocalRepository
import com.oussama.portfolio.data.local.room.PortfolioDatabase
import com.oussama.portfolio.data.remote.RemoteRepository
import com.oussama.portfolio.data.remote.RetrofitClient
import com.oussama.portfolio.utils.Network
import com.oussama.portfolio.utils.NetworkConnectivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideDataStoreRepository(
    ): DataStoreRepository = DataStoreRepository()


    @Provides
    @Singleton
    fun provideLocalRepository(portfolioDatabase: PortfolioDatabase): LocalRepository {
        return LocalRepository(portfolioDatabase)
    }

    @Provides
    @Singleton
    fun provideRemoteRepository(retrofitClient: RetrofitClient, networkConnectivity: NetworkConnectivity): RemoteRepository {
        return RemoteRepository(retrofitClient,  networkConnectivity)
    }

    @Provides
    @Singleton
    fun provideMainRepository(
        remoteRepository: RemoteRepository,
        localRepository: LocalRepository
    ): MainRepository {
        return MainRepository(remoteRepository, localRepository)
    }

    @Provides
    @Singleton
    fun provideNetworkConnectivity(): NetworkConnectivity {
        return Network()
    }
}