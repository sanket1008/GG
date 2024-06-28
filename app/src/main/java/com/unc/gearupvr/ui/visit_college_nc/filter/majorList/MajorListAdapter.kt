package com.unc.gearupvr.ui.visit_college_nc.filter.majorList

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.unc.gearupvr.databinding.ListMajorItemBinding
import com.unc.gearupvr.model.Majors


class MajorListAdapter(
    private var items: List<Majors>,
    private val callback: ((Majors, Boolean, Int) -> Unit)?
) : RecyclerView.Adapter<MajorListAdapter.MajorListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MajorListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListMajorItemBinding.inflate(inflater)
        return MajorListViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size


    override fun onBindViewHolder(holder: MajorListViewHolder, position: Int) =
        holder.bind(items[position], callback, position)

    class MajorListViewHolder(private val binding: ListMajorItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: Majors,
            callback: ((Majors, Boolean, Int) -> Unit)?,
            position: Int
        ) {
            binding.item = item
            binding.majorsCheckbox.setOnCheckedChangeListener { _, isChecked ->
                callback?.invoke(item, isChecked, position)
            }
            binding.executePendingBindings()

        }

    }

}