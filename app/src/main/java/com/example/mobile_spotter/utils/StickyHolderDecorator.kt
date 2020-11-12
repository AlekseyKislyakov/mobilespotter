package com.example.mobile_spotter.utils

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class StickyHolderDecorator(private val space: Int) :
    RecyclerView.ItemDecoration() {

    private var fullWidthItems = 0

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)

        addSpaceToView(outRect, parent.getChildAdapterPosition(view), parent)
    }

    private fun addSpaceToView(outRect: Rect?, position: Int?, parent: RecyclerView?) {
        if (position == null || parent == null)
            return

        val grid = parent.layoutManager as GridLayoutManager
        val spanSize = grid.spanSizeLookup.getSpanSize(position)
        val spanCount = grid.spanCount

        if (spanSize == spanCount) {
            outRect?.right = space
        } else {
            var allSpanSize = 0
            for (i: Int in IntRange(0, position)) {
                if(grid.spanSizeLookup.getSpanSize(i) == spanCount) {
                    allSpanSize = 0
                } else {
                    allSpanSize += grid.spanSizeLookup.getSpanSize(i)
                }
            }
            val currentModuloResult = (allSpanSize) % spanCount
            if (currentModuloResult == 0) {
                outRect?.right = space
            }
        }

        if (position == 0) {
            outRect?.top = space
        }
        outRect?.left = space
        outRect?.bottom = space


    }
}
