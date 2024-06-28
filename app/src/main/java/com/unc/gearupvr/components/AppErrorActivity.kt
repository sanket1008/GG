package com.unc.gearupvr.components

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.unc.gearupvr.R
import com.unc.gearupvr.databinding.ActivityAppErrorBinding
import com.unc.gearupvr.model.GearupApp
import org.jetbrains.anko.sdk27.coroutines.onClick


class AppErrorActivity : AppCompatActivity() {
    companion object {

        fun createIntent(
            context: Context?,
            pageType: Int? = null
        ): Intent {
            val intent = Intent(context, AppErrorActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra(PAGE_TAG, pageType)
            return intent
        }

        private const val PAGE_TAG = "pageType"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val binding: ActivityAppErrorBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_app_error)

        when (intent.getIntExtra(PAGE_TAG, 0)) {
            404 -> {
                binding.descMsg.text = this.getString(R.string.error_404)
            }
            403, 401 -> {
                binding.descMsg.text = this.getString(R.string.error_403)
            }
            GearupApp.SSL_ERROR_CODE -> {
                binding.descMsg.text = this.getString(R.string.common_error_msg)
            }

        }
        binding.tryAgain.onClick { triggerRebirth(applicationContext) }

    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        triggerRebirth(applicationContext)
    }

    private fun triggerRebirth(context: Context) {
        val packageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(context.packageName)
        val componentName = (intent ?: return).component
        val mainIntent = Intent.makeRestartActivityTask(componentName)
        context.startActivity(mainIntent)
        Runtime.getRuntime().exit(0)
    }


}
