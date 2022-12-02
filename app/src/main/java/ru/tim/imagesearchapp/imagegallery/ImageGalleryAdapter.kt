package ru.tim.imagesearchapp.imagegallery

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.doOnPreDraw
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import ru.tim.imagesearchapp.R
import ru.tim.imagesearchapp.data.GalleryItem
import ru.tim.imagesearchapp.databinding.GalleryItemBinding

/** Адаптер для списка изображений с поддержкой пагинации. */
class ImageGalleryAdapter(private val callbacks: Callbacks) :
    PagingDataAdapter<GalleryItem, ImageGalleryAdapter.ImageHolder>(ImageComparator) {

    interface Callbacks {
        /**
         * Сообщает о нажатии на элемент списка.
         * @param position позиция нажатого элемента
         * @param imageView представление изображения
         */
        fun onItemClick(position: Int, imageView: ImageView)

        /**
         * Сообщает о готовности изображения.
         * @param position позиция нажатого элемента
         */
        fun onImageReady(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = GalleryItemBinding.inflate(layoutInflater, parent, false)

        return ImageHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageHolder, position: Int) {
        val galleryItem = getItem(position)

        galleryItem?.let {
            holder.bind(it, position)
        }
    }

    inner class ImageHolder(private val binding: GalleryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(galleryItem: GalleryItem, position: Int) {
            binding.imageView.transitionName = position.toString()

            // Задаём соотношение сторон для imageView, чтобы recyclerView отрисовывал imageView
            // и при остутсвии изображения и если есть placeholder
            // с теми же размерами, что и загружаемое изображение,
            // чтобы потом при замене на реальное изображение не менялись размеры imageView и,
            // как следствие, не происходила перестройка и перерисовка recyclerView.
            val ratio: Float = galleryItem.original_width.toFloat() / galleryItem.original_height
            (binding.imageView.layoutParams as ConstraintLayout.LayoutParams).dimensionRatio =
                ratio.toString()
            binding.imageView.requestLayout()

            // Сначала пробуем загрузить только из кэша и по окончании этой попытки
            // запускаем анимацию (shared element transition), если происходит возвращение
            // с экрана детализации. Если дожидаться загрузки из интернета, анимамция может
            // запуститься через слишком большой промежуток времени, что будет выглядеть плохо.
            Glide.with(itemView.context)
                .load(galleryItem.thumbnail)
                .onlyRetrieveFromCache(true)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        callbacks.onImageReady(position)
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        callbacks.onImageReady(position)
                        return false
                    }

                })
                .error(
                    // Если в кэше нет изображения,
                    // то сначала пробуем загрузить thumbnail, затем original
                    Glide.with(itemView.context)
                        .load(galleryItem.thumbnail)
                        .error(
                            Glide.with(itemView.context)
                                .load(galleryItem.original)
                        )
                )
                .into(binding.imageView)

            itemView.setOnClickListener {
                callbacks.onItemClick(position, binding.imageView)
            }
        }
    }

    object ImageComparator : DiffUtil.ItemCallback<GalleryItem>() {
        override fun areItemsTheSame(oldItem: GalleryItem, newItem: GalleryItem): Boolean {
            return oldItem.original == newItem.original
        }

        override fun areContentsTheSame(oldItem: GalleryItem, newItem: GalleryItem): Boolean {
            return oldItem == newItem
        }
    }
}