package com.unc.gearupvr.service

import com.google.gson.Gson
import com.unc.gearupvr.model.GearupApp
import com.unc.gearupvr.model.HighSchool
import com.unc.gearupvr.model.UserType

object ServiceUserDetails {

    fun fetchHighSchoolData(
        hint: String,
        completionHandler: ((searchKey: String, highSchoolList: List<HighSchool>?, Error?) -> Unit)
    ) {
        val param: HashMap<String, String> = hashMapOf()
        param["query"] = hint
        val request = ApiRequest(API.HighSchoolSearch, param)

        request.invoke { result, error, _ ->
            var highSchoolData: List<HighSchool> = emptyList()
            var err: Error? = null
            when {
                result != null -> highSchoolData =
                    Gson().fromJson(result.toString(), Array<HighSchool>::class.java).toList()
                error != null -> //Placeholder for generating custom error message for UI
                    err = error
                else -> err =
                    Error("The server encountered an unexpected condition that prevented it from fulfilling the request.")
            }

            completionHandler(hint, highSchoolData, err)
        }
    }


    fun postHighSchoolDetails(
        userType: UserType,
        schoolUID: String? = null,
        customSchool: String? = null,
        completionHandler: ((String, Error?) -> Unit)
    ) {

        val deviceID: String = GearupApp.deviceId ?: return
        val param: HashMap<String, String> = hashMapOf()
        param["device_id"] = deviceID
        param["user_type"] = userType.uid
        if (schoolUID != null) param["school"] = schoolUID
        if (customSchool != null) param["custom_school"] = customSchool

        val request = ApiRequest(API.HighSchoolPost, param)

        request.invoke { result, error, _ ->
            var data = ""
            var err: Error? = null
            when {
                result != null -> data = result.toString()
                error != null -> //Placeholder for generating custom error message for UI
                    err = error
                else -> err =
                    Error("The server encountered an unexpected condition that prevented it from fulfilling the request.")
            }

            completionHandler(data, err)
        }
    }

}