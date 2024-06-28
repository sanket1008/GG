package com.unc.gearupvr.model

import com.google.gson.annotations.SerializedName

data class CollegeDetail(

    val uid: String? = null,
    val video: Video? = null,
    val tiles: List<DashboardTile>? = null,
    val logo: String? = null,
    val location: Location? = null,
    val majors: List<Majors>? = null,
    @SerializedName("dark_mode_content")
    val darkModeContent: String? = null,
    @SerializedName("light_mode_content")
    val lightModeContent: String? = null,
    val name: String? = null,
    @SerializedName("short_name")
    val shortName: String? = null,
    val email: String? = null,
    @SerializedName("student_capacity")
    val studentCapacity: Int? = null,
    @SerializedName("phone_number")
    val phoneNumber: String? = null,
    @SerializedName("facebook_url")
    val facebookUrl: String? = null,
    @SerializedName("twitter_url")
    val twitterUrl: String? = null,
    @SerializedName("linkedin_url")
    val linkedInUrl: String? = null,
    val website: String? = null,
    @SerializedName("in_state_sat_score")
    val inStateSatScore: String? = null,
    @SerializedName("in_state_act_score")
    val inStateActScore: String? = null,
    @SerializedName("in_state_cost_per_year")
    val inStateCostPerYear: String? = null,
    @SerializedName("out_state_sat_score")
    val outStateSatScore: String? = null,
    @SerializedName("out_state_act_score")
    val outStateActScore: String? = null,
    @SerializedName("out_state_cost_per_year")
    val outStateCostPerYear: String? = null,
    @SerializedName("disability_access")
    val disabilityAccess: Boolean? = null,
    @SerializedName("disability_access_url")
    val disabilityAccessUrl: String? = null,
    val address: String? = null,
    @SerializedName("instagram_url")
    val instagramUrl: String? = null,
    val career: String? = null
) {
    var tagsList: List<String>
        get() {
            return if (tags.isNullOrBlank()) emptyList() else tags?.split(",") ?: emptyList()
        }
        set(_) {}

    private var tags: String? = null
}