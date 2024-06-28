package com.unc.gearupvr.utils

import android.annotation.TargetApi
import android.app.Activity
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Build
import android.webkit.*
import com.unc.gearupvr.R
import com.unc.gearupvr.ui.web_view.WebViewActivity
import com.wang.avi.AVLoadingIndicatorView
import org.jetbrains.anko.alert

enum class WebPageType {
    CustomPage,
    ExternalPage,
    WebViewFragment;
}

class UNWebViewClient(
    private val loader: AVLoadingIndicatorView?,
    private val activity: Activity,
    private val typeOfView: WebPageType
) :
    WebViewClient() {

    private var uri: Uri? = null

    override fun onReceivedSslError(
        view: WebView?,
        handler: SslErrorHandler?,
        error: SslError?
    ) {
        //TODO: should bypass the ssl error only for dev and qa build variant only
        // handler?.proceed()
        //handler?.cancel()
        //super.onReceivedSslError(view, handler, error)
        DialogUtils.showAlert(
            activity,
            activity.getString(R.string.app_name),
            activity.getString(R.string.common_error_msg),
            positiveButtonText = activity.getString(R.string.ok_button)

        ) { value ->
            when (value) {

                DialogUtils.positiveButton -> {

                }

            }
        }
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        this.uri = Uri.parse(url)
        loader?.show()
    }


    private fun showUserNavigationOptionAlert(url: String) {
        if (url.isNotEmpty()) {
            DialogUtils.showAlert(
                activity,
                activity.getString(R.string.app_name),
                activity.getString(R.string.link_opening_dialog_title),
                activity.getString(R.string.in_app_browser_tag),
                activity.getString(R.string.external_browser_tag),
                activity.getString(R.string.cancel_tag)
            ) { value ->
                when (value) {
                    DialogUtils.negativeButton -> {
                        url.let { url ->
                            ExternalLinks.openUrl(
                                activity,
                                url
                            )
                        }
                    }
                    DialogUtils.positiveButton -> {
                        activity.startActivity(
                            WebViewActivity.createIntent(
                                context = activity,
                                url = url
                            )
                        )
                    }
                    DialogUtils.neutralButton -> {
                    }
                }
            }
        }
    }

    private fun shouldOverrideUrlLoading(url: String): Boolean = when (typeOfView) {
        WebPageType.CustomPage -> {
            showUserNavigationOptionAlert(url)
            true
        }
        WebPageType.ExternalPage -> {
            when {
                uri == null -> {
                    false
                }
                uri?.host != Uri.parse(url).host -> {
                    showUserNavigationOptionAlert(url)
                    true
                }

                else -> false
            }
        }
        WebPageType.WebViewFragment -> false
    }

    @Deprecated("Deprecated in Java")
    override fun shouldOverrideUrlLoading(view: WebView?, request: String): Boolean {
        return shouldOverrideUrlLoading(request)
    }

    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        return shouldOverrideUrlLoading(request?.url.toString())
    }


    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        loader?.hide()
    }

    @Deprecated("Deprecated in Java")
    override fun onReceivedError(
        view: WebView?,
        errorCode: Int,
        description: String?,
        failingUrl: String?
    ) {
        super.onReceivedError(view, errorCode, description, failingUrl)
        loader?.hide()
        val alert =
            view?.context?.alert(
                view.context?.getString(R.string.generic_error_message) + " " + description
            )
        if (alert != null) {
            alert.positiveButton("OK") {}
            alert.show()
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        super.onReceivedError(view, request, error)
        loader?.hide()
        val alert =
            view?.context?.alert(
                view.context?.getString(R.string.generic_error_message) + " " + error?.description
            )
        if (alert != null) {
            alert.positiveButton("OK") {}
            alert.show()
        }
    }
}
