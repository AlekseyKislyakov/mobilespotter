package com.example.mobile_spotter.ext

import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.ColorRes
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.activity_main.*
import com.example.mobile_spotter.MainActivity

fun <T> LifecycleOwner.observe(liveData: LiveData<T>, action: (T) -> Unit) {
    liveData.observe(this, Observer { result -> action.invoke(result) })
}

fun Fragment.getColor(@ColorRes color: Int): Int {
    return ResourcesCompat.getColor(resources, color, null)
}

fun Fragment.showKeyboard() {
    activity?.getSystemService(Context.INPUT_METHOD_SERVICE)?.let {
        (it as InputMethodManager).run {
            toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0)
        }
    }
}

fun Fragment.hideKeyboard(view: View) {
    activity?.getSystemService(Context.INPUT_METHOD_SERVICE)?.let {
        (it as InputMethodManager).run {
            hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}

interface KeyboardShowListener {

    fun onKeyboardHeightChanged(value: Int)

    fun Fragment.setKeyboardListener() {
        // this is used to have some extra space so view wont be stretched
        val safetyShift = 100
        // onGlobalLayoutListener is needed to check for layout change
        /*activity?.nav_host_fragment?.viewTreeObserver?.addOnGlobalLayoutListener {
            var r = Rect()
            // r will be populated with the coordinates of your view that area still visible.
            activity?.nav_host_fragment?.getWindowVisibleDisplayFrame(r)

            // check how much is the height of the bottom of screen (probably, keyboard)
            var heightDiff = activity?.nav_host_fragment?.rootView?.height?.minus((r.bottom - r.top))
            if (heightDiff != null) {
                // if more than 1/4 of the screen, its probably a keyboard...
                if (heightDiff > activity?.nav_host_fragment?.rootView?.height?.div(4) ?: 0) {
                    // and send callback with value
                    this@KeyboardShowListener.onKeyboardHeightChanged(r.bottom - safetyShift.dpToPx())
                } else {
                    this@KeyboardShowListener.onKeyboardHeightChanged(-1)
                }
            }
        }*/
    }
}