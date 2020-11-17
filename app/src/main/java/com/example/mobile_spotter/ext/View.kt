package com.example.mobile_spotter.ext

import android.view.View
import android.widget.ImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton

fun View.setMargins(left: Int = 0, top: Int = 0, right: Int = 0, bottom: Int = 0) {
    val params = this.layoutParams as ConstraintLayout.LayoutParams
    params.setMargins(left.px, top.px, right.px, bottom.px)
    this.layoutParams = params
}