package com.unc.gearupvr.model

import com.google.gson.annotations.SerializedName
import com.unc.gearupvr.R
import java.io.Serializable

data class MenuItem(
    val active: Boolean? = null, // true
    val icon: String? = null, // media/iconfinder_about_2639759.png
    @SerializedName("key_name")
    val keyName: String? = null, // ABOUT_US
    val order: Int = 0, // 7
    val page: String? = null, // a62783ba-2629-4a26-845e-7a1bb7e76536
    val title: String? = null, // About Us
    val uid: String? = null, // 8148a012-b1a7-4e90-9ce4-864177bcadf2
    val url: String? = null,
    @SerializedName("url_type")
    val urlType: String? = null // pre_defined
) : Serializable {

    var layoutResID: Int
        get() {
            return when (this.keyName) {
                "HOME" -> R.id.navigation_home
                "NC_COLLEGES" -> R.id.navigation_visit_college_nc
                null -> R.id.navigation_custom_page
                "DISABILITY_ACCESS" -> R.id.nav_disability_access
                "CAREERS" -> R.id.navigation_visit_careers
                else -> R.id.navigation_coming_soon


            }
        }
        set(_) {}

}