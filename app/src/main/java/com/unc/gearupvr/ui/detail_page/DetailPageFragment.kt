package com.unc.gearupvr.ui.detail_page

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.unc.gearupvr.BuildConfig
import com.unc.gearupvr.R
import com.unc.gearupvr.components.video_player.VideoPlayerActivity
import com.unc.gearupvr.components.video_player.rendering.Utils
import com.unc.gearupvr.databinding.DetailPageFragmentBinding
import com.unc.gearupvr.model.DashboardTile
import com.unc.gearupvr.utils.UNWebViewClient
import com.unc.gearupvr.utils.WebPageType
import com.wang.avi.AVLoadingIndicatorView
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.alert


class DetailPageFragment : Fragment() {

    companion object {
        fun newInstance(tile: DashboardTile) = DetailPageFragment().apply {
            this.tile = tile
        }
    }

    private val viewModel: DetailPageViewModel by lazy {
        ViewModelProviders.of(this).get(DetailPageViewModel::class.java)
    }

    lateinit var tile: DashboardTile

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DetailPageFragmentBinding.inflate(inflater, container, false)

        binding.title.text = tile.title

        if ((activity?.supportFragmentManager?.backStackEntryCount ?: 0) > 0) {
            binding.backButton.visibility = View.VISIBLE
            binding.backButton.onClick {
                if (binding.webView.canGoBack())
                    binding.webView.goBack()
                else
                    activity?.onBackPressed()
            }
        } else {
            binding.backButton.visibility = View.GONE
        }

        //Show/Hide activity indicator based on viewModel busy status
        viewModel.isBusy.observe(this, Observer {
            if (it) {
                binding.indicatorView.avi.show()
            } else {
                binding.indicatorView.avi.hide()
            }
        })

        when {
            tile.page != null && (tile.page ?: return null).isNotEmpty() -> {
                setupWebView(binding.webView, WebPageType.CustomPage, binding.indicatorView.avi)
                viewModel.loadData(tile.page)
            }
            tile.url != null && (tile.url ?: return null).isNotEmpty() -> {
                binding.videoThumb.visibility = View.GONE
                binding.playButton.visibility = View.GONE
                setupWebView(binding.webView, WebPageType.ExternalPage, binding.indicatorView.avi)
                binding.webView.loadUrl(tile.url?:"")
            }
            else -> {
                val alert =
                    alert(context?.getString(R.string.generic_error_message) ?: "")
                alert.positiveButton("OK") {}
                alert.show()
            }
        }

        viewModel.page.observe(this, Observer {

            if (it != null) {

                if (it.video != null) {
                    binding.videoThumb.visibility = View.VISIBLE
                    binding.playButton.visibility = View.VISIBLE
                    Glide.with(this)
                        .load(it.video.imageURL)
                        .centerCrop()
                        .placeholder(R.drawable.video_placeholder)
                        .into(binding.videoThumb)
                    binding.playButton.onClick { _ ->
                        if (Utils.isVRSupported()) {
                            (context)?.let { ctx ->
                                activity?.startActivity(
                                    VideoPlayerActivity.getIntent(
                                        ctx,
                                        it.video
                                    )
                                )
                            }
                        } else {
                            val alert =
                                alert(getString(R.string.vr_video_compatibility_error))
                            alert.positiveButton(getString(R.string.ok_button)) {}
                            alert.show()
                        }
                    }
                } else {
                    binding.videoThumb.visibility = View.GONE
                    binding.playButton.visibility = View.GONE
                }

                val baseURL = "https://" + BuildConfig.API_BASE
                binding.webView.loadDataWithBaseURL(
                    baseURL,
                    it.lightModeContent?:"",
                    "text/html; charset=utf-8",
                    "UTF-8",
                    ""
                )
            } else {
                binding.videoThumb.visibility = View.GONE
                binding.playButton.visibility = View.GONE
            }
        })

        return binding.root
    }

    private fun setupWebView(
        webView: WebView,
        webPageType: WebPageType,
        indicatorView: AVLoadingIndicatorView
    ) {
        webView.canGoBack()
        webView.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
            if (!(keyCode != KeyEvent.KEYCODE_BACK || event.action != KeyEvent.ACTION_UP || !webView.canGoBack())
            ) {
                webView.goBack()
                return@OnKeyListener true
            }
            false
        })
        webView.settings.blockNetworkImage = false
        webView.settings.allowContentAccess = true
        webView.settings.defaultTextEncodingName = "utf-8"
        if (webPageType == WebPageType.ExternalPage) {
            @SuppressLint("SetJavaScriptEnabled") // enabled javascript because in external page alerts and submits are not working
            webView.settings.javaScriptEnabled = true
        }
        webView.webViewClient =
            activity?.let {
                UNWebViewClient(
                    indicatorView,
                    it,
                    webPageType
                )
            }!!
    }

}