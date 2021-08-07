package com.techyourchance.dagger2course.common.dependnecyinjection.app

import android.app.Application
import com.techyourchance.dagger2course.Constants
import com.techyourchance.dagger2course.common.dependnecyinjection.Retrofit1
import com.techyourchance.dagger2course.common.dependnecyinjection.Retrofit2
import com.techyourchance.dagger2course.networking.StackoverflowApi
import com.techyourchance.dagger2course.networking.UrlProvider
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
class AppModule(val application: Application) {

    @Provides
    @AppScope
    @Named("Retrofit1")
    fun retrofit1(urlProvider: UrlProvider): Retrofit {
        return Retrofit.Builder()
                .baseUrl(urlProvider.getBaseUrl1())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
    }

    @Provides
    @AppScope
    @Named("Retrofit2")
    fun retrofit2(urlProvider: UrlProvider): Retrofit {
        return Retrofit.Builder()
                .baseUrl(urlProvider.getBaseUrl2())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
    }

//    @Provides
//    @Named("base_url")
//    fun baseUrl() = Constants.BASE_URL

//    @Provides
//    @Named("other_base_url")
//    fun otherBaseUrl() = "https://blabla.com/"

    @AppScope
    @Provides
    fun urlProvider() = UrlProvider()

    @Provides
    fun application() = application

    @Provides
    @AppScope
    fun stackoverflowApi(@Named("Retrofit2") retrofit: Retrofit) = retrofit.create(StackoverflowApi::class.java)

}

