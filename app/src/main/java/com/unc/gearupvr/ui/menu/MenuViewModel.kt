package com.unc.gearupvr.ui.menu

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.unc.gearupvr.model.MenuItem

class MenuViewModel : ViewModel() {

    companion object {
        const val MAX_MENU_ITEMS_COUNT: Int = 3
    }

    var menusList: MutableLiveData<List<MenuItem>> = MutableLiveData(emptyList())
    var moreNavSelectedItem: MutableLiveData<MenuItem> = MutableLiveData()
    fun moreNavigationItemSelected(item: MenuItem) {
        moreNavSelectedItem.postValue(item)
    }
}