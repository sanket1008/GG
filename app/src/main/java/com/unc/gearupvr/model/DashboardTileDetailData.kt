package com.unc.gearupvr.model

import com.google.gson.annotations.SerializedName

data class DashboardTileDetailData(
    @SerializedName("dark_mode_content")
    val darkModeContent: String? = null,
    @SerializedName("light_mode_content")
    val lightModeContent: String? = null,
    val video: Video? = Video(),
    val title: String = "",
    val uid: String = "",
    val active: String = ""
)