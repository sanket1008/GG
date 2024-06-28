package com.unc.gearupvr.service

import com.google.gson.Gson
import com.unc.gearupvr.model.DashboardData

object DashboardService {
    fun fetchHomeScreenDetails(completionHandler: ((DashboardData?, Error?) -> Unit)) {
        val request = ApiRequest(API.DashboardData)
        request.invoke { result, error, _ ->
            var dashboardData: DashboardData? = null
            var err: Error? = null
            when {
                result != null -> dashboardData =
                    Gson().fromJson(result.toString(), DashboardData::class.java)
                error != null -> //Placeholder for generating custom error message for UI
                    err = error
                else -> err =
                    Error("The server encountered an unexpected condition that prevented it from fulfilling the request.")
            }

            completionHandler(dashboardData, err)
        }
    }
}