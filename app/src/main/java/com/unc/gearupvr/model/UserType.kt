package com.unc.gearupvr.model

import android.graphics.drawable.Drawable
import com.unc.gearupvr.R

data class UserType(
    val title: String,
    val uid: String
) {
    var imageRes: Drawable?
        get() {
            return when (this.title) {
                "Student" -> GearupApp.ctx?.getDrawable(R.drawable.ic_student)
                "Teacher" -> GearupApp.ctx?.getDrawable(R.drawable.ic_teacher)
                "Parent" -> GearupApp.ctx?.getDrawable(R.drawable.ic_parent)
                "Non Traditional" -> GearupApp.ctx?.getDrawable(R.drawable.ic_nontraditional)
                "Other" -> GearupApp.ctx?.getDrawable(R.drawable.ic_others)
                else -> {
                    GearupApp.ctx?.getDrawable(R.drawable.ic_default_icon)
                }
            }

        }
        set(_) {}
}