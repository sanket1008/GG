package com.unc.gearupvr.ui.careers

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.unc.gearupvr.BuildConfig
import com.unc.gearupvr.R
import com.unc.gearupvr.databinding.CareersListItemBinding
import com.unc.gearupvr.model.Careers

class CareersListViewAdapter(
    private val careers: MutableList<Careers>,
    private val viewModel: CareersListViewModel
) :
    RecyclerView.Adapter<CareersListViewAdapter.CareersListViewHolder>() {


    inner class CareersListViewHolder(private val binding: CareersListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Careers?, viewModel: CareersListViewModel) {
            binding.career = item
            binding.viewModel = viewModel
            Glide.with(itemView.context)
                .load("https://" + BuildConfig.API_BASE + "/" + item?.logo)
                .centerCrop()
                .placeholder(R.drawable.default_tile_icon)
                .into(binding.logoImageView)
            binding.executePendingBindings()

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CareersListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CareersListItemBinding.inflate(inflater)
        return CareersListViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: CareersListViewHolder,
        position: Int
    ) {
        holder.bind(careers[position], viewModel)
    }

    override fun getItemCount(): Int = careers.size

}