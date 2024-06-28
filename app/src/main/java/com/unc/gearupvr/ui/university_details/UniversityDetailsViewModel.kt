package com.unc.gearupvr.ui.university_details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.unc.gearupvr.model.CollegeDetail
import com.unc.gearupvr.service.CareersService
import com.unc.gearupvr.service.CollegeService


class UniversityDetailsViewModel : ViewModel() {
    val isBusy: MutableLiveData<Boolean> = MutableLiveData(false)
    val isUrlError: MutableLiveData<Boolean> = MutableLiveData(false)
    val page: LiveData<CollegeDetail> get() = _page
    private val _page = MutableLiveData<CollegeDetail>()
    private var _isEmail = MutableLiveData<Boolean>()
    private var _isFB = MutableLiveData<Boolean>()
    private var _isTwitter = MutableLiveData<Boolean>()
    private var _isLinkedIn = MutableLiveData<Boolean>()
    private var _isWeb = MutableLiveData<Boolean>()
    private var _isPhone = MutableLiveData<Boolean>()
    private var _isVideo = MutableLiveData<Boolean>()
    private var _isTags = MutableLiveData<Boolean>()
    private var _isInsta = MutableLiveData<Boolean>()
    private val _name = MutableLiveData<String>()
    val isEmail: LiveData<Boolean> get() = _isEmail
    val isFB: LiveData<Boolean> get() = _isFB
    val isTwitter: LiveData<Boolean> get() = _isTwitter
    val isLinkedIn: LiveData<Boolean> get() = _isLinkedIn
    val isWeb: LiveData<Boolean> get() = _isWeb
    val isPhone: LiveData<Boolean> get() = _isPhone
    val isVideo: LiveData<Boolean> get() = _isVideo
    val isTags: LiveData<Boolean> get() = _isTags
    val isInstagram: LiveData<Boolean> get() = _isInsta
    val name: LiveData<String> get() = _name


    fun loadData(uid: String? = null) {

        isBusy.value = true
        CollegeService.getCollegeDetails(uid) { result, _ ->
            if (result != null) {
                println(result)

                _page.postValue(result)
                _isEmail.postValue(!result.email.isNullOrEmpty())
                _isFB.postValue(!result.facebookUrl.isNullOrEmpty())
                _isTwitter.postValue(!result.twitterUrl.isNullOrEmpty())
                _isLinkedIn.postValue(!result.linkedInUrl.isNullOrEmpty())
                _isWeb.postValue(!result.website.isNullOrEmpty())
                _isPhone.postValue(!result.phoneNumber.isNullOrEmpty())
                _isVideo.postValue(!result.video?.name.isNullOrEmpty())
                _name.postValue(result.name)
                if (result.tagsList.isNotEmpty())
                    _isTags.postValue(true)
                else
                    _isTags.postValue(false)
            } else {
                isUrlError.value = true
            }
            isBusy.value = false
        }
    }

    fun loadCareerData(uid: String? = null) {

        isBusy.value = true
        CareersService.getCareersDetails(uid) { result, _ ->
            if (result != null) {
                println(result)

                _page.postValue(result)
                _isEmail.postValue(!result.email.isNullOrEmpty())
                _isFB.postValue(!result.facebookUrl.isNullOrEmpty())
                _isTwitter.postValue(!result.twitterUrl.isNullOrEmpty())
                _isLinkedIn.postValue(!result.linkedInUrl.isNullOrEmpty())
                _isWeb.postValue(!result.website.isNullOrEmpty())
                _isPhone.postValue(!result.phoneNumber.isNullOrEmpty())
                _isVideo.postValue(!result.video?.name.isNullOrEmpty())
                _isInsta.postValue(!result.instagramUrl.isNullOrEmpty())
                _name.postValue(result.career)
            } else {
                isUrlError.value = true
            }
            isBusy.value = false
        }
    }

}



