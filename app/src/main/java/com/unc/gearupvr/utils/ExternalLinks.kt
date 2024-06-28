package com.unc.gearupvr.utils

import android.content.Context
import android.content.Intent
import android.net.Uri

class ExternalLinks {
    companion object {
        fun openUrl(context: Context, url: String) {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(url)
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }

        fun sendSms(context: Context, uri: Uri) {
            val intent = Intent(Intent.ACTION_SENDTO, uri)
            intent.putExtra("sms_body", "")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}