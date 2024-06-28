package com.unc.gearupvr.model

import com.google.gson.annotations.SerializedName

data class DashboardData(

    @SerializedName("active_tiles")
    val activeTiles: List<DashboardTile> = listOf(),

    val title: String = "",
    val uid: String = "",
    val video: Video? = Video()
)