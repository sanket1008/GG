package com.unc.gearupvr.ui.user_details.highSchool

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.unc.gearupvr.R
import com.unc.gearupvr.databinding.HighSchoolFragmentBinding
import com.unc.gearupvr.ui.user_details.UserDetailsViewModel
import org.jetbrains.anko.sdk27.coroutines.onClick

class HighSchoolFragment : Fragment() {

    companion object {
        fun newInstance(parent: UserDetailsViewModel) = HighSchoolFragment().apply {
            sharedViewModel = parent
        }
    }

    private lateinit var sharedViewModel: UserDetailsViewModel

    private lateinit var viewModel: HighSchoolViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProviders.of(this).get(HighSchoolViewModel::class.java)
        val binding = HighSchoolFragmentBinding.inflate(inflater, container, false)

        binding.presenterViewModel = sharedViewModel
        binding.viewModel = viewModel
        viewModel.searchKey.observe(this, Observer {
            binding.selectedSchoolName = it
        })
        viewModel.selectedSchool.observe(this, Observer {
            binding.selectedSchoolUID = it.uid
        })

        val autoCompleteTextView = binding.autoCompleteTextView
        (context)?.let { ctx ->
            val adapter = ArrayAdapter<String>(ctx, R.layout.high_school_popup_item, emptyList())
            autoCompleteTextView.setAdapter(adapter)
            adapter.notifyDataSetChanged()
        }


        //Show/Hide activity indicator based on viewModel busy status
        val indicator = binding.include2.avi
        sharedViewModel.isBusy.observe(this, Observer {
            if (it) {
                indicator.show()
                indicator.bringToFront()
            } else {
                indicator.hide()
            }
        })

        viewModel.isBusy.observe(this, Observer {
            if (it) {
                indicator.show()
                indicator.bringToFront()
            } else {
                indicator.hide()
            }
        })

        val clear = binding.clear
        viewModel.shouldShowClearButton.observe(this, Observer {
            when {
                it -> clear.visibility = View.VISIBLE
                else -> clear.visibility = View.INVISIBLE
            }
        })
        clear.onClick {
            autoCompleteTextView.setText("")
        }

        viewModel.highSchools.observe(this, Observer {
            val highSchoolsNameList = it.map { schools -> schools.name }
            (context)?.let { ctx ->
                val adapter =
                    ArrayAdapter<String>(ctx, R.layout.high_school_popup_item, highSchoolsNameList)
                autoCompleteTextView.setAdapter(adapter)
                adapter.notifyDataSetChanged()
            }
        })

        autoCompleteTextView.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                viewModel.setSelectedHighSchool(position)
            }

        return binding.root
    }
}