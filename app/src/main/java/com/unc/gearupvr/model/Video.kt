package com.unc.gearupvr.model

import com.unc.gearupvr.BuildConfig
import java.io.Serializable

data class Video(
    val name: String = "",
    val uid: String = "",
    val url: String = ""
) : Serializable {
    var imageURL: String
        get() {
            return if (thumbnail.isNullOrBlank()) "" else "https://" + BuildConfig.API_BASE + "/" + thumbnail
        }
        set(_) {}

    private var thumbnail: String? = null
}