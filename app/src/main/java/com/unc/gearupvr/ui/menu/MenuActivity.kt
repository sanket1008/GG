package com.unc.gearupvr.ui.menu

import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.forEach
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.microsoft.appcenter.AppCenter
import com.microsoft.appcenter.analytics.Analytics
import com.microsoft.appcenter.crashes.Crashes
import com.unc.gearupvr.R
import com.unc.gearupvr.model.DashboardTile
import com.unc.gearupvr.model.MenuItem
import com.unc.gearupvr.ui.careers.CareersListFragment
import com.unc.gearupvr.ui.detail_page.DetailPageFragment
import com.unc.gearupvr.ui.disability_access.DisabilityListFragment
import com.unc.gearupvr.ui.home.HomeFragment
import com.unc.gearupvr.ui.visit_college_nc.VisitCollegeNCFragment
import com.unc.gearupvr.utils.ImageDownloader
import java.io.Serializable


@Suppress("UNCHECKED_CAST")
class MenuActivity : AppCompatActivity() {

    companion object {
        private const val MENU_ITEMS: String = "menuItems"
        private const val DETAIL_FRAGMENT: String = "DETAIL_FRAGMENT"
        fun getIntent(context: Context, menuItems: List<MenuItem>): Intent {
            val intent = Intent(context, MenuActivity::class.java)
            intent.putExtra(MENU_ITEMS, menuItems as? Serializable)
            return intent
        }
    }

    private lateinit var viewModel: MenuViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.menu_activity)
        AppCenter.start(
            application, "8434742e-691a-463c-bd7e-ee4e1ed2c26d",
            Analytics::class.java, Crashes::class.java
        )

        viewModel = ViewModelProviders.of(this)[MenuViewModel::class.java]

        viewModel.moreNavSelectedItem.observe(this, Observer {
            if (it != null) {
                val fragment = getFragment(it.layoutResID, it)
                changeFragment(fragment, isFirst = false, shouldAnimate = true)
            }
        })

        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        (intent.getSerializableExtra(MENU_ITEMS) as? List<MenuItem>)?.let { menuItems ->

            if (menuItems.isNotEmpty()) {

                viewModel.menusList.postValue(menuItems)

                navView.setOnNavigationItemSelectedListener { bottomBarItem ->

                    //TODO: if navigation item is reselected, it should make use of back trace instead of reinitializing the fragment

                    supportFragmentManager.popBackStack(
                        null,
                        FragmentManager.POP_BACK_STACK_INCLUSIVE
                    )

                    val menuItem: MenuItem? =
                        menuItems.firstOrNull { bottomBarItem.itemId == it.layoutResID }

                    val fragment = getFragment(
                        bottomBarItem.itemId,
                        menuItem
                    )

                    this.changeFragment(fragment, isFirst = true, shouldAnimate = false)
                    return@setOnNavigationItemSelectedListener true
                }

                for (index in 0..minOf(menuItems.size, MenuViewModel.MAX_MENU_ITEMS_COUNT)) {
                    if (index < MenuViewModel.MAX_MENU_ITEMS_COUNT) {

                        //add(int groupId, int itemId, int order, CharSequence title)
                        val item = navView.menu.add(
                            Menu.NONE,
                            menuItems[index].layoutResID,
                            menuItems[index].order,
                            menuItems[index].title

                        )

                        val bitmap = menuItems[index].icon?.let { ImageDownloader.getImage(it) }
                        if (bitmap != null)
                            item.icon = BitmapDrawable(resources, bitmap)
                        else
                            item.setIcon(R.drawable.ic_dashboard_black_24dp)

                    } else {
                        navView.menu.add(
                            Menu.NONE,
                            R.id.navigation_more,
                            index,
                            "More"
                        ).setIcon(R.drawable.ic_more_nav)
                    }
                }

                val menuView = navView.getChildAt(0) as? ViewGroup ?: return

                menuView.forEach {
                    it.findViewById<View>(R.id.largeLabel)?.setPadding(0, 0, 0, 0)
                    val params = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )

                    it.findViewById<View>(R.id.largeLabel)?.layoutParams = params


                }
                if (navView.menu.size() > 0)
                    navView.selectedItemId = navView.menu[0].itemId
            }
        }
    }

    private fun getFragment(
        layoutResID: Int,
        item: MenuItem?
    ): Fragment {
        return when (layoutResID) {
            R.id.navigation_home -> HomeFragment(item?.title ?: "")
            R.id.navigation_visit_college_nc -> VisitCollegeNCFragment.newInstance(
                item?.title ?: ""
            )
            R.id.navigation_visit_careers -> CareersListFragment.newInstance(item?.title ?: "")
            R.id.navigation_more -> MoreNavFragment(viewModel)
            R.id.navigation_custom_page -> {
                DetailPageFragment.newInstance(
                    DashboardTile(
                        description = "",
                        image = "",
                        page = item?.page,
                        url = item?.url,
                        urlType = item?.urlType.toString(),
                        title = item?.title.toString()
                    )
                )

            }
            R.id.nav_disability_access -> DisabilityListFragment()
            else -> Fragment(R.layout.coming_soon)
        }
    }

    fun changeFragment(newFragment: Fragment, isFirst: Boolean, shouldAnimate: Boolean) {

        val oldFragment = supportFragmentManager.findFragmentByTag(DETAIL_FRAGMENT)
        if (oldFragment != newFragment) {
            val fragmentTransaction = supportFragmentManager.beginTransaction()

            if (shouldAnimate) {
                fragmentTransaction.setCustomAnimations(
                    R.anim.enter_from_right,
                    R.anim.exit_to_left,
                    R.anim.enter_from_left,
                    R.anim.exit_to_right
                )
            }
            if (!isFirst) {
                fragmentTransaction.add(R.id.content, newFragment, DETAIL_FRAGMENT)
                fragmentTransaction.addToBackStack(null)
            } else {
                fragmentTransaction.replace(R.id.content, newFragment, DETAIL_FRAGMENT)
            }
            fragmentTransaction.commit()
        }

    }
}
