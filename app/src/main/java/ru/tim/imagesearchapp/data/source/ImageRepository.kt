package ru.tim.imagesearchapp.data.source

import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ru.tim.imagesearchapp.data.source.api.ImageResponse
import ru.tim.imagesearchapp.data.source.api.SerpApi
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/** Репозиторий изображений в виде синглтона. */
object ImageRepository {

    private const val TAG = "ImageRepository"
    private val serpApi = SerpApi.api
    private lateinit var serpRequest: Call<ImageResponse>

    /**
     * Делает запрос к [serpApi], которое создаёт и возвращает
     * GET-запрос поиска изображений в виде Call<ImageResponse>
     * по запросу [query] и номеру страницы [page].
     */
    private fun searchImagesRequest(query: String, page: Int): Call<ImageResponse> {
        return serpApi.searchImages(query, page)
    }

    /**
     * По тексту запроса [query] и номеру страницы [page] получает запрос,
     * который передаёт в [fetchImagesByRequest] и возвращает результат этой функции.
     * */
    suspend fun searchImages(query: String, page: Int): ImageResponse {
        return fetchImagesByRequest(searchImagesRequest(query, page))
    }

    /**
     * Запускает выполнение запроса [serpRequest],
     * возвращает ответ [ImageResponse] со списком изображений.
     */
    private suspend fun fetchImagesByRequest(serpRequest: Call<ImageResponse>): ImageResponse {
        this.serpRequest = serpRequest
        return suspendCoroutine { continuation ->
            serpRequest.enqueue(object : Callback<ImageResponse> {

                override fun onFailure(call: Call<ImageResponse>, t: Throwable) {
                    Log.e(TAG, "Failed to fetch images", t)
                    continuation.resumeWithException(t)
                }

                override fun onResponse(
                    call: Call<ImageResponse>,
                    response: Response<ImageResponse>
                ) {
                    Log.d(TAG, "Response received: ${response.body()}")
                    val imageResponse = response.body() ?: ImageResponse()
                    val filteredGalleryItems = imageResponse.galleryItems.filterNot {
                        it.thumbnail.isBlank() || it.original.isBlank() ||
                                it.original_width == 0 || it.original_height == 0
                    }

                    imageResponse.galleryItems = filteredGalleryItems

                    continuation.resume(imageResponse)
                }
            })
        }
    }

    /** Прерывает выполнение запроса. */
    fun cancelRequestInFlight() {
        if (::serpRequest.isInitialized) serpRequest.cancel()
    }
}