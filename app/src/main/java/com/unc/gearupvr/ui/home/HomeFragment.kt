package com.unc.gearupvr.ui.home

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.unc.gearupvr.R
import com.unc.gearupvr.components.video_player.VideoPlayerActivity
import com.unc.gearupvr.components.video_player.rendering.Utils
import com.unc.gearupvr.databinding.HomeFragmentBinding
import com.unc.gearupvr.ui.detail_page.DetailPageFragment
import com.unc.gearupvr.ui.menu.MenuActivity
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.alert

class HomeFragment(private val title: String) : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var recyclerView: RecyclerView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val binding = HomeFragmentBinding.inflate(inflater, container, false)
        val indicator = binding.loader.avi

        binding.homeTitle.text = title
        binding.scrollView.isSmoothScrollingEnabled = true

        binding.swipeContainer.setOnRefreshListener {
            homeViewModel.loadData()
        }

        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.isNestedScrollingEnabled = false

        homeViewModel.isBusy.observe(this, Observer {
            if (homeViewModel.dashboardData.value != null) {
                binding.swipeContainer.isRefreshing = it

            } else {
                if (it) indicator.smoothToShow()
            }
            if (!it && indicator.visibility == View.VISIBLE) {
                indicator.smoothToHide()
            }
        })

        homeViewModel.dashboardData.observe(this, Observer {
            if (it != null) {
                if (it.video != null) {
                    val videoTitleTextView = binding.videoTitle
                    videoTitleTextView.text = it.video.name
                    videoTitleTextView.visibility = View.VISIBLE
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

                val adapter = DashboardTileAdapter(it.activeTiles, homeViewModel)
                recyclerView.adapter = adapter
            }
        })

        homeViewModel.selectedTile.observe(this, Observer {
            if (it != null) {
                (activity as MenuActivity).changeFragment(
                    DetailPageFragment.newInstance(it),
                    isFirst = false,
                    shouldAnimate = true
                )
                homeViewModel.selectedTile.postValue(null)

            }
        })

        return binding.root
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        recyclerView.adapter?.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        homeViewModel.selectedTile.removeObservers(this)
        homeViewModel.dashboardData.removeObservers(this)
        homeViewModel.isBusy.removeObservers(this)
        super.onDestroyView()
    }
}