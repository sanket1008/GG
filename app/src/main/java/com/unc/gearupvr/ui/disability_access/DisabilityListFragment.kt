package com.unc.gearupvr.ui.disability_access


import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.lifecycle.ViewModelProviders
import com.unc.gearupvr.R
import com.unc.gearupvr.model.College
import com.unc.gearupvr.ui.visit_college_nc.CollegesListViewFragment
import com.unc.gearupvr.ui.visit_college_nc.VisitCollegeNCViewModel
import com.unc.gearupvr.utils.ExternalLinks
import com.unc.gearupvr.utils.KeyboardHandler
import org.jetbrains.anko.sdk27.coroutines.onClick


class DisabilityListFragment : CollegesListViewFragment() {

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.viewModel =
            ViewModelProviders.of(this).get(VisitCollegeNCViewModel::class.java)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = super.onCreateView(inflater, container, savedInstanceState)
        val headerView =
            inflater.inflate(R.layout.disability_access_top_bar, null)
        val layoutParams = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP)
        headerView?.layoutParams = layoutParams
        binding.parentLayout.addView(headerView)
        val swipeLayout = binding.swipeContainer
            .layoutParams as RelativeLayout.LayoutParams
        headerView?.id?.let { swipeLayout.addRule(RelativeLayout.BELOW, it) }

        val backButton = headerView?.findViewById(R.id.back_button) as ImageView
        backButton.onClick { activity?.onBackPressed() }


        if ((activity?.supportFragmentManager?.backStackEntryCount ?: 0) > 0) {
            backButton.visibility = View.VISIBLE
            backButton.onClick {
                activity?.onBackPressed()
            }
        } else {
            backButton.visibility = View.GONE
        }
        val searchCollege = headerView.findViewById(R.id.search_college) as EditText
        searchCollege.setOnEditorActionListener { _, action, _ ->
            var handled = false
            if (action == EditorInfo.IME_ACTION_DONE) {
                search(searchCollege.text.toString())
                activity?.let { KeyboardHandler.hideKeyboard(it) }
                handled = true
            }
            handled
        }

        searchCollege.setOnTouchListener { _, event ->
            val drawableRight = 2
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= (searchCollege.right - searchCollege.compoundDrawables[drawableRight].bounds.width())) {
                    searchCollege.text.clear()
                    searchCollege.clearFocus()
                    activity?.let { KeyboardHandler.hideKeyboard(it) }
                    search("")


                }
            }
            false
        }

        return view
    }

    override fun loadMoreOnResume() {
        viewModel?.loadData(disabilityAccess = true)
        showLoader()
    }

    override fun refreshLoadMore() {
        viewModel?.loadData(disabilityAccess = true)
        showLoader()

    }

    override fun loadOnScroll() {
        if (viewModel?.loadData(loadMore = true, disabilityAccess = true) == true)
            binding.aviPagination.avi.smoothToShow()
    }

    override fun search(query: String) {
        viewModel?.query = query
        showLoader()
        viewModel?.loadData(disabilityAccess = true)
    }

    override fun onItemClick(
        newCollege: College,
        oldCollege: College?,
        oldItemView: View?,
        newItemView: View
    ) {
        activity?.let { ExternalLinks.openUrl(it, newCollege.disabilityUrl) }
    }
}