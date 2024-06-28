package com.unc.gearupvr.ui.visit_college_nc

import android.content.Context
import android.content.res.ColorStateList
import android.os.Build
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.unc.gearupvr.BuildConfig
import com.unc.gearupvr.R
import com.unc.gearupvr.databinding.CollegeListItemBinding
import com.unc.gearupvr.model.College
import com.unc.gearupvr.ui.menu.MenuActivity
import com.unc.gearupvr.ui.university_details.UniversityDetailsFragment
import org.jetbrains.anko.sdk27.coroutines.onClick


class CollegeListViewAdapter(
    private val colleges: MutableList<College>,
    private val selectionHandler: ((oldCollege: College?, oldView: View?, newCollege: College, newView: View) -> Unit)?
) :
    RecyclerView.Adapter<CollegeListViewAdapter.CollegeListViewHolder>() {
    var isNavIconVisible: Boolean = false
    private var selectedView: View? = null

    private var selectedCollege: College? = null
    private val itemClickListener = { college: College, view: View ->
        selectionHandler?.let { it(selectedCollege, selectedView, college, view) }
        selectedCollege = college
        selectedView = view
    }


    inner class CollegeListViewHolder(private val binding: CollegeListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: College?, itemClickListener: (College, View) -> Unit?) {


            binding.college = item
            binding.nextButton.onClick {
                (itemView.context as MenuActivity).changeFragment(
                    UniversityDetailsFragment.newInstance(item ?: return@onClick),
                    isFirst = false,
                    shouldAnimate = true
                )
            }
            itemClickListener.let { row ->
                itemView.onClick {
                    if (item != null) {
                        row(item, binding.parentView)
                    }
                }
            }
            if (isNavIconVisible)
                binding.nextButton.visibility = View.VISIBLE
            else
                binding.nextButton.visibility = View.GONE


            Glide.with(itemView.context)
                .load("https://" + BuildConfig.API_BASE + "/" + item?.logo)
                .fitCenter()
                .placeholder(R.drawable.default_tile_icon)
                .into(binding.logoImageView)


            // adding tag programmatically
            binding.chipGroup.removeAllViews()
            if (item != null) {
                if (item.tagsList.isNotEmpty()) {
                    if (item.tagsList.size > 4)
                        for (index in 0..minOf(item.tagsList.size - 1, 3)) {
                            val chip =
                                getStyledChip(binding.chipGroup.context, item.tagsList[index])
                            binding.chipGroup.addView(chip)
                        }
                    else {
                        for (index in 0..minOf(item.tagsList.size - 1, item.tagsList.size)) {
                            val chip =
                                getStyledChip(binding.chipGroup.context, item.tagsList[index])
                            binding.chipGroup.addView(chip)
                        }
                    }
                }
            }

            if (item != null) {
                if (item.tagsList.size > 4) {
                    val chip = getStyledChip(binding.chipGroup.context, "...")
                    binding.chipGroup.addView(chip)
                }
            }


            binding.executePendingBindings()

        }

        private fun getStyledChip(context: Context, text: String): Chip {
            val chip = Chip(context)
            chip.text = text
            chip.isCheckable = false
            chip.isClickable = false
            chip.isEnabled = false
            chip.chipBackgroundColor =
                ColorStateList.valueOf(
                    ContextCompat.getColor(
                        context,
                        R.color.chip_state
                    )
                )
            chip.chipStrokeColor = ColorStateList.valueOf(
                ContextCompat.getColor(
                    context,
                    R.color.chip_border_color
                )
            )
            chip.chipStrokeWidth = context.resources.getDimension(R.dimen.chip_border_width)
            chip.chipBackgroundColor = ColorStateList.valueOf(
                ContextCompat.getColor(
                    context,
                    R.color.chip_item_bg
                )
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                chip.setTextAppearance(R.style.ChipTextAppearance)
            }
            val cornersRadius = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                15F,
                chip.context.resources.displayMetrics
            )
            chip.chipCornerRadius = cornersRadius
            return chip
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CollegeListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = CollegeListItemBinding.inflate(inflater)
        return CollegeListViewHolder(binding)
    }


    override fun onBindViewHolder(holder: CollegeListViewHolder, position: Int) {
        holder.bind(colleges[position], itemClickListener)
    }

    override fun getItemCount(): Int = colleges.size
}