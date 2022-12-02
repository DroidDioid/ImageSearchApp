package ru.tim.imagesearchapp.data.source.api

import com.google.gson.annotations.SerializedName
import ru.tim.imagesearchapp.data.GalleryItem

/** Ответ с сервера, содержащий список изображений. */
class ImageResponse {
    /** Список элементов с информацией об изображениях. */
    @SerializedName("images_results")
    var galleryItems: List<GalleryItem> = listOf()
}