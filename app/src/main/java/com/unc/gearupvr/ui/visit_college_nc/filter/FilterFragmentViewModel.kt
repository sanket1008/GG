package com.unc.gearupvr.ui.visit_college_nc.filter

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.unc.gearupvr.model.Majors
import com.unc.gearupvr.model.RangeLimit
import com.unc.gearupvr.model.StudentCapacity
import com.unc.gearupvr.ui.visit_college_nc.VisitCollegeNCViewModel

class FilterFragmentViewModel : ViewModel() {


    val stateType = MutableLiveData<String>(VisitCollegeNCViewModel.stateType)
    val satScore = MutableLiveData<RangeLimit>(VisitCollegeNCViewModel.satScore)
    val actScore = MutableLiveData<RangeLimit>(VisitCollegeNCViewModel.actScore)
    val costRange = MutableLiveData<RangeLimit>(VisitCollegeNCViewModel.costRange)
    val studentCapacity =
        MutableLiveData<StudentCapacity>(VisitCollegeNCViewModel.studentCapacity)
    val filterMajorsList = MutableLiveData<List<Majors>>(VisitCollegeNCViewModel.filterMajorsList)
    fun getNumberOfSelectedMajors(): Int {
        var counter = 0
        if (!filterMajorsList.value.isNullOrEmpty())
            filterMajorsList.value?.iterator()?.forEach { row ->
                if (row.isChecked) {
                    counter++
                }
            }
        return counter
    }

    fun removeSelectedMajorsList() {
        filterMajorsList.value = emptyList()
    }

    fun getMajorsList(): List<Majors>? {
        return if (filterMajorsList.value.isNullOrEmpty())
            emptyList()
        else
            filterMajorsList.value?.toList()
    }

}