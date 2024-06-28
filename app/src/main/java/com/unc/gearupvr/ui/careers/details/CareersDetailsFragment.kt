package com.unc.gearupvr.ui.careers.details

import android.content.ActivityNotFoundException
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.unc.gearupvr.BuildConfig
import com.unc.gearupvr.R
import com.unc.gearupvr.components.video_player.VideoPlayerActivity
import com.unc.gearupvr.components.video_player.rendering.Utils
import com.unc.gearupvr.databinding.UniversityDetailsFragmentBinding
import com.unc.gearupvr.model.Careers
import com.unc.gearupvr.ui.university_details.UniversityDetailsViewModel
import com.unc.gearupvr.utils.ExternalLinks
import com.unc.gearupvr.utils.UNWebViewClient
import com.unc.gearupvr.utils.WebPageType
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.alert

class CareersDetailsFragment : Fragment() {

    companion object {
        fun newInstance(careers: Careers) = CareersDetailsFragment().apply {
            this.careers = careers
        }
    }

    lateinit var careers: Careers
    private lateinit var viewModel: UniversityDetailsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProviders.of(this).get(UniversityDetailsViewModel::class.java)
        val binding = UniversityDetailsFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        //  binding.college = college
        binding.viewModel = viewModel
        val webView = binding.webView
        webView.settings.blockNetworkImage = false
        webView.settings.allowContentAccess = true
        webView.settings.defaultTextEncodingName = "utf-8"
        webView.webViewClient =
            activity?.let {
                UNWebViewClient(
                    binding.indicatorView.avi,
                    it,
                    WebPageType.CustomPage
                )
            }!!

        //Show/Hide activity indicator based on viewModel busy status
        viewModel.isBusy.observe(this, Observer {
            if (it) {
                binding.indicatorView.avi.show()
            } else {
                binding.indicatorView.avi.hide()
            }
        })

        when {
            !careers.uid.isNullOrEmpty() -> {
                viewModel.loadCareerData(careers.uid)
            }
            else -> {
                val alert =
                    alert(context?.getString(R.string.generic_error_message) ?: "")
                alert.positiveButton("OK") {
                    activity?.onBackPressed()
                }
                alert.show()
            }
        }

        viewModel.page.observe(this, Observer {

            if (it != null) {

                if (it.video != null) {
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
                }

                binding.parentLayout.visibility = View.VISIBLE
                val baseURL = "https://" + BuildConfig.API_BASE
                it.lightModeContent?.let { it1 ->
                    webView.loadDataWithBaseURL(
                        baseURL,
                        it1,
                        "text/html; charset=utf-8",
                        "UTF-8",
                        ""
                    )
                }
                val socialMediaUrl: String? = it.facebookUrl
                binding.iconFb.onClick {
                    socialMediaUrl?.let { url ->
                        ExternalLinks.openUrl(
                            context ?: return@let,
                            url
                        )
                    }
                }
                val twitterUrl = it.twitterUrl
                binding.iconTwitter.onClick {
                    twitterUrl?.let { url ->
                        ExternalLinks.openUrl(
                            context ?: return@let,
                            url
                        )
                    }
                }
                val webUrl = it.website
                binding.iconWeb.onClick {
                    webUrl?.let { url ->
                        ExternalLinks.openUrl(
                            context ?: return@let,
                            url
                        )
                    }
                }
                val linkedInUrl = it.linkedInUrl
                binding.iconLinkedin.onClick {
                    linkedInUrl?.let { url ->
                        ExternalLinks.openUrl(
                            context ?: return@let,
                            url
                        )
                    }
                }
                binding.buttonSendSms.show()
                val uri = Uri.parse("smsto:" + it.phoneNumber)
                binding.buttonSendSms.onClick {
                    activity?.applicationContext?.let { context ->
                        ExternalLinks.sendSms(
                            context,
                            uri
                        )
                    }
                }
                val instaUrl = it.instagramUrl
                binding.iconInstagram.onClick {
                    instaUrl?.let { url ->
                        ExternalLinks.openUrl(
                            context ?: return@let,
                            url
                        )
                    }
                }
                val emailId = it.email
                binding.icEmail.onClick {
                    try {
                        context?.let { context ->
                            ExternalLinks.openUrl(
                                context,
                                "mailto:$emailId?subject=" + activity?.getString(R.string.app_name)
                            )
                        }
                    } catch (e: ActivityNotFoundException) {
                        val alert =
                            alert(getString(R.string.email_not_config))
                        alert.positiveButton(getString(R.string.ok_button)) {}
                        alert.show()
                    }
                }

            } else {
                binding.videoThumb.visibility = View.GONE
                binding.playButton.visibility = View.GONE
            }

        })

        viewModel.isUrlError.observe(this, Observer {
            if (it) {
                val alert =
                    alert(context?.getString(R.string.url_parse_error) ?: "")
                alert.positiveButton("OK") { activity?.onBackPressed() }
                alert.show()

            }
        })
        viewModel.isPhone.observe(this, Observer {
            if (it) {
                binding.scrollView.viewTreeObserver
                    .addOnScrollChangedListener {
                        if (binding.scrollView.getChildAt(0).bottom
                            <= (binding.scrollView.height + binding.scrollView.scrollY)
                        ) {
                            binding.buttonSendSms.hide()
                        } else {
                            binding.buttonSendSms.show()
                        }
                    }

            }
        })
        binding.backButton.onClick {
            activity?.onBackPressed()
        }

        return binding.root

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(UniversityDetailsViewModel::class.java)
        // TODO: Use the ViewModel
    }

}
