package ru.tim.imagesearchapp.detailimage

import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.github.chrisbanes.photoview.OnScaleChangedListener
import ru.tim.imagesearchapp.data.GalleryItem
import ru.tim.imagesearchapp.databinding.DetailItemBinding

/** Адаптер для ViewPager с поддержкой пагинации. */
class DetailImageAdapter :
    PagingDataAdapter<GalleryItem, DetailImageAdapter.ImageHolder>(ImageComparator) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = DetailItemBinding.inflate(layoutInflater, parent, false)

        return ImageHolder(binding)
    }

    override fun onBindViewHolder(holder: ImageHolder, position: Int) {
        val galleryItem = getItem(position)

        galleryItem?.let {
            holder.bind(it, position)
        }
    }

    /** Возвращает заголовок, связанный с картинкой. */
    fun getItemTitle(position: Int) = getItem(position)?.title

    /** Возвращает ссылку на веб-страницу, откуда взята картинка. */
    fun getItemLink(position: Int) = getItem(position)?.link

    class ImageHolder(private val binding: DetailItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(galleryItem: GalleryItem, position: Int) {
            binding.root.tag = position
            binding.detailImage.apply {
                transitionName = position.toString()
                // Не даём ViewPager переключать элементы, пока изображение увеличено,
                // чтобы не было несвоевременного переключения страниц
                // при увеличении и просмотре изображения (можно отключить)
                setOnScaleChangeListener { _, _, _ -> setAllowParentInterceptOnEdge(scale < 1.05f) }
            }

            // Показываем прогресс бар, делаем изображение неактивным (недоступным для увеличения)
            binding.progressBar.isVisible = true
            binding.detailImage.isEnabled = false

            Glide.with(itemView.context)
                .load(galleryItem.original)
                .override(1000) // Чтобы не было ошибки отрисовки больших фото
                .thumbnail(
                    Glide.with(itemView.context)
                        .load(galleryItem.thumbnail)

                )
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.progressBar.isVisible = false
                        binding.detailImage.isEnabled = true
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        binding.progressBar.isVisible = false
                        binding.detailImage.isEnabled = true
                        return false
                    }

                })
                .into(binding.detailImage)
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