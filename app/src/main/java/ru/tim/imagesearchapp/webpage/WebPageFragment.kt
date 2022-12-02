package ru.tim.imagesearchapp.webpage

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import ru.tim.imagesearchapp.databinding.FragmentWebpageBinding

/** Создаёт и отображает экран, показывающий веб-страницы сайтов. */
class WebPageFragment : Fragment() {

    private val webViewModel: WebPageViewModel by viewModels()
    private lateinit var binding: FragmentWebpageBinding
    private val args: WebPageFragmentArgs by navArgs()

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentWebpageBinding.inflate(inflater, container, false)

        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = webViewModel

        binding.webView.apply {
            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    webViewModel.setProgress(newProgress)
                }

                override fun onReceivedTitle(view: WebView?, title: String?) {
                    (activity as AppCompatActivity?)?.supportActionBar?.title = title
                }
            }
            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    (activity as AppCompatActivity?)?.supportActionBar?.subtitle = url
                }
            }
            loadUrl(args.link)
            settings.javaScriptEnabled = true
            settings.builtInZoomControls = true
            settings.displayZoomControls = false
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // изменяем поведение кнопки назад для возможности возвращения внутри WebView
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (binding.webView.canGoBack()) {
                binding.webView.goBack()
            } else {
                this.isEnabled = false
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // Убираем подзаголовок
        (requireActivity() as AppCompatActivity).supportActionBar?.subtitle = null
    }
}