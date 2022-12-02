package ru.tim.imagesearchapp.imagegallery

import android.app.Application
import android.graphics.drawable.Drawable
import androidx.lifecycle.*
import androidx.paging.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import ru.tim.imagesearchapp.QueryPreferences
import ru.tim.imagesearchapp.R
import ru.tim.imagesearchapp.data.GalleryItem
import ru.tim.imagesearchapp.data.source.ImageGalleryPagingSource
import ru.tim.imagesearchapp.data.source.ImageRepository

/**
 * ViewModel для экрана [ImageGalleryFragment].
 */
class ImageGalleryViewModel : ViewModel() {

    /**
     * Показывает обновляется ли swipeRefreshLayout.
     *
     * Возвращает true, только когда происходит обновление существующих данных, а не поиск новых.
     */
    val isRefreshing: LiveData<Boolean>
        get() = _isRefreshing
    private val _isRefreshing = MutableLiveData(false)

    /** Определяет видимость SwipeRefreshLayout. */
    val isRefreshLayoutVisible: LiveData<Boolean>
        get() = _isRefreshLayoutVisible
    private val _isRefreshLayoutVisible = MutableLiveData(false)

    /** Определяет видимость ProgressBar. */
    val isProgressBarVisible: LiveData<Boolean>
        get() = _isProgressBarVisible
    private val _isProgressBarVisible = MutableLiveData(false)

    /** Определяет видимость RecyclerView. */
    val isRecyclerViewVisible: LiveData<Boolean>
        get() = _isRecyclerViewVisible
    private val _isRecyclerViewVisible = MutableLiveData(false)

    /** Определяет видимость заменителя списка. */
    val isListPlaceholderVisible: LiveData<Boolean>
        get() = _isListPlaceholderVisible
    private val _isListPlaceholderVisible = MutableLiveData(false)

    /** Хранит ResId строки для отображения сообщения заменителя списка. */
    val placeholderMessageResId: LiveData<Int>
        get() = _placeholderMessageResId
    private val _placeholderMessageResId = MutableLiveData(R.string.error_page_text)


    /** Хранит ResId изображения для отображения изображения заменителя списка. */
    val placeholderImageResId: LiveData<Int>
        get() = _placeholderImageResId
    private val _placeholderImageResId = MutableLiveData(R.drawable.error_24)

    /** Хранит ResId строки для описания изображения заменителя списка. */
    val placeholderImageDescriptionResId: LiveData<Int>
        get() = _placeholderImageDescriptionResId
    private val _placeholderImageDescriptionResId = MutableLiveData(R.string.error_page_image)


    /** Текущий запрос */
    private val _query = MutableLiveData("")

    /** Задаёт текущий запрос */
    fun setQuery(query: String) {
        _query.value = query
    }

    /** Определяет видимость стартового изображения */
    val isStartImageVisible = _query.map { query ->
        query.isBlank()
    }

    /** Отображает ProgressBar у SwipeRefreshLayout. */
    fun startRefreshing() {
        _isRefreshing.value = true
    }

    /**
     * Обрабатывает состояния загрузки данных.
     *
     * Управляет видимостью элементов в зависимости от состояния.
     * @param loadStates состояния загрузки
     * @param listItemCount количество элементов в списке
     */
    fun processLoadStates(loadStates: CombinedLoadStates, listItemCount: Int) {
        // Отображаем swipeRefreshLayout, если не идёт загрузка данных или
        // происходит обновление текущих данных
        if (loadStates.refresh !is LoadState.Loading) _isRefreshing.value = false
        _isRefreshLayoutVisible.value =
            loadStates.refresh !is LoadState.Loading || _isRefreshing.value == true

        // Отображаем progress bar, если идёт поиск новых данных, а не обновление текущих
        _isProgressBarVisible.value =
            loadStates.refresh is LoadState.Loading && _isRefreshing.value == false

        if (loadStates.refresh is LoadState.NotLoading) {
            // По завершении загрузки проверяем не пустой ли список
            if (listItemCount == 0) {
                // Ставим заменитель в случае пустого списка
                _isRecyclerViewVisible.value = false
                _isListPlaceholderVisible.value = true

                _placeholderMessageResId.value = R.string.no_images_page_text
                _placeholderImageResId.value = R.drawable.no_images_24
                _placeholderImageDescriptionResId.value = R.string.no_images_page_image
            } else {
                // Отображаем список, если он не пустой
                _isListPlaceholderVisible.value = false
                _isRecyclerViewVisible.value = true
            }
        } else if (loadStates.refresh is LoadState.Error) {
            // В случае ошибки при загрузке ставим заменитель
            _isRecyclerViewVisible.value = false
            _isListPlaceholderVisible.value = true

            _placeholderMessageResId.value = R.string.error_page_text
            _placeholderImageResId.value = R.drawable.error_24
            _placeholderImageDescriptionResId.value = R.string.error_page_image
        }
    }
}