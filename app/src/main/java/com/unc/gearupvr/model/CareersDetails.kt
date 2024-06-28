package com.unc.gearupvr.model

import com.google.gson.annotations.SerializedName

data class CareersDetails(
    val uid: String? = null,
    val video: Video? = null,
    val logo: String? = null,
    val location: Location? = null,
    @SerializedName("dark_mode_content")
    val darkModeContent: String? = null,
    @SerializedName("light_mode_content")
    val lightModeContent: String? = null,
    val career: String? = null,
    val email: String? = null,
    @SerializedName("phone_number")
    val phoneNumber: String? = null,
    @SerializedName("facebook_url")
    val facebookUrl: String? = null,
    @SerializedName("twitter_url")
    val twitterUrl: String? = null,
    @SerializedName("linkedin_url")
    val linkedInUrl: String? = null,
    @SerializedName("instagram_url")
    val instagramUrl: String? = null,
    val website: String? = null
)