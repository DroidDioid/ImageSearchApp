package ru.tim.imagesearchapp.data.source

import androidx.paging.PagingSource
import androidx.paging.PagingState
import ru.tim.imagesearchapp.data.GalleryItem
/**
 * Предоставляет данные с поддержкой постраничной загрузки.
 * @param query запрос по которому осуществляется поиск.
 */
class ImageGalleryPagingSource(private val query: String) : PagingSource<Int, GalleryItem>() {

    override fun getRefreshKey(state: PagingState<Int, GalleryItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    /**
     * Выполняет запрос поиска изображений для следущей страницы и возвращает результат в виде
     * [LoadResult.Page][androidx.paging.PagingSource.LoadResult.Page] при успешной загрузке или
     * [LoadResult.Error][androidx.paging.PagingSource.LoadResult.Error] при ошибке загрузки.
     */
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, GalleryItem> {
        return try {
            val pageNumber = params.key ?: 0
            val response = ImageRepository.searchImages(query, pageNumber)

            LoadResult.Page(
                data = response.galleryItems,
                prevKey = null,
                nextKey = if (response.galleryItems.isNotEmpty()) pageNumber + 1 else null
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}