package com.example.imagecropdrawandtexteditor.editimage.view

import android.content.Context
import android.util.AttributeSet
import androidx.viewpager.widget.ViewPager

/**
 * 禁用ViewPager滑动事件
 *
 * @author panyi
 */
class CustomViewPager : ViewPager {
    private var isCanScroll = false

    constructor(context: Context?) : super(context!!) {}

    override fun setCurrentItem(item: Int, smoothScroll: Boolean) {
        isCanScroll = true
        super.setCurrentItem(item, smoothScroll)
        isCanScroll = false
    }

    override fun setCurrentItem(item: Int) {
        setCurrentItem(item, false)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs) {
    }

    fun setScanScroll(isCanScroll: Boolean) {
        this.isCanScroll = isCanScroll
    }

    override fun scrollTo(x: Int, y: Int) {
        if (isCanScroll) {
            super.scrollTo(x, y)
        }
    }
} // end class
