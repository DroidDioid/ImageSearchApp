package ru.tim.imagesearchapp.detailimage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.SharedElementCallback
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.transition.TransitionInflater
import androidx.viewpager2.widget.ViewPager2
import com.github.chrisbanes.photoview.PhotoView
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.tim.imagesearchapp.R
import ru.tim.imagesearchapp.SharedImageViewModel
import ru.tim.imagesearchapp.databinding.FragmentDetailImageBinding

/** Создаёт и отображает экран детального просмотра изображений. */
class DetailImageFragment : Fragment() {

    private val sharedViewModel: SharedImageViewModel by activityViewModels()
    private lateinit var binding: FragmentDetailImageBinding
    private val imagePagingAdapter = DetailImageAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentDetailImageBinding.inflate(inflater, container, false)

        binding.fragment = this

        prepareTransitions()
        // Откладываем выполнение анимации до готовности изображения
        postponeEnterTransition()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewPager.adapter = imagePagingAdapter

        // Очищаем заголовок ActionBar, чтобы он не обрезался при открытии страниц
        (requireActivity() as AppCompatActivity).supportActionBar?.title = null

        // Меняем заголовок ActionBar в зависимости от текущего элемента
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                (requireActivity() as AppCompatActivity).supportActionBar?.title =
                    imagePagingAdapter.getItemTitle(position)
            }
        })

        // Переходим к нужной позиции и запускаем анимацию перехода
        binding.viewPager.doOnPreDraw {
            binding.viewPager.setCurrentItem(sharedViewModel.sharedItemPosition, false)
            startPostponedEnterTransition()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            sharedViewModel.galleryItemFlow.collectLatest { pagingData ->
                pagingData?.let {
                    imagePagingAdapter.submitData(pagingData)
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // Сохраняем позицию общего элемента при закрытии
        sharedViewModel.sharedItemPosition = binding.viewPager.currentItem
    }

    /**
     * Открывает экран [WebPageFragment][ru.tim.imagesearchapp.webpage.WebPageFragment]
     * с веб-страницей сайта, откуда взято изображение.
     */
    fun onWebButtonClick() {
        val link = imagePagingAdapter.getItemLink(binding.viewPager.currentItem) ?: ""
        val action = DetailImageFragmentDirections.actionDetailToWeb(link)
        findNavController().navigate(action)
    }

    /**
     * Настраивает слушателя для изменения общего элемента (shared element)
     * при изменении общей позиции для корректного отображения анимации.
     */
    private fun prepareTransitions() {
        // Задаём анимацию при открытии фрагмента
        sharedElementEnterTransition = TransitionInflater.from(requireContext())
            .inflateTransition(R.transition.image_shared_element_transition)

        setEnterSharedElementCallback(object : SharedElementCallback() {
            override fun onMapSharedElements(
                names: MutableList<String>,
                sharedElements: MutableMap<String, View>
            ) {
                val position = sharedViewModel.sharedItemPosition

                binding.viewPager.findViewWithTag<ViewGroup>(position)
                    ?.findViewById<PhotoView>(R.id.detailImage)
                    ?.let {
                        sharedElements[names[0]] = it
                    }
            }
        })
    }
}