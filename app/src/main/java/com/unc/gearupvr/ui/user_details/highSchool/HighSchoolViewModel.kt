package com.unc.gearupvr.ui.user_details.highSchool

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.unc.gearupvr.model.HighSchool
import com.unc.gearupvr.service.ServiceUserDetails

class HighSchoolViewModel : ViewModel() {

    val isBusy: MutableLiveData<Boolean> = MutableLiveData(false)
    val shouldShowClearButton: MutableLiveData<Boolean> = MutableLiveData(false)
    val highSchools: LiveData<List<HighSchool>> get() = mutableLiveDataHighSchools
    private val mutableLiveDataHighSchools = MutableLiveData<List<HighSchool>>()

    var selectedSchool: MutableLiveData<HighSchool> = MutableLiveData()

    fun setSelectedHighSchool(position: Int) {
        (highSchools.value)?.let {
            if (it.size > position) selectedSchool.value = it[position]
        }
    }


    private var _searchKey = String()
    var searchKey: MutableLiveData<String> = SearchKeyMutableLiveData {
        println(it)
        if (it?.isBlank() != false)
            mutableLiveDataHighSchools.postValue(emptyList())

        if (it != null && _searchKey != it) {
            _searchKey = it
            shouldShowClearButton.value = !_searchKey.isBlank()
            if (_searchKey.length > 2) {
                isBusy.value = true
                ServiceUserDetails.fetchHighSchoolData(
                    _searchKey
                ) { key, data, error ->
                    // ignore all response other than the latest
                    if (_searchKey == key) {
                        when {
                            data != null -> mutableLiveDataHighSchools.postValue(data)
                            error != null -> println(error.localizedMessage)
                        }
                        isBusy.value = false
                    }
                }
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

