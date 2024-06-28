package com.unc.gearupvr.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.unc.gearupvr.model.DashboardData
import com.unc.gearupvr.model.DashboardTile
import com.unc.gearupvr.service.DashboardService
import kotlin.reflect.KProperty

class HomeViewModel : ViewModel() {

    private val _isBusy: MutableLiveData<Boolean> = MutableLiveData(false)
    val isBusy: LiveData<Boolean> = _isBusy
    var selectedTile = MutableLiveData<DashboardTile>()

    fun selected(item: DashboardTile) {
        if (selectedTile.value != item)
            selectedTile.postValue(item)
    }

    private var _dashboardData: MutableLiveData<DashboardData> by lazy {
        MutableLiveData<DashboardData>().also {
            loadData()
        }
    }
    val dashboardData: LiveData<DashboardData> = _dashboardData

    fun loadData() {
        _isBusy.value = true
        DashboardService.fetchHomeScreenDetails { data, error ->
            when {
                data != null -> _dashboardData.postValue(data)
                error != null -> print(error.localizedMessage)
            }
            _isBusy.value = false
        }
    }
}

private operator fun Any.setValue(
    homeViewModel: HomeViewModel,
    property: KProperty<*>,
    mutableLiveData: MutableLiveData<DashboardData>
) {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
}


