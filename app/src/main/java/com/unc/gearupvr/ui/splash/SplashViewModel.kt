package com.unc.gearupvr.ui.splash

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.unc.gearupvr.model.GearupApp
import com.unc.gearupvr.model.MenuItem
import com.unc.gearupvr.service.MenuConfigurationService
import kotlinx.coroutines.*

class SplashViewModel : ViewModel() {

    val isBusy: MutableLiveData<Boolean> = MutableLiveData(false)

    val splashState: LiveData<SplashState> get() = _splashState
    private val _splashState = MutableLiveData<SplashState>()

    var menusList: List<MenuItem> = emptyList()

    fun loadAPI() {
        GlobalScope.launch {
            delay(3000)
            (GearupApp.ctx?.getSharedPreferences(
                GearupApp.SHARED_PREFERENCES,
                Context.MODE_PRIVATE
            ))?.let { sharedPref ->
                if (sharedPref.getBoolean(GearupApp.IS_ANALYTICS_SUBMITTED, false)) {

                    withContext(Dispatchers.Main) {
                        isBusy.value = true
                    }

                    MenuConfigurationService.fetchMenuConfiguration { menuList, error ->
                        if (menuList != null) {
                            println(menuList)
                            menusList = menuList
                            _splashState.postValue(SplashState.MenuActivity)
                        } else if (error != null) println(error.localizedMessage)
                        isBusy.value = false
                    }
                } else {
                    _splashState.postValue(SplashState.UserDetailsActivity)
                }
            }

        }
    }
}

sealed class SplashState {
    object UserDetailsActivity : SplashState()
    object MenuActivity : SplashState()
}