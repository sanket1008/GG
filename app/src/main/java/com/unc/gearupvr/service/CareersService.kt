package com.unc.gearupvr.service

import com.google.gson.Gson
import com.unc.gearupvr.model.CareersResponse
import com.unc.gearupvr.model.CollegeDetail

object CareersService {
    fun fetchCareersList(
        limit: Int,
        offset: Int,
        completionHandler: ((CareersResponse?, Error?) -> Unit)
    ) {

        val param: HashMap<String, String> = hashMapOf()
        param["offset"] = offset.toString()
        param["limit"] = limit.toString()
        val request = ApiRequest(API.Careers, param)
        request.invoke { result, error, _ ->
            var colleges: CareersResponse? = null
            var e: Error? = null

            try {
                when {
                    result != null -> colleges = Gson()
                        .fromJson(result.toString(), CareersResponse::class.java)
                    error != null -> //Placeholder for generating custom error message for UI
                        e = error
                    else -> e =
                        Error("The server encountered an unexpected condition that prevented it from fulfilling the request.")
                }
            } catch (exception: Exception) {
                e = Error(exception.localizedMessage)
            }

            completionHandler(colleges, e)
        }
    }

    fun getCareersDetails(
        uid: String? = null,
        completionHandler: ((CollegeDetail?, Error?) -> Unit)
    ) {
        val path: ArrayList<String?> = arrayListOf(uid)
        val request = ApiRequest(API.CareersDetails, null, path)
        request.invoke { result, error, _ ->
            var careersDetails: CollegeDetail? = null
            var e: Error? = null

            try {
                when {
                    result != null -> careersDetails =
                        Gson().fromJson(result.toString(), CollegeDetail::class.java)
                    error != null -> //Placeholder for generating custom error message for UI
                        e = error
                    else -> e =
                        Error("The server encountered an unexpected condition that prevented it from fulfilling the request.")
                }

            } catch (exception: Exception) {
                e = Error(exception.localizedMessage)
            }

            completionHandler(careersDetails, e)


        }
    }
}
