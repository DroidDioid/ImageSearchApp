package ru.tim.imagesearchapp.imagegallery

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import ru.tim.imagesearchapp.R
import ru.tim.imagesearchapp.databinding.LoadStateItemBinding

/**
 * Адаптер для отображения в RecyclerView элементов основанных на LoadState,
 * таких как спиннер загрузки и кнопка повторной загрузки в случае ошибки.
 */
class ImageGalleryLoadStateAdapter(
    private val retry: () -> Unit
) : LoadStateAdapter<ImageGalleryLoadStateAdapter.LoadStateViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): LoadStateViewHolder {
        return LoadStateViewHolder(parent, retry)
    }

    override fun onBindViewHolder(holder: LoadStateViewHolder, loadState: LoadState) {
        val layoutParams = holder.itemView.layoutParams as StaggeredGridLayoutManager.LayoutParams
        layoutParams.isFullSpan = true

        holder.bind(loadState)
    }

    class LoadStateViewHolder(parent: ViewGroup, retry: () -> Unit) : RecyclerView.ViewHolder(
        LayoutInflater.from(parent.context)
            .inflate(R.layout.load_state_item, parent, false)
    ) {
        private val binding = LoadStateItemBinding.bind(itemView)
        private val progressBar: ProgressBar = binding.progressBar
        private val retry: ImageButton = binding.retryButton
            .also {
                it.setOnClickListener { retry() }
            }

        fun bind(loadState: LoadState) {
            progressBar.isVisible = loadState is LoadState.Loading
            retry.isVisible = loadState is LoadState.Error
        }
    }
}