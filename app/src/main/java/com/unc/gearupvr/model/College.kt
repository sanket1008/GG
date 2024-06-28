package com.unc.gearupvr.model

import com.google.android.gms.maps.model.Marker
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class College(
    val description: String? = null, // The University of Chicago, situated in Chicago's Hyde Park community, offers a rich campus life in a big-city setting. The Chicago Maroons have more than 15 NCAA Division III teams, which compete in the University Athletic Association, and have strong basketball and wrestling programs. At Chicago, freshmen are required to live on campus, and more than 50 percent of students choose to remain on campus, while others live in off-campus apartments and houses. On-campus students are placed in "houses" within their dorm, which serve as tight-knit communities and provide academic and social support. Chicago offers more than 400 student organizations.
    val location: Location? = null,
    val logo: String? = null, // null
    val name: String? = null, // University of Chicago
    val uid: String? = null, // e09ed91b-46ec-4163-ad33-b7139bc4f72b
    @SerializedName("disability_access_url")
    val disabilityUrl: String = ""
) : Serializable {
    var marker: Marker? = null
    var tagsList: List<String>
        get() {
            return if (tags.isNullOrBlank()) emptyList() else tags?.split(",") ?: emptyList()
        }
        set(_) {}

    private var tags: String? = null

}