package com.unc.gearupvr.ui.user_details.user_type

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.unc.gearupvr.R
import com.unc.gearupvr.model.GearupApp
import com.unc.gearupvr.ui.user_details.UserDetailsViewModel
import com.wang.avi.AVLoadingIndicatorView

class UserTypeFragment : Fragment() {

    companion object {
        fun newInstance(parent: UserDetailsViewModel) = UserTypeFragment().apply {
            sharedViewModel = parent
        }
    }


    private val viewModel: UserTypeViewModel by lazy {
        ViewModelProviders.of(this).get(UserTypeViewModel::class.java)
    }

    private lateinit var sharedViewModel: UserDetailsViewModel /*by lazy {
        ViewModelProviders.of(this).get(UserDetailsViewModel::class.java)
    }*/

    private lateinit var linearLayoutManager: LinearLayoutManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.user_type_fragment, container, false)
        val userTypeRecyclerView = root.findViewById<RecyclerView>(R.id.userTypeList)
        val indicator = root.findViewById<AVLoadingIndicatorView>(R.id.avi)
        linearLayoutManager = LinearLayoutManager(GearupApp.ctx)
        userTypeRecyclerView.hasFixedSize()
        userTypeRecyclerView.layoutManager = linearLayoutManager

        //Show/Hide activity indicator based on viewModel busy status
        viewModel.isBusy.observe(this, Observer {
            if (it || sharedViewModel.isBusy.value == true) {
                indicator.smoothToShow()
            } else {
                indicator.smoothToHide()
            }
        })

        sharedViewModel.isBusy.observe(this, Observer {
            if (it || viewModel.isBusy.value == true) {
                indicator.smoothToShow()
            } else {
                indicator.smoothToHide()
            }
        })

        viewModel.loadData()

        viewModel.userTypes.observe(this, Observer {
            val adapter = UserTypeAdapter(it, sharedViewModel)
            userTypeRecyclerView.adapter = adapter

        })
        return root
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadData()
    }
}