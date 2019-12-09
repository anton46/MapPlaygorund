package com.test.app.di

import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import com.test.app.BuildConfig
import com.test.app.domain.api.FoursquareApi
import com.test.app.domain.proxy.FoursquareApiProxy
import com.test.app.net.factory.ApiFactory
import com.test.app.net.factory.GenericApiFactory
import com.test.app.net.provider.ApiProvider
import com.test.app.net.provider.ApiProviderFactory
import com.test.app.net.provider.DefaultApiProvider
import com.test.app.net.retrofit.GsonConverterFactory
import com.test.app.net.retrofit.IBaseUrlProvider
import com.test.app.net.retrofit.IBaseUrlProviderFactory
import com.test.app.net.retrofit.FoursquareApiBaseUrl
import com.test.app.net.settings.NetworkSettingsProvider
import io.reactivex.Scheduler
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
class NetModule {

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return Gson()
    }

    @Provides
    @Singleton
    fun provideOkHttpClientBuilder() = OkHttpClient.Builder()

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        val httpLoggingInterceptor = HttpLoggingInterceptor()

        httpLoggingInterceptor.level = if (BuildConfig.DEBUG)
            HttpLoggingInterceptor.Level.BODY
        else
            HttpLoggingInterceptor.Level.NONE

        return httpLoggingInterceptor
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        builder: OkHttpClient.Builder,
        interceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return builder
            .addNetworkInterceptor(interceptor)
            .addInterceptor(interceptor)
            .retryOnConnectionFailure(false)
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Singleton
    @Provides
    fun provideGsonConverterFactory(
        gson: Gson
    ): GsonConverterFactory {
        return GsonConverterFactory.create(gson)
    }

    @Singleton
    @Provides
    fun provideApiProviderFactory(): ApiProviderFactory {
        return object : ApiProviderFactory {
            override fun <Api> create(apiFactory: ApiFactory<Api>): ApiProvider<Api> {
                return DefaultApiProvider(apiFactory.create())
            }
        }
    }

    @Singleton
    @Provides
    fun provideFoursquareAPI(
        apiFactory: ApiFactory<FoursquareApi>,
        apiProviderFactory: ApiProviderFactory,
        @Named(RxModule.COMPUTATION) schedulerFactory: Scheduler
    ): FoursquareApi {
        return FoursquareApiProxy(apiProviderFactory.create(apiFactory), schedulerFactory)
    }

    @Singleton
    @Provides
    @Named(FOURSQUARE)
    fun provideFoursquareApiBaseUrl(networkSettingsProvider: NetworkSettingsProvider): IBaseUrlProviderFactory {
        return object : IBaseUrlProviderFactory {
            override fun create(): IBaseUrlProvider {
                return FoursquareApiBaseUrl(networkSettingsProvider)
            }
        }
    }

    @Singleton
    @Provides
    fun provideFoursquareApiFactory(
        client: OkHttpClient,
        @Named(FOURSQUARE) urlProviderFactory: IBaseUrlProviderFactory,
        converterFactory: GsonConverterFactory
    ): ApiFactory<FoursquareApi> {
        return GenericApiFactory(
            client,
            urlProviderFactory,
            converterFactory,
            FoursquareApi::class.java
        )
    }

    companion object {
        private const val LOG_TAG = "HTTP"
        private const val FOURSQUARE = "FOURSQUARE"
    }
}