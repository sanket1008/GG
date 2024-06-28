package com.unc.gearupvr.ui.visit_college_nc.filter

import android.os.Bundle
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.gridlayout.widget.GridLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.jaygoo.widget.SeekBar
import com.unc.gearupvr.R
import com.unc.gearupvr.databinding.FilterFragmentBinding
import com.unc.gearupvr.model.RangeLimit
import com.unc.gearupvr.ui.visit_college_nc.VisitCollegeNCViewModel
import com.unc.gearupvr.ui.visit_college_nc.filter.majorList.MajorListFragment
import com.unc.gearupvr.utils.FilterConstants
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.textColor


class FilterActivity : AppCompatActivity() {

    companion object {
        private const val DETAIL_FRAGMENT: String = "DETAIL_FRAGMENT"
    }

    lateinit var binding: FilterFragmentBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.filter_fragment)
        val viewModel = ViewModelProviders.of(this).get(FilterFragmentViewModel::class.java)


        binding.radioGrpStateType.clearCheck()
        binding.actRangeBar.setIndicatorTextDecimalFormat("0")
        binding.satRangeBar.setIndicatorTextDecimalFormat("0")
        binding.costRangeBar?.setIndicatorTextDecimalFormat("0")
        binding.costRangeBar?.setIndicatorTextStringFormat("\$%s")
        binding.actRangeBar.setRange(FilterConstants.actMin, FilterConstants.actMax)
        binding.satRangeBar.setRange(FilterConstants.satMin, FilterConstants.satMax)
        binding.costRangeBar.setRange(FilterConstants.costMIN, FilterConstants.costMAX)



        viewModel.stateType.observe(this, Observer {
            println(it)
            when (it) {
                FilterConstants.inState -> {
                    binding.radioButtonInState.isChecked = true
                }
                FilterConstants.outState -> {
                    binding.radioButtonOutState.isChecked = true
                }
                else -> {
                    binding.radioButtonInState.isChecked = true
                }
            }
        })
        viewModel.satScore.observe(this, Observer {
            binding.satRangeBar.setProgress(it.min, it.max)
        })
        viewModel.actScore.observe(this, Observer {
            binding.actRangeBar.setProgress(it.min, it.max)
        })
        viewModel.costRange.observe(this, Observer {
            binding.costRangeBar.setProgress(it.min, it.max)
        })

        binding.imgSelectMajors.onClick {
            println(viewModel)
            false.changeFragment(
                MajorListFragment.newInstance(viewModel),
                shouldAnimate = true
            )
        }

        viewModel.filterMajorsList.observe(this, Observer {
            if (it.isNullOrEmpty()) {
                binding.txtSelectedTag.text = getString(R.string.tag_selected_placeholder)
            } else {
                binding.txtSelectedTag.text =
                    getString(R.string.tag_selected, it.filter { major -> major.isChecked }.size)
            }
        })

        var radioButtonId = 0
        enumValues<FilterConstants.Companion.StudentBody>().forEach {
            val rb = RadioButton(this)
            rb.text = it.displayName
            val param: GridLayout.LayoutParams = GridLayout.LayoutParams(
                GridLayout.spec(
                    GridLayout.UNDEFINED, GridLayout.FILL, 1f
                ),
                GridLayout.spec(GridLayout.UNDEFINED, GridLayout.FILL, 1f)
            )
            param.height = ViewGroup.LayoutParams.WRAP_CONTENT
            param.width = 0
            param.setMargins(10, 10, 10, 20)
            rb.layoutParams = param
            rb.buttonDrawable =
                this.let { context ->
                    ContextCompat.getDrawable(
                        context,
                        R.drawable.radio_button_selector
                    )
                }
            rb.textColor =
                this.let { context ->
                    ContextCompat.getColor(
                        context,
                        R.color.radio_button_txt
                    )
                }
            rb.setPadding(10, 10, 20, 10)
            rb.id = radioButtonId
            if (it.value == VisitCollegeNCViewModel.studentCapacity) {
                viewModel.studentCapacity.value = VisitCollegeNCViewModel.studentCapacity
                rb.isChecked = true
            }
            radioButtonId++
            binding.radioGrpStudentSize.addView(rb)
        }

        binding.radioGrpStateType.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radio_button_inState -> viewModel.stateType.value = FilterConstants.inState
                R.id.radio_button_outState -> viewModel.stateType.value = FilterConstants.outState
                else -> {
                    viewModel.stateType.value = FilterConstants.inState
                }
            }
        }
        binding.radioGrpStudentSize.setOnCheckedChangeListener { _, checkedId ->
            when {
                checkedId >= 0 -> viewModel.studentCapacity.value =
                    FilterConstants.Companion.StudentBody.values()[checkedId].value
                else -> {
                    viewModel.studentCapacity.value = null
                }
            }
        }

        binding.closeFilter.onClick {
            onBackPressed()
        }
        binding.buttonApply.onClick {
            viewModel.satScore.value = RangeLimit(
                binding.satRangeBar.leftSeekBar.roundOfProgress(),
                binding.satRangeBar.rightSeekBar.roundOfProgress()
            )
            viewModel.actScore.value = RangeLimit(
                binding.actRangeBar.leftSeekBar.roundOfProgress(),
                binding.actRangeBar.rightSeekBar.roundOfProgress()
            )
            viewModel.costRange.value = RangeLimit(
                binding.costRangeBar.leftSeekBar.roundOfProgress(),
                binding.costRangeBar.rightSeekBar.roundOfProgress()
            )
            VisitCollegeNCViewModel.satScore = (viewModel.satScore.value ?: return@onClick)
            VisitCollegeNCViewModel.actScore = (viewModel.actScore.value ?: return@onClick)
            VisitCollegeNCViewModel.costRange = (viewModel.costRange.value ?: return@onClick)
            VisitCollegeNCViewModel.stateType = (viewModel.stateType.value ?: return@onClick)
            VisitCollegeNCViewModel.studentCapacity =
                (viewModel.studentCapacity.value)
            VisitCollegeNCViewModel.filterMajorsList = (viewModel.getMajorsList() ?: return@onClick)
            onBackPressed()
        }
        binding.clearStudentBodySize.onClick {
            binding.radioGrpStudentSize.clearCheck()
        }
        binding.clearAll.onClick {
            viewModel.satScore.value = RangeLimit(FilterConstants.satMin, FilterConstants.satMax)
            viewModel.actScore.value = RangeLimit(FilterConstants.actMin, FilterConstants.actMax)
            viewModel.costRange.value = RangeLimit(FilterConstants.costMIN, FilterConstants.costMAX)
            viewModel.stateType.value = FilterConstants.inState
            viewModel.removeSelectedMajorsList()

            binding.radioGrpStudentSize.clearCheck()
            VisitCollegeNCViewModel.satScore = (viewModel.satScore.value ?: return@onClick)
            VisitCollegeNCViewModel.actScore = (viewModel.actScore.value ?: return@onClick)
            VisitCollegeNCViewModel.costRange = (viewModel.costRange.value ?: return@onClick)
            VisitCollegeNCViewModel.stateType = (viewModel.stateType.value ?: return@onClick)
            VisitCollegeNCViewModel.studentCapacity = (viewModel.studentCapacity.value)
            VisitCollegeNCViewModel.filterMajorsList = (
                    viewModel.getMajorsList() ?: return@onClick
                    )

            onBackPressed()
        }


    }

    private fun SeekBar.roundOfProgress(): Float {
        return indicatorTextDecimalFormat.format(progress).toFloat()
    }

    private fun Boolean.changeFragment(
        newFragment: Fragment,
        shouldAnimate: Boolean
    ) {

        val oldFragment = supportFragmentManager.findFragmentByTag(DETAIL_FRAGMENT)
        if (oldFragment != newFragment) {
            val fragmentTransaction = supportFragmentManager.beginTransaction()

            if (shouldAnimate) {
                fragmentTransaction.setCustomAnimations(
                    R.anim.enter_from_right,
                    R.anim.exit_to_left,
                    R.anim.enter_from_left,
                    R.anim.exit_to_right
                )
            }
            if (!this) {
                fragmentTransaction.add(
                    R.id.content, newFragment,
                    DETAIL_FRAGMENT
                )
                fragmentTransaction.addToBackStack(null)
            } else {
                fragmentTransaction.replace(
                    R.id.content, newFragment,
                    DETAIL_FRAGMENT
                )
            }
            fragmentTransaction.commit()
        }

    }
}
