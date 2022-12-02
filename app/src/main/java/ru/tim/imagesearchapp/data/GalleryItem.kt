package ru.tim.imagesearchapp.data

/** Содержит информацию об изображении. */
data class GalleryItem(
    /** Краткое описание изображения. */
    var title: String = "",
    /** Доменное имя оригинала изображения [original]. */
    var source: String = "",
    /** Ссылка на страницу, предоставляющую изображение. */
    var link: String = "",
    /** URL миниатюры изображения. */
    var thumbnail: String = "",
    /** URL оригинала изображения. */
    var original: String = "",
    /** Индекс изображения. */
    var position: Int = 0,
    /** Ширина оригинального изображения [original]. */
    var original_width: Int = 0,
    /** Высота оригинального изображения [original]. */
    var original_height: Int = 0,
)