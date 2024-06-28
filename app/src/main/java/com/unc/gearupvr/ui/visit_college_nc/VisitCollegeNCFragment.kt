package com.unc.gearupvr.ui.visit_college_nc


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.unc.gearupvr.R
import com.unc.gearupvr.components.SwitchTrackTextDrawable
import com.unc.gearupvr.databinding.VisitCollegeNcFragmentBinding
import com.unc.gearupvr.model.GearupApp
import com.unc.gearupvr.ui.visit_college_nc.filter.FilterActivity
import com.unc.gearupvr.utils.KeyboardHandler
import org.jetbrains.anko.sdk27.coroutines.onCheckedChange
import org.jetbrains.anko.sdk27.coroutines.onClick


class VisitCollegeNCFragment : Fragment() {

    companion object {
        fun newInstance(_title: String): VisitCollegeNCFragment {
            val fragment = VisitCollegeNCFragment()
            val args = Bundle()
            args.putString(GearupApp.NAV_TITLE, _title)
            fragment.arguments = args
            return fragment
        }

        private const val LIST_VIEW_FRAGMENT: String = "LIST_VIEW_FRAGMENT"
    }

    private lateinit var viewModel: VisitCollegeNCViewModel
    private lateinit var listViewFragment: CollegesListViewFragment
    private lateinit var mapViewFragment: CollegeMapFragment
    lateinit var binding: VisitCollegeNcFragmentBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = VisitCollegeNcFragmentBinding.inflate(inflater, container, false)

        viewModel = ViewModelProviders.of(this).get(VisitCollegeNCViewModel::class.java)
        listViewFragment = CollegesListViewFragment.newInstance(viewModel)
        mapViewFragment = CollegeMapFragment.newInstance(viewModel)

        binding.navTitle.text = arguments?.getString(GearupApp.NAV_TITLE) ?: ""


        //Set first view as list view
        activity?.supportFragmentManager?.beginTransaction()
            ?.replace(R.id.list_content, listViewFragment, LIST_VIEW_FRAGMENT)
            ?.commit()


        //setup toggle button
        binding.toggleButton.trackDrawable =
            context?.let { SwitchTrackTextDrawable(it, R.string.list_view, R.string.map_view) }
        binding.toggleButton.onCheckedChange { _, isChecked ->
            val fragmentTransaction = activity?.supportFragmentManager?.beginTransaction()
            if (isChecked) {
                fragmentTransaction?.setCustomAnimations(
                    R.anim.enter_from_right,
                    R.anim.exit_to_left,
                    R.anim.enter_from_left,
                    R.anim.exit_to_right
                )

                fragmentTransaction?.replace(
                    R.id.list_content,
                    mapViewFragment,
                    LIST_VIEW_FRAGMENT
                )
            } else {
                fragmentTransaction?.setCustomAnimations(
                    R.anim.enter_from_left,
                    R.anim.exit_to_right,
                    R.anim.enter_from_right,
                    R.anim.exit_to_left
                )
                fragmentTransaction?.replace(
                    R.id.list_content,
                    listViewFragment,
                    LIST_VIEW_FRAGMENT
                )
            }
            fragmentTransaction?.commit()
        }

        viewModel.isBusy.observe(this, Observer {
            binding.toggleButton.isEnabled = !it
        })
        binding.imgFilter.onClick {
            activity?.let {
                val intent = Intent(it, FilterActivity::class.java)
                it.startActivity(intent)
            }
        }

        binding.imgSearch.onClick {
            binding.cardSearch.visibility = View.VISIBLE
            context?.let { context -> showKeyboard(context) }
            binding.searchCollege.requestFocus()
            binding.cardSearch.startAnimation(inFromRightAnimation())
        }

        binding.searchCollege.setOnEditorActionListener { _, action, _ ->
            var handled = false
            if (action == EditorInfo.IME_ACTION_DONE) {
                (activity?.supportFragmentManager?.findFragmentByTag(LIST_VIEW_FRAGMENT) as? CollegesListViewFragment)?.search(
                    binding.searchCollege.text.toString()
                )
                activity?.let { KeyboardHandler.hideKeyboard(it) }
                handled = true
            }
            handled
        }

        binding.searchCollege.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                binding.searchCollege.text.clear()

                if (event.rawX <= (binding.searchCollege.left +
                            binding.searchCollege.compoundDrawablePadding +
                            binding.searchCollege.compoundDrawables[0].bounds.width())
                ) {
                    activity?.let { KeyboardHandler.hideKeyboard(it) }
                    binding.cardSearch.visibility = View.GONE
                    (activity?.supportFragmentManager?.findFragmentByTag(LIST_VIEW_FRAGMENT) as? CollegesListViewFragment)?.search(
                        binding.searchCollege.text.toString()
                    )


                }
            }
            false
        }



        return binding.root
    }


    private fun showKeyboard(context: Context) {
        (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).toggleSoftInput(
            InputMethodManager.SHOW_FORCED,
            InputMethodManager.HIDE_IMPLICIT_ONLY
        )
    }

    private fun inFromRightAnimation(): Animation? {
        val inFromRight: Animation = TranslateAnimation(
            Animation.RELATIVE_TO_PARENT, +1.0f,
            Animation.RELATIVE_TO_PARENT, 0.0f,
            Animation.RELATIVE_TO_PARENT, 0.0f,
            Animation.RELATIVE_TO_PARENT, 0.0f
        )
        inFromRight.duration = 200
        inFromRight.interpolator = AccelerateInterpolator()
        return inFromRight
    }

    override fun onResume() {
        super.onResume()
        if (viewModel.isFilter) {
            binding.imgFilter.setBackgroundResource(R.drawable.ic_filter_full)
        } else
            binding.imgFilter.setBackgroundResource(R.drawable.ic_filter_empty)
    }
}
