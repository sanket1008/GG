package com.unc.gearupvr.utils

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import com.unc.gearupvr.R


class DialogUtils {

    companion object {
        const val positiveButton = "positive"
        const val negativeButton = "negative"
        const val neutralButton = "neutral"

        fun showAlert(
            context: Context,
            title: String? = null,
            msgBody: String? = null,
            positiveButtonText: String? = null,
            negativeButtonText: String? = null,
            neutralButtonText: String? = null, onItemClick: ((buttonTag: String) -> Unit)? = null
        ) {

            val alert =
                AlertDialog.Builder(context, R.style.CustomDialogTheme)
            alert.setMessage(msgBody)
            alert.setTitle(title)
            alert.setPositiveButton(positiveButtonText) { _: DialogInterface, _: Int ->
                onItemClick?.invoke(positiveButton)
            }
            alert.setNegativeButton(negativeButtonText) { _: DialogInterface, _: Int ->
                onItemClick?.invoke(negativeButton)
            }
            alert.setNeutralButton(neutralButtonText) { _: DialogInterface, _: Int ->
                onItemClick?.invoke(neutralButton)
            }
            alert.show()
        }

    }


}