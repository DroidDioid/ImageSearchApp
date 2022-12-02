package ru.tim.imagesearchapp.webpage

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.tim.imagesearchapp.imagegallery.ImageGalleryFragment

/**
 * ViewModel для экрана [WebPageFragment].
 */
class WebPageViewModel : ViewModel() {

    /** Определяет видимость ProgressBar. */
    val isProgressBarVisible: LiveData<Boolean>
        get() = _isProgressBarVisible
    private val _isProgressBarVisible = MutableLiveData(false)

    /** Определяет прогресс ProgressBar. */
    val progressBarProgress: LiveData<Int>
        get() = _progressBarProgress
    private val _progressBarProgress = MutableLiveData(0)

    /** Задаёт новый прогресс и видимость ProgressBar. */
    fun setProgress(progress: Int) {
        if (progress == 100) {
            _isProgressBarVisible.value = false
        } else {
            _isProgressBarVisible.value = true
            _progressBarProgress.value = progress
        }
    }
}