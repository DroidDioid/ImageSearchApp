package ru.tim.imagesearchapp.imagegallery

import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.core.app.SharedElementCallback
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.transition.TransitionInflater
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import ru.tim.imagesearchapp.R
import ru.tim.imagesearchapp.SharedImageViewModel
import ru.tim.imagesearchapp.databinding.FragmentImageGalleryBinding

/**
 * Создаёт и отображает экран со списком изображений и строкой поиска.
 * Реализует интерфейс [ImageGalleryAdapter.Callbacks] для получения данных из адаптера списка.
 */
class ImageGalleryFragment : Fragment(), ImageGalleryAdapter.Callbacks {

    private val sharedViewModel: SharedImageViewModel by activityViewModels()
    private val imageGalleryViewModel: ImageGalleryViewModel by viewModels()
    private lateinit var binding: FragmentImageGalleryBinding
    private val imagePagingAdapter = ImageGalleryAdapter(this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentImageGalleryBinding.inflate(inflater, container, false)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = imageGalleryViewModel

        setupRecyclerView()
        setupRefreshLayout()

        prepareTransitions()
        // Откладываем анимацию перехода, пока не загрузится
        // нужное изображение в адаптере RecyclerView
        postponeEnterTransition()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addMenu()

        // Передаём текущий запрос из общей ViewModel в частную
        viewLifecycleOwner.lifecycleScope.launch {
            sharedViewModel.queryFlow.collectLatest { query ->
                imageGalleryViewModel.setQuery(query)
            }
        }

        // Переходим к нужной позиции, если она сохранена, и если это не пересоздание фрагмента
        binding.recyclerView.doOnPreDraw {
            if (savedInstanceState == null) {
                binding.recyclerView.scrollToPosition(sharedViewModel.sharedItemPosition)
            }
        }

        // Следим за поступлением данных и передаём в адаптер
        viewLifecycleOwner.lifecycleScope.launch {
            sharedViewModel.galleryItemFlow.collectLatest { pagingData ->
                pagingData?.let {
                    imagePagingAdapter.submitData(pagingData)
                }
            }
        }

        // Отслеживаем состояния загрузки
        viewLifecycleOwner.lifecycleScope.launch {
            imagePagingAdapter.loadStateFlow
                // Оставляем только вызовы инициированные изменением состояния refresh
                .distinctUntilChangedBy { it.refresh }
                .collectLatest { loadStates ->
                    imageGalleryViewModel.processLoadStates(
                        loadStates,
                        imagePagingAdapter.itemCount
                    )
                }
        }
    }

    /**
     * Создаёт и добавляет меню.
     *
     * Меню состоит из кнопки поиска.
     */
    private fun addMenu() {
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_image_gallery, menu)

                val searchItem: MenuItem = menu.findItem(R.id.menu_item_search)
                val searchView = searchItem.actionView as SearchView

                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {

                    override fun onQueryTextSubmit(query: String?): Boolean {
                        query?.takeIf {
                            query != sharedViewModel.searchTerm
                        }?.let {
                            // Cдвигаем список вверх для новых данных
                            binding.recyclerView.scrollToPosition(0)

                            sharedViewModel.searchImages(query)

                            // Сворачиваем поле поиска в значок
                            searchView.setQuery("", false)
                            searchView.isIconified = true
                        }
                        return true
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        return false
                    }

                })

                // Заполняет поле поиска при его разворачивании текущим поисковым запросом
                searchView.setOnSearchClickListener {
                    searchView.setQuery(sharedViewModel.searchTerm, false)
                }
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }


    /**
     * Настраивает SwipeRefreshLayout, задавая цветовую схему и listener.
     */
    private fun setupRefreshLayout() {
        binding.swipeRefreshLayout.apply {
            setOnRefreshListener {
                imageGalleryViewModel.startRefreshing()
                imagePagingAdapter.refresh()
            }
            setColorSchemeResources(
                R.color.purple_500,
                R.color.purple_200
            )
        }
    }

    /**
     * Настраивает RecyclerView, задавая ему layoutManager и adapter.
     *
     * Для настройки layoutManager используется разное число столбцов
     * в зависимости от ориентации экрана.
     */
    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager =
            if (requireActivity().resources.configuration.orientation
                == Configuration.ORIENTATION_PORTRAIT
            ) {
                StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
            } else {
                StaggeredGridLayoutManager(5, StaggeredGridLayoutManager.VERTICAL)
            }
        binding.recyclerView.itemAnimator = null

        binding.recyclerView.adapter = imagePagingAdapter.withLoadStateFooter(
            ImageGalleryLoadStateAdapter { imagePagingAdapter.retry() }
        )
    }

    /**
     * Запускает экран детализации изображения при нажатии на элемент списка.
     * @param position позиция нажатого элемента
     * @param imageView представление изображения
     */
    override fun onItemClick(position: Int, imageView: ImageView) {
        sharedViewModel.sharedItemPosition = position
        val extras = FragmentNavigatorExtras(imageView to imageView.transitionName)
        findNavController().navigate(R.id.actionGalleryToDetail, null, null, extras)
    }

    /**
     * Запускает отложенную анимацию транзакции по готовности изображения.
     * @param position позиция нажатого элемента
     */
    override fun onImageReady(position: Int) {
        if (position == sharedViewModel.sharedItemPosition) {
            startPostponedEnterTransition()
        }
    }

    /**
     * Настраивает слушателя для изменения общего элемента (shared element)
     * при изменении общей позиции для корректного отображения анимации.
     */
    private fun prepareTransitions() {
        // Задаём анимацию при закрытии фрагмента
        exitTransition = TransitionInflater.from(requireContext())
            .inflateTransition(R.transition.grid_exit_transition)

        setExitSharedElementCallback(object : SharedElementCallback() {
            override fun onMapSharedElements(
                names: MutableList<String>,
                sharedElements: MutableMap<String, View>
            ) {
                val position = sharedViewModel.sharedItemPosition
                val selectedViewHolder =
                    binding.recyclerView.findViewHolderForAdapterPosition(position)
                selectedViewHolder?.itemView?.findViewById<ImageView>(R.id.imageView)?.let {
                    sharedElements[names[0]] = it
                }
            }
        })
    }

    companion object {
        private const val TAG = "ImageGalleryFragment"
    }
}