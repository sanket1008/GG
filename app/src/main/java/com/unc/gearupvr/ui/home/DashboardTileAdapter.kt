package com.unc.gearupvr.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.unc.gearupvr.BuildConfig
import com.unc.gearupvr.R
import com.unc.gearupvr.databinding.DashboardTileItemBinding
import com.unc.gearupvr.model.DashboardTile

class DashboardTileAdapter(
    private val items: List<DashboardTile>,
    private val presenter: HomeViewModel
) :
    RecyclerView.Adapter<DashboardTileAdapter.DashboardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DashboardViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DashboardTileItemBinding.inflate(inflater)
        return DashboardViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: DashboardViewHolder, position: Int) =
        holder.bind(items[position], presenter)

    inner class DashboardViewHolder(private val binding: DashboardTileItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DashboardTile, presenter: HomeViewModel) {
            binding.item = item
            binding.presenter = presenter
            Glide.with(itemView.context)
                .load("https://" + BuildConfig.API_BASE + "/" + item.image)
                .centerCrop()
                .placeholder(R.drawable.default_tile_icon)
                .into(binding.tileImageView)
            binding.executePendingBindings()

        }

    }
}