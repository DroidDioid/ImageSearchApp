package ru.tim.imagesearchapp.data.source.api

import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/** Интерфейс реализуемый Retrofit, предоставляет доступ к [Serp API](https://serpapi.com). */
interface SerpApi {

    /**
     * Создаёт и возвращает GET-запрос поиска изображений в виде [Call<ImageResponse>][retrofit2.Call]
     * по запросу [query] и номеру страницы [page].
     */
    @GET("/search.json")
    fun searchImages(@Query("q") query: String, @Query("ijn") page: Int): Call<ImageResponse>

    /** Фабрика для создания синглтона [api]. */
    companion object Factory {

        private const val BASE_URL = "https://serpapi.com"

        private val client = OkHttpClient.Builder()
            .addInterceptor(ImageSearchInterceptor())
            .build()

        private val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        /** Реализация интерфейса [SerpApi]. */
        val api: SerpApi = retrofit.create(SerpApi::class.java)
    }
}