package com.unc.gearupvr.ui.visit_college_nc.filter.majorList

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.unc.gearupvr.model.Majors
import com.unc.gearupvr.service.API
import com.unc.gearupvr.service.ApiRequest


class MajorListViewModel : ViewModel() {
    val isBusy: MutableLiveData<Boolean> = MutableLiveData(false)
    val majorsList: LiveData<List<Majors>> get() = _majorsList
    private val _majorsList = MutableLiveData<List<Majors>>()
    private val _primaryMajorsList = MutableLiveData<List<Majors>>()
    val isError: MutableLiveData<Boolean> = MutableLiveData(false)
    val searchKey: MutableLiveData<String> = SearchKeyMutableLiveData {
        it?.let { value -> onSearchKeyChanged(value) }
    }
    val selectedContentsCount: MutableLiveData<String> = MutableLiveData("")

    // API Call
    fun loadData() {
        isBusy.value = true
        val request = ApiRequest(API.Majors)
        request.invoke { result, _, _ ->
            if (result != null) {
                println(result)
                val responseList =
                    Gson().fromJson(result.toString(), Array<Majors>::class.java).toList()
                updateInitialData(responseList)
            } else {
                isBusy.value = false
                isError.value = true
            }
        }
    }

    private fun onSearchKeyChanged(searchKey: String) {
        searchList(searchKey)
    }

    private fun searchList(searchKey: String) {
        if (searchKey.isNotEmpty()) {
            val filterList = _primaryMajorsList.value?.filter {
                it.title?.contains(searchKey, true) ?: false
            }
            _majorsList.postValue(filterList)
        } else
            _majorsList.postValue(_primaryMajorsList.value)
    }

    fun changeCheckedStatus(item: Majors, status: Boolean) {
        _primaryMajorsList.value?.find { it.uid.equals(item.uid) }?.isChecked = status
        getSelectedCount()
    }

    fun getSelectedList(): List<Majors>? {
        return _primaryMajorsList.value

    }

    fun clearList() {
        _primaryMajorsList.value?.filter { it.isChecked }?.apply {
            iterator().forEach { major -> major.isChecked = false }
        }
        _majorsList.value = _primaryMajorsList.value
        getSelectedCount()

    }

    private fun updateInitialData(majorsList: List<Majors>) {
        _majorsList.postValue(majorsList)
        _primaryMajorsList.value = majorsList
        getSelectedCount()

    }

    fun getSelectedCount() {
        when {
            !_primaryMajorsList.value.isNullOrEmpty() -> {
                selectedContentsCount.postValue(
                    _primaryMajorsList.value?.filter { it.isChecked }?.size.toString() + " Selected Item"
                )
            }
            else -> {
                selectedContentsCount.postValue("0 Selected Item")

            }

        }
    }

    inner class SearchKeyMutableLiveData(val serviceHandler: ((searchKey: String?) -> Unit)) :
        MutableLiveData<String>() {
        override fun setValue(value: String?) {
            super.setValue(value)
            serviceHandler(value)
        }
    }
}
