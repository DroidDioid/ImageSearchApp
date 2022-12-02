package ru.tim.imagesearchapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import ru.tim.imagesearchapp.data.GalleryItem
import ru.tim.imagesearchapp.data.source.ImageGalleryPagingSource
import ru.tim.imagesearchapp.data.source.ImageRepository
import ru.tim.imagesearchapp.imagegallery.ImageGalleryFragment

/**
 * Общая ViewModel для фрагментов.
 * @param app контекст приложения
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SharedImageViewModel(app: Application) : AndroidViewModel(app) {

    /** Текущий запрос */
    private val _queryFlow = MutableStateFlow("")
    val queryFlow: StateFlow<String>
        get() = _queryFlow

    /** Текущий запрос */
    val searchTerm: String
        get() = _queryFlow.value

    init {
        _queryFlow.value = QueryPreferences.getStoredQuery(getApplication())
    }

    /** Последняя сохранённая позиция элемента общая для разных фрагментов*/
    var sharedItemPosition: Int = 0

    /**
     * Поток данных [PagingData], содержащий изображения [GalleryItem].
     */
    val galleryItemFlow = _queryFlow.flatMapLatest { query ->
        if (query.isNotBlank()) {
            Pager(PagingConfig(pageSize = 30)) {
                ImageGalleryPagingSource(query)
            }.flow
                .cachedIn(viewModelScope)
        } else {
            emptyFlow()
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)


    /** Запускает поиск изображений.
     *
     * Сохраняет новый запрос в [QueryPreferences].
     */
    fun searchImages(query: String) {
        QueryPreferences.setStoredQuery(getApplication(), query)
        _queryFlow.value = query
    }

    override fun onCleared() {
        super.onCleared()
        // Прерываем запрос
        ImageRepository.cancelRequestInFlight()
    }
}