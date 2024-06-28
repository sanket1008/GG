package com.unc.gearupvr.ui.visit_college_nc

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLngBounds
import com.unc.gearupvr.R
import com.unc.gearupvr.databinding.CollegesListViewFragmentBinding
import com.unc.gearupvr.model.College
import com.unc.gearupvr.ui.menu.MenuActivity
import com.unc.gearupvr.ui.university_details.UniversityDetailsFragment


open class CollegesListViewFragment : Fragment() {

    companion object {
        fun newInstance(viewModelShared: VisitCollegeNCViewModel) =
            CollegesListViewFragment().apply {
                viewModel = viewModelShared
            }
    }

    var viewModel: VisitCollegeNCViewModel? = null
    lateinit var binding: CollegesListViewFragmentBinding


    open var locationValues: LatLngBounds? = null
    open var selectionHandler: ((oldCollege: College?, oldView: View?, newCollege: College, newView: View) -> Unit)? =
        { oldCollege, oldItemView, newCollege, newItemView ->

            onItemClick(newCollege, oldCollege, oldItemView, newItemView)
        }


    lateinit var adapter: CollegeListViewAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = CollegesListViewFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.loader.avi.smoothToHide()
        binding.aviPagination.avi.smoothToHide()

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
        adapter = CollegeListViewAdapter(
            (viewModel?.collegeList?.value ?: return null),
            selectionHandler
        )
        binding.recyclerView.adapter = adapter
        viewModel?.collegeList?.observe(this, Observer {
            updateNoItemLabel()
            adapter.notifyDataSetChanged()
        })

        viewModel?.isBusy?.observe(this, Observer {
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

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val pastVisibleItems = layoutManager.findFirstVisibleItemPosition()

                if (dy > 0) //check for scroll down
                {
                    when (viewModel?.isBusy?.value) {
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

    open fun loadOnScroll() {
        if (viewModel?.loadData(loadMore = true) == true)
            binding.aviPagination.avi.smoothToShow()
    }

    private fun updateNoItemLabel() {
        if (viewModel?.isBusy?.value == false && viewModel?.collegeList?.value.isNullOrEmpty()) {
            binding.noItemToShow.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.GONE
        } else {
            binding.noItemToShow.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        }
    }

    open fun search(query: String) {
        viewModel?.query = query
        showLoader()
        viewModel?.loadData()
    }

    fun showLoader() {
        binding.loader.avi.smoothToShow()
        binding.loader.avi.bringToFront()
    }

    override fun onResume() {
        super.onResume()
        loadMoreOnResume()
    }

    open fun loadMoreOnResume() {
        showLoader()
        viewModel?.loadData()
    }

    open fun refreshLoadMore() {
        viewModel?.loadData()
    }

    open fun onItemClick(
        newCollege: College,
        oldCollege: College?,
        oldItemView: View?,
        newItemView: View
    ) {
        (activity as MenuActivity).changeFragment(
            UniversityDetailsFragment.newInstance(newCollege),
            isFirst = false,
            shouldAnimate = true
        )
    }

}
