package com.unc.gearupvr.ui.user_details

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.unc.gearupvr.R
import com.unc.gearupvr.ui.menu.MenuActivity
import com.unc.gearupvr.ui.user_details.highSchool.HighSchoolFragment
import com.unc.gearupvr.ui.user_details.user_type.UserTypeFragment


class UserDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.user_details_activity)

        val viewModel = ViewModelProviders.of(this)[UserDetailsViewModel::class.java]
        changeFragment(UserTypeFragment.newInstance(viewModel), true)

        viewModel.selectedUserType.observe(this, Observer {
            if (it != null) {
                if (it.title.equals("other", true)) {
                    viewModel.saveButtonPressed()
                } else {
                    changeFragment(
                        HighSchoolFragment.newInstance(viewModel),
                        isFirst = false,
                        shouldAnimate = true
                    )
                }

            }
        })

        viewModel.menusList.observe(this, Observer {
            if (it != null && it.isNotEmpty()) {
                startActivity(MenuActivity.getIntent(this, it))
                finish()
            }
        })
    }


    private fun changeFragment(
        newFragment: Fragment,
        isFirst: Boolean,
        shouldAnimate: Boolean = false
    ) {
        val fragmentTransaction =
            supportFragmentManager.beginTransaction()

        if (shouldAnimate) {
            fragmentTransaction.setCustomAnimations(
                R.anim.enter_from_right,
                R.anim.exit_to_left,
                R.anim.enter_from_left,
                R.anim.exit_to_right
            )
        }

        fragmentTransaction.replace(R.id.container, newFragment)

        if (!isFirst) {
            fragmentTransaction.addToBackStack(newFragment.javaClass.name)
        }
        fragmentTransaction.commit()
    }
}
