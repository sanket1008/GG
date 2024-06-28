package com.unc.gearupvr.ui.user_details.user_type

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.unc.gearupvr.model.UserType
import com.unc.gearupvr.service.API
import com.unc.gearupvr.service.ApiRequest
import kotlin.reflect.KProperty

class UserTypeViewModel : ViewModel() {

    val isBusy: MutableLiveData<Boolean> = MutableLiveData(false)
    val userTypes: LiveData<List<UserType>> get() = mutableLiveDataUserTypes
    private val mutableLiveDataUserTypes = MutableLiveData<List<UserType>>()


    fun loadData() {
        isBusy.value = true
        val request = ApiRequest(API.UserType)
        request.invoke { result, _, _ ->
            if (result != null) {
                println(result)
                val gson = Gson()
                val userTypes =
                    gson.fromJson(result.toString(), Array<UserType>::class.java).toList()
                mutableLiveDataUserTypes.postValue(userTypes)
            }
            isBusy.value = false
        }
    }

    private operator fun Any.setValue(
        viewModel: ViewModel,
        property: KProperty<*>,
        mutableLiveData: Any
    ) = Unit

}