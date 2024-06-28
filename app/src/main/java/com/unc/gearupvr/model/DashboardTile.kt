package com.unc.gearupvr.model

import com.google.gson.annotations.SerializedName

data class DashboardTile(
    val description: String = "",
    val image: String = "",
    val order: Int = 0,
    val title: String = "",
    val url: String? = "",
    val page: String? = "",

    @SerializedName("url_type")
    val urlType: String = ""
)