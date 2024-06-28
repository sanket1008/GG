package com.unc.gearupvr.ui.web_view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.unc.gearupvr.R
import com.unc.gearupvr.databinding.ActivityWebViewBinding
import com.unc.gearupvr.utils.FilterConstants
import com.unc.gearupvr.utils.UNWebViewClient
import com.unc.gearupvr.utils.WebPageType
import org.jetbrains.anko.sdk27.coroutines.onClick


class WebViewActivity : AppCompatActivity() {

    val const = FilterConstants()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val binding: ActivityWebViewBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_web_view)
        val webView = binding.webView
        webView.settings.blockNetworkImage = false
        webView.settings.allowContentAccess = true
        webView.settings.defaultTextEncodingName = "utf-8"
        //for enabling JS inside the webView please un-comment the below line
        //webView.settings.javaScriptEnabled = true
        val url = intent.getStringExtra(urlTag)
        webView.webViewClient =
            UNWebViewClient(
                binding.indicatorView.avi,
                this,
                WebPageType.WebViewFragment
            )
        webView.loadUrl(url?:"")
        binding.navTitle.text = webView.title
        webView.webChromeClient = object : WebChromeClient() {
            override fun onReceivedTitle(view: WebView, title: String) {
                super.onReceivedTitle(view, title)
                if (!TextUtils.isEmpty(title)) {
                    binding.navTitle.text = title
                }
            }
        }
        webView.canGoBack()
        webView.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
            if (!(keyCode != KeyEvent.KEYCODE_BACK || event.action != KeyEvent.ACTION_UP || !webView.canGoBack())
            ) {
                webView.goBack()
                return@OnKeyListener true
            }
            false
        })
        binding.backButton.onClick { if (webView.canGoBack()) webView.goBack() else onBackPressed() }

        binding.closePage.onClick { onBackPressed() }
    }

    companion object {

        fun createIntent(
            context: Context?,
            url: String? = null
        ): Intent {
            val intent = Intent(context, WebViewActivity::class.java)
            intent.putExtra(urlTag, url)
            return intent
        }

        const val urlTag = "url"
    }


}
