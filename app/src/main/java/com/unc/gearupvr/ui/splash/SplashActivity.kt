package com.unc.gearupvr.ui.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import com.unc.gearupvr.BuildConfig
import com.unc.gearupvr.R
import com.unc.gearupvr.ui.menu.MenuActivity
import com.unc.gearupvr.ui.user_details.UserDetailsActivity
import com.wang.avi.AVLoadingIndicatorView

class SplashActivity : AppCompatActivity() {
    lateinit var viewModel: SplashViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.splash_activity)
        AppCenter.start(
            application, BuildConfig.APP_CENTER_KEY,
            Analytics::class.java, Crashes::class.java
        )
//        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        viewModel = ViewModelProviders.of(this).get(SplashViewModel::class.java)

        val indicator = findViewById<AVLoadingIndicatorView>(R.id.avi)
        //Show/Hide activity indicator based on viewModel busy status
        viewModel.isBusy.observe(this, Observer {
            if (it) {
                indicator.show()
            } else {
                indicator.hide()
            }
        })

        viewModel.splashState.observe(this, Observer {
            when (it) {
                SplashState.UserDetailsActivity -> startActivity(
                    Intent(
                        this,
                        UserDetailsActivity::class.java
                    )
                )
                SplashState.MenuActivity -> {

                    startActivity(MenuActivity.getIntent(this, viewModel.menusList))
                }
            }
            finish()
        })
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadAPI()
    }
}