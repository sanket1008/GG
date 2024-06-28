package com.unc.gearupvr.ui.visit_college_nc

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLngBounds
import com.unc.gearupvr.model.College
import com.unc.gearupvr.model.Majors
import com.unc.gearupvr.model.RangeLimit
import com.unc.gearupvr.model.StudentCapacity
import com.unc.gearupvr.service.CollegeService
import com.unc.gearupvr.utils.FilterConstants
import kotlin.properties.ObservableProperty
import kotlin.reflect.KProperty


class VisitCollegeNCViewModel : ViewModel() {

    companion object {

        private var shouldRefresh: Boolean = false
        var filterMajorsList: List<Majors> by observing(
            emptyList(),
            didSet = { shouldRefresh = true })

        var stateType: String? by observing(
            FilterConstants.inState,
            didSet = { shouldRefresh = true })

        var satScore: RangeLimit? by observing(
            RangeLimit(FilterConstants.satMin, FilterConstants.satMax),
            didSet = { shouldRefresh = true })

        var actScore: RangeLimit? by observing(
            RangeLimit(FilterConstants.actMin, FilterConstants.actMax),
            didSet = { shouldRefresh = true })

        var costRange: RangeLimit? by observing(
            RangeLimit(FilterConstants.costMIN, FilterConstants.costMAX),
            didSet = { shouldRefresh = true })


        var studentCapacity: StudentCapacity? by observing(
            null,
            didSet = { shouldRefresh = true })

        private fun <T> observing(
            initialValue: T,
            willSet: () -> Unit = { },
            didSet: () -> Unit = { }
        ) = object : ObservableProperty<T>(initialValue) {
            override fun beforeChange(property: KProperty<*>, oldValue: T, newValue: T): Boolean =
                true.apply { willSet() }

            override fun afterChange(property: KProperty<*>, oldValue: T, newValue: T) = didSet()
        }
    }

    var query: String by observing(
        "",
        didSet = { shouldRefresh = true })

    private val _isBusy: MutableLiveData<Boolean> = MutableLiveData(false)
    val isBusy: LiveData<Boolean> = _isBusy

    private val _collegeList: MutableLiveData<MutableList<College>> =
        MutableLiveData(mutableListOf())
    val collegeList: LiveData<MutableList<College>> get() = _collegeList

    private val paginationLimit = 20
    private var offset = 0
    private var totalCount = -1
    private val didReachEndOfList: Boolean get() = (totalCount != -1 && offset > totalCount)

    val isFilter
        get() =
            !(satScore == (RangeLimit(
                FilterConstants.satMin,
                FilterConstants.satMax
            ))
                    && actScore == (RangeLimit(
                FilterConstants.actMin,
                FilterConstants.actMax
            ))
                    && costRange == (RangeLimit(
                FilterConstants.costMIN,
                FilterConstants.costMAX
            ))
                    && getOnlySelectedMajors().isEmpty()
                    && stateType == FilterConstants.inState
                    && studentCapacity?.operator == null)

    fun loadData(
        latLng: LatLngBounds? = null,
        loadMore: Boolean? = false,
        disabilityAccess: Boolean? = null
    ): Boolean {
        var shouldClear = false
        if (_isBusy.value == true) return true
        if (shouldRefresh || loadMore == false) {
            totalCount = -1
            offset = 0
            shouldRefresh = false
            shouldClear = true
        } else {
            offset += paginationLimit
        }

        if (didReachEndOfList) return false

        _isBusy.postValue(true)
        CollegeService.fetchCollageList(
            latLngBounds = latLng,
            majors = getOnlySelectedMajors(),
            query = query,
            stateType = stateType,
            satRange = satScore,
            actRange = actScore,
            costRange = costRange,
            studentCapacity = studentCapacity,
            limit = paginationLimit,
            offset = offset,
            disabilityAccess = disabilityAccess
        ) { collegeResponse, error ->
            when {
                collegeResponse != null -> {
                    if (shouldClear) {
                        _collegeList.value?.clear()
                    }
                    _collegeList += collegeResponse.results
                    totalCount = collegeResponse.count
                }
                error != null -> println(error.localizedMessage)
            }
            _isBusy.postValue(false)
        }
        return true
    }


    private fun getOnlySelectedMajors(): List<Majors> {
        return when {
            !filterMajorsList.isNullOrEmpty() -> {
                filterMajorsList.filter { it.isChecked }
            }
            else -> emptyList()
        }
    }

}

operator fun <T> MutableLiveData<MutableList<T>>.plusAssign(values: List<T>) {
    val value = this.value ?: mutableListOf()
    value.addAll(values)
    this.value = value
}
