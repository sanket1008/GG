package com.unc.gearupvr.service

import com.google.android.gms.maps.model.LatLngBounds
import com.google.gson.Gson
import com.unc.gearupvr.model.CollegeResponse
import com.unc.gearupvr.model.Majors
import com.unc.gearupvr.model.RangeLimit
import com.unc.gearupvr.model.StudentCapacity
import com.unc.gearupvr.model.CollegeDetail

object CollegeService {
    fun fetchCollageList(
        latLngBounds: LatLngBounds? = null,
        majors: List<Majors>? = null,
        query: String? = null,
        stateType: String? = null,
        satRange: RangeLimit? = null,
        actRange: RangeLimit? = null,
        costRange: RangeLimit? = null,
        disabilityAccess: Boolean? = null,
        studentCapacity: StudentCapacity? = null,
        limit: Int? = null,
        offset: Int? = null,
        completionHandler: ((CollegeResponse?, Error?) -> Unit)
    ) {

        val filter: HashMap<String, Any> = HashMap()

        if (disabilityAccess != null) {
            filter["disability_access"] = disabilityAccess
        } else {
            if (satRange != null) {
                filter["sat_score_range"] = satRange.toHashMap()
            }
            if (actRange != null) {
                filter["act_score_range"] = actRange.toHashMap()
            }
            if (costRange != null) {
                filter["avg_cost_range"] = costRange.toHashMap()
            }
            val latLang: HashMap<String, Any> = HashMap()
            if (latLngBounds != null) {
                latLang["lat1"] = latLngBounds.northeast.latitude
                latLang["lng1"] = latLngBounds.northeast.longitude
                latLang["lat2"] = latLngBounds.southwest.latitude
                latLang["lng2"] = latLngBounds.southwest.longitude
                filter["location_poly"] = latLang
            }
            if (studentCapacity != null) {
                val studentBodySize: HashMap<String, Any?> = HashMap()
                studentBodySize["operator"] = studentCapacity.operator
                studentBodySize["value1"] = studentCapacity.valueOne
                studentBodySize["value2"] = studentCapacity.valueTwo
                filter["student_capacity"] = studentBodySize
            }
            if (!stateType.isNullOrEmpty())
                filter["state_type"] = stateType
            if (!majors.isNullOrEmpty()) {
                filter["majors"] = majors.map { it.uid }
            }
        }



        if (!query.isNullOrEmpty())
            filter["query"] = query

        val pagination: HashMap<String, Any?> = hashMapOf("limit" to limit, "offset" to offset)
        val param: HashMap<String, Any> = hashMapOf("filter" to filter, "pagination" to pagination)
        val request = ApiRequest(API.ListColleges, param)
        request.invoke { result, error, _ ->
            var colleges: CollegeResponse? = null
            var e: Error? = null

            try {
                when {
                    result != null -> colleges = Gson()
                        .fromJson(result.toString(), CollegeResponse::class.java)
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

    fun getCollegeDetails(
        uid: String? = null,
        completionHandler: ((CollegeDetail?, Error?) -> Unit)
    ) {
        val path: ArrayList<String?> = arrayListOf(uid)
        val request = ApiRequest(API.CollegeDetails, null, path)
        request.invoke { result, error, _ ->
            var collegeDetails: CollegeDetail? = null
            var e: Error? = null

            try {
                when {
                    result != null -> collegeDetails =
                        Gson().fromJson(result.toString(), CollegeDetail::class.java)
                    error != null -> //Placeholder for generating custom error message for UI
                        e = error
                    else -> e =
                        Error("The server encountered an unexpected condition that prevented it from fulfilling the request.")
                }

            } catch (exception: Exception) {
                e = Error(exception.localizedMessage)
            }

            completionHandler(collegeDetails, e)


        }
    }
}