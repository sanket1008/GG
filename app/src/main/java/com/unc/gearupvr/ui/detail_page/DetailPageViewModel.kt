package com.unc.gearupvr.ui.detail_page

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.unc.gearupvr.model.DashboardTileDetailData
import com.unc.gearupvr.service.API
import com.unc.gearupvr.service.ApiRequest

class DetailPageViewModel : ViewModel() {

    val isBusy: MutableLiveData<Boolean> = MutableLiveData(false)
    val page: LiveData<DashboardTileDetailData> get() = _page
    private val _page = MutableLiveData<DashboardTileDetailData>()

    fun loadData(uid: String? = null) {
        isBusy.value = true
        val path: ArrayList<String?> = arrayListOf(uid)
        val request = ApiRequest(API.DetailPage, null, path)
        request.invoke { result, _, _ ->
            if (result != null) {
                println(result)
                val gson = Gson()
                val detailPageData =
                    gson.fromJson(result.toString(), DashboardTileDetailData::class.java)
                _page.postValue(detailPageData)
            }
            isBusy.value = false
        }
    }

}