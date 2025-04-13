package de.dimskiy.waypoints.platform.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.dimskiy.waypoints.domain.providers.photonservice.PhotonApiService
import de.dimskiy.waypoints.platform.network.KarooPhotonApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor { message ->
                Timber.tag("OkHttp").d(message)
            }.apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

//    @Provides
//    @Singleton
//    fun provideRetrofitPhotonApiService(
//        okHttpClient: OkHttpClient,
//        @ApplicationContext context: Context
//    ): PhotonApiService = Retrofit.Builder()
//            .baseUrl(context.getString(R.string.photon_api_base_url))
//            .client(okHttpClient)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//            .create(RetrofitPhotonApiService::class.java)
//    }

    @Provides
    @Singleton
    fun provideKarooPhotonApiService(impl: KarooPhotonApiService): PhotonApiService = impl
}
