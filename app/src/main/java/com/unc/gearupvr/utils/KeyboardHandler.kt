package com.unc.gearupvr.utils

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager

class KeyboardHandler {
    companion object {
        fun hideKeyboard(activity: Activity) {
            (activity.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.let { inputMethodManager ->
                (activity.currentFocus)?.let { currentFocus ->
                    inputMethodManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
                }
            }
        }
    }
}