package com.unc.gearupvr.ui.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.unc.gearupvr.R
import com.unc.gearupvr.databinding.MoreNavFragmentBinding

class MoreNavFragment(private val viewModel: MenuViewModel) : Fragment() {

    private lateinit var binding: MoreNavFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = MoreNavFragmentBinding.inflate(inflater, container, false)

        val layoutManager = LinearLayoutManager(activity)
        binding.recyclerView.layoutManager = layoutManager

        //set recycler view divider
        val dividerItemDecoration = DividerItemDecoration(
            binding.recyclerView.context,
            layoutManager.orientation
        )
        binding.recyclerView.context.getDrawable(R.drawable.divider_item_decoration)?.let {
            dividerItemDecoration.setDrawable(it)
        }
        binding.recyclerView.addItemDecoration(dividerItemDecoration)

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel.menusList.observe(this, Observer {
            if (it != null && it.size > MenuViewModel.MAX_MENU_ITEMS_COUNT) {
                binding.recyclerView.adapter = MoreNavAdapter(
                    it.subList(MenuViewModel.MAX_MENU_ITEMS_COUNT, it.size),
                    viewModel
                )
            }
        })
    }

}
