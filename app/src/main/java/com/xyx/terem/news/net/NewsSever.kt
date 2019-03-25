package com.xyx.terem.news.net

import com.xyx.terem.news.BuildConfig
import io.reactivex.Observable
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class NewsSever {

    private object Holder {
        val INSTANCE = NewsSever()
    }

    companion object {
        private const val BASE_URL = "https://hacker-news.firebaseio.com"

        val instance: NewsSever by lazy { Holder.INSTANCE }
    }

    private val mNewsApi: NewsApi

    private constructor() {
        var retrofitBuilder = Retrofit
            .Builder()
            .baseUrl(BASE_URL)
        if (BuildConfig.DEBUG) {
            val httpLoggingInterceptorBody = HttpLoggingInterceptor()
            httpLoggingInterceptorBody.level = HttpLoggingInterceptor.Level.BODY
            val okHttpClient = OkHttpClient
                .Builder()
                .addInterceptor(httpLoggingInterceptorBody)
                .build()
            retrofitBuilder = retrofitBuilder
                .client(okHttpClient)
        }
        val retrofit = retrofitBuilder
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
        mNewsApi = retrofit.create(NewsApi::class.java)
    }

    fun getNewStories(): Observable<Array<Int>> {
        return mNewsApi.getNewStories()
    }

    fun getItem(id: Int): Observable<ItemBean> {
        return mNewsApi.getItem(id)
    }

}