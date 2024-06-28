package com.unc.gearupvr.ui.careers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.unc.gearupvr.R
import com.unc.gearupvr.databinding.CareersListFragmentBinding
import com.unc.gearupvr.model.GearupApp
import com.unc.gearupvr.ui.careers.details.CareersDetailsFragment
import com.unc.gearupvr.ui.menu.MenuActivity
import org.jetbrains.anko.sdk27.coroutines.onClick

class CareersListFragment : Fragment() {


    companion object {
        fun newInstance(_title: String): CareersListFragment {
            val fragment = CareersListFragment()
            val args = Bundle()
            args.putString(GearupApp.NAV_TITLE, _title)
            fragment.arguments = args
            return fragment
        }

    }

    private lateinit var viewModel: CareersListViewModel
    lateinit var binding: CareersListFragmentBinding
    lateinit var adapter: CareersListViewAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = CareersListFragmentBinding.inflate(inflater, container, false)
        viewModel = ViewModelProviders.of(this).get(CareersListViewModel::class.java)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.navTitle.text = arguments?.getString(GearupApp.NAV_TITLE) ?: ""
        binding.loader.avi.smoothToHide()
        binding.aviPagination.avi.smoothToHide()

        val backButton = binding.backButton
        backButton.onClick { activity?.onBackPressed() }
        if ((activity?.supportFragmentManager?.backStackEntryCount ?: 0) > 0) {
            backButton.visibility = View.VISIBLE
            backButton.onClick {
                activity?.onBackPressed()
            }
        } else {
            backButton.visibility = View.GONE
        }

        val layoutManager = LinearLayoutManager(activity)
        binding.recyclerView.layoutManager = layoutManager
        val dividerItemDecoration = DividerItemDecoration(
            binding.recyclerView.context,
            layoutManager.orientation
        )
        binding.recyclerView.context.getDrawable(R.drawable.divider_item_decoration)?.let {
            dividerItemDecoration.setDrawable(it)
        }
        binding.recyclerView.addItemDecoration(dividerItemDecoration)
        adapter = CareersListViewAdapter(
            (viewModel.careersList.value ?: return null),
            viewModel
        )
        binding.recyclerView.adapter = adapter
        viewModel.careersList.observe(this, Observer {
            updateNoItemLabel()
            adapter.notifyDataSetChanged()
        })
        viewModel.isBusy.observe(this, Observer {
            updateNoItemLabel()
            if (!it) {
                binding.swipeContainer.isRefreshing = false
                binding.loader.avi.smoothToHide()
                binding.aviPagination.avi.smoothToHide()
            }
        })

        binding.swipeContainer.setOnRefreshListener {
            refreshLoadMore()
        }
        viewModel.selectedCareers.observe(this, Observer {
            if (it != null) {

                (activity as MenuActivity).changeFragment(
                    CareersDetailsFragment.newInstance(careers = it),
                    isFirst = false,
                    shouldAnimate = true
                )
                viewModel.selectedCareers.postValue(null)

            }
        })
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val pastVisibleItems = layoutManager.findFirstVisibleItemPosition()

                if (dy > 0) //check for scroll down
                {
                    when (viewModel.isBusy.value) {
                        false -> {
                            if (visibleItemCount + pastVisibleItems >= totalItemCount) {
                                loadOnScroll()

                            }
                        }

                        else -> {}
                    }
                }
            }
        })
        return binding.root
    }


    override fun onResume() {
        super.onResume()
        loadMoreOnResume()
    }

    private fun loadOnScroll() {
        if (viewModel.loadData(loadMore = true))
            binding.aviPagination.avi.smoothToShow()
    }

    private fun loadMoreOnResume() {
        showLoader()
        viewModel.loadData()
    }

    private fun showLoader() {
        binding.loader.avi.smoothToShow()
        binding.loader.avi.bringToFront()
    }

    private fun refreshLoadMore() {
        viewModel.loadData()
    }


    private fun updateNoItemLabel() {
        if (viewModel.isBusy.value == false && viewModel.careersList.value.isNullOrEmpty()) {
            binding.noItemToShow.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
        } else {
            binding.noItemToShow.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        }
    }

}
