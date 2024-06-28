package com.unc.gearupvr.ui.user_details

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.unc.gearupvr.model.GearupApp
import com.unc.gearupvr.model.MenuItem
import com.unc.gearupvr.model.UserType
import com.unc.gearupvr.service.MenuConfigurationService
import com.unc.gearupvr.service.ServiceUserDetails


class UserDetailsViewModel : ViewModel() {

    var menusList: MutableLiveData<List<MenuItem>> = MutableLiveData(emptyList())
    val isBusy: MutableLiveData<Boolean> = MutableLiveData(false)
    var selectedUserType = MutableLiveData<UserType>()
    fun selected(item: UserType) {
        if (isBusy.value != true)
            selectedUserType.value = item
    }

    fun saveButtonPressed(schoolUID: String? = null, schoolName: String? = null) {
        isBusy.value = true

        ServiceUserDetails.postHighSchoolDetails(
            selectedUserType.value ?: return,
            schoolUID,
            schoolName
        ) { _, error ->
            when {
                error != null -> println(error.localizedMessage)
                else -> {
                    (GearupApp.ctx?.getSharedPreferences(
                        GearupApp.SHARED_PREFERENCES,
                        Context.MODE_PRIVATE
                    ))?.let { sharedPref ->
                        val editor = sharedPref.edit()
                        editor.putBoolean(GearupApp.IS_ANALYTICS_SUBMITTED, true)
                        editor.apply()
                    }
                }
            }

            MenuConfigurationService.fetchMenuConfiguration { menuList, err ->
                if (menuList != null) {
                    println(menuList)
                    menusList.postValue(menuList)
                } else if (error != null) println(err?.localizedMessage)
                isBusy.value = false
            }
        }
    }
}