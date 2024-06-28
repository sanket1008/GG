package com.unc.gearupvr.ui.menu

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.unc.gearupvr.BuildConfig
import com.unc.gearupvr.R
import com.unc.gearupvr.databinding.MoreNavListItemBinding
import com.unc.gearupvr.model.MenuItem

class MoreNavAdapter(
    private val items: List<MenuItem>,
    private val navigationViewModel: MenuViewModel
) : RecyclerView.Adapter<MoreNavAdapter.MoreNavViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoreNavViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = MoreNavListItemBinding.inflate(inflater)
        return MoreNavViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: MoreNavViewHolder, position: Int) =
        holder.bind(items[position], navigationViewModel)

    inner class MoreNavViewHolder(private val binding: MoreNavListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: MenuItem, presenter: MenuViewModel) {
            binding.item = item
            binding.viewModel = presenter
            Glide.with(itemView.context)
                .load("https://" + BuildConfig.API_BASE + "/" + item.icon)
                .centerCrop()
                .placeholder(R.drawable.default_tile_icon)
                .into(binding.navigationIcon)
            binding.executePendingBindings()
        }
    }
}