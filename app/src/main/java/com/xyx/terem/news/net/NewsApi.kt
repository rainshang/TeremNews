package com.xyx.terem.news.net

import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path

interface NewsApi {

    @GET("/v0/newstories.json?print=pretty")
    fun getNewStories(): Observable<Array<Int>>

    @GET("v0/item/{id}.json?print=pretty")
    fun getItem(@Path("id") id: Int): Observable<ItemBean>
}