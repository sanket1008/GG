package com.unc.gearupvr.ui.visit_college_nc.filter.majorList

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.unc.gearupvr.R
import com.unc.gearupvr.databinding.MajorListFragmentBinding
import com.unc.gearupvr.model.Majors
import com.unc.gearupvr.ui.visit_college_nc.filter.FilterFragmentViewModel
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.alert


class MajorListFragment : Fragment() {

    companion object {
        fun newInstance(filterFragmentViewModel: FilterFragmentViewModel) =
            MajorListFragment().apply {
                filterMainViewModel = filterFragmentViewModel
            }
    }

    private lateinit var filterMainViewModel: FilterFragmentViewModel
    private lateinit var viewModel: MajorListViewModel

    private var callBack: ((currentRow: Majors, checkStatus: Boolean, position: Int) -> Unit)? =
        { selectedMajor: Majors, b: Boolean, _: Int ->
            viewModel.changeCheckedStatus(selectedMajor, b)
        }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = MajorListFragmentBinding.inflate(inflater, container, false)
        viewModel = ViewModelProviders.of(this).get(MajorListViewModel::class.java)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        viewModel.isBusy.observe(this, Observer {
            if (it) {
                binding.indicatorView.avi.show()
            } else {
                binding.indicatorView.avi.hide()
            }
        })

        viewModel.loadData()

        viewModel.majorsList.observe(this, Observer { majorsList ->

            filterMainViewModel.filterMajorsList.value?.forEach { filterValue ->
                majorsList.find { it.uid == filterValue.uid }?.isChecked = filterValue.isChecked
            }

            binding.indicatorView.avi.hide()
            binding.parentLayout.visibility = View.VISIBLE
            val layoutManager: RecyclerView.LayoutManager =
                LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
            binding.recyclerView.layoutManager = layoutManager
            viewModel.getSelectedCount()
            val adapter =
                MajorListAdapter(majorsList, callBack)
            binding.recyclerView.adapter = adapter

        })
        viewModel.isError.observe(this, Observer {
            if (it) {
                val alert =
                    alert(getString(R.string.generic_error_message))
                alert.positiveButton(getString(R.string.ok_button)) { activity?.onBackPressed() }
                alert.show()
            }

        })

        binding.searchMajors.setOnTouchListener { _, event ->
            val rightButton = 2
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.x >= binding.searchMajors.right - binding.searchMajors.compoundDrawables[rightButton].bounds.width()
                ) {
                    binding.searchMajors.text.clear()
                }
            }
            false
        }
        binding.doneButton.onClick {
            filterMainViewModel.filterMajorsList.value =
                (viewModel.getSelectedList()?.filter { it.isChecked } ?: return@onClick)

            hideKeyBoard()
            activity?.onBackPressed()
        }
        binding.resetButton.onClick {
            viewModel.clearList()
            filterMainViewModel.filterMajorsList.value = emptyList()

            hideKeyBoard()
            activity?.onBackPressed()
        }
        binding.backButton.onClick {
            viewModel.getSelectedList()
            hideKeyBoard()
            activity?.onBackPressed()

        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        filterMainViewModel.getNumberOfSelectedMajors()
    }

    private fun hideKeyBoard() {
        val imm: InputMethodManager =
            (activity
                ?: return).getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow((view ?: return).windowToken, 0)
    }
}
