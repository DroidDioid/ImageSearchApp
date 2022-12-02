package ru.tim.imagesearchapp.data.source.api

import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Response

private const val SERP_API_KEY = "13378bd1a89ab290028f74c6cd64c30e2cdb2a3d1d0b07e3fe11faa2a9c148aa"

/**
 * Перехватчик запросов.
 */
class ImageSearchInterceptor : Interceptor {

    /** Добавляет поля общие для всех запросов. */
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val newUrl: HttpUrl = originalRequest.url().newBuilder()
            .addQueryParameter("tbm", "isch")
            .addQueryParameter("api_key", SERP_API_KEY)
            .addQueryParameter("engine", "google")
            .addQueryParameter("output", "json")
            .addQueryParameter("safe", "active")
            .build()

        val newRequest = originalRequest.newBuilder()
            .url(newUrl)
            .build()

        return chain.proceed(newRequest)
    }

}