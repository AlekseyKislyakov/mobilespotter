package com.example.mobile_spotter.utils

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HolderDecorator(private val space: Int) :
    RecyclerView.ItemDecoration() {

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
            if (position == 0) {
                outRect?.top = space
            }
            outRect?.right = space
        } else {
            var allSpanSize = 0
            for (i: Int in IntRange(0, position)) {
                allSpanSize += grid.spanSizeLookup.getSpanSize(i)
                if(position <= spanCount - 1){
                    outRect?.top = space
                }
            }
            val currentModuloResult = allSpanSize % spanCount
            if (currentModuloResult == 0) {

                outRect?.right = space
            }
        }
        outRect?.left = space
        outRect?.bottom = space


    }
}
