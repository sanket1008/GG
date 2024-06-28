package com.unc.gearupvr.ui.careers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.unc.gearupvr.model.Careers
import com.unc.gearupvr.service.CareersService

class CareersListViewModel : ViewModel() {

    private val _isBusy: MutableLiveData<Boolean> = MutableLiveData(false)
    val isBusy: LiveData<Boolean> = _isBusy
    private val _careersList: MutableLiveData<MutableList<Careers>> =
        MutableLiveData(mutableListOf())
    val careersList: LiveData<MutableList<Careers>> get() = _careersList
    private val paginationLimit = 20
    private var offset = 0
    private var totalCount = -1
    private val didReachEndOfList: Boolean get() = (totalCount != -1 && offset > totalCount)

    var selectedCareers = MutableLiveData<Careers>()
    fun selected(item: Careers) {
        if (selectedCareers.value != item)
            selectedCareers.postValue(item)
    }


    fun loadData(loadMore: Boolean? = false): Boolean {
        var shouldClear = false
        if (_isBusy.value == true) return true
        if (loadMore == true) {
            offset += paginationLimit

        } else {
            shouldClear = true
            offset = 0
            totalCount = -1
        }
        if (didReachEndOfList) return false
        _isBusy.postValue(true)
        CareersService.fetchCareersList(limit = paginationLimit, offset = offset)
        { careersResponse, error ->
            when {
                careersResponse != null -> {
                    if (shouldClear) {
                        _careersList.value?.clear()
                    }
                    _careersList += careersResponse.results
                    totalCount = careersResponse.count
                }
                error != null -> println(error.localizedMessage)
            }
            _isBusy.postValue(false)
        }
        return true
    }
}


operator fun <T> MutableLiveData<MutableList<T>>.plusAssign(values: List<T>) {
    val value = this.value ?: mutableListOf()
    value.addAll(values)
    this.value = value
}
