package com.unc.gearupvr.ui.user_details.user_type

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.unc.gearupvr.databinding.UserTypeListItemBinding
import com.unc.gearupvr.model.UserType
import com.unc.gearupvr.ui.user_details.UserDetailsViewModel

class UserTypeAdapter(
    private val items: List<UserType>,
    private val presenter: UserDetailsViewModel
) :
    RecyclerView.Adapter<UserTypeAdapter.UserTypeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserTypeViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = UserTypeListItemBinding.inflate(inflater)
        return UserTypeViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: UserTypeViewHolder, position: Int) =
        holder.bind(items[position], presenter)

    inner class UserTypeViewHolder(private val binding: UserTypeListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: UserType, presenter: UserDetailsViewModel) {
            binding.presenter = presenter
            binding.item = item
            binding.executePendingBindings()

        }

    }
}