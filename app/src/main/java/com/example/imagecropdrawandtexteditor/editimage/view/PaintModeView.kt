package com.xinlan.imageeditlibrary.editimage.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.annotation.RequiresApi
import android.os.Build
import android.util.AttributeSet
import android.view.View

/**
 * Created by panyi on 17/2/11.
 */
class PaintModeView : View {
    private var mPaint: Paint? = null
    var stokenColor = 0
        private set
    private var mStokeWidth = -1f
    private var mRadius = 0f

    constructor(context: Context?) : super(context) {
        initView(context)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initView(context)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context,
        attrs,
        defStyleAttr) {
        initView(context)
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        initView(context)
    }

    protected fun initView(context: Context?) {
        mPaint = Paint()
        mPaint!!.isAntiAlias = true
        mPaint!!.color = Color.RED

        //mStokeWidth = 10;
        //mStokeColor = Color.RED;
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mPaint!!.color = stokenColor
        mRadius = mStokeWidth / 2
        canvas.drawCircle((width shr 1).toFloat(), (height shr 1).toFloat(), mRadius, mPaint!!)
    }

    fun setPaintStrokeColor(newColor: Int) {
        stokenColor = newColor
        this.invalidate()
    }

    fun setPaintStrokeWidth(width: Float) {
        mStokeWidth = width
        this.invalidate()
    }

    val stokenWidth: Float
        get() {
            if (mStokeWidth < 0) {
                mStokeWidth = measuredHeight.toFloat()
            }
            return mStokeWidth
        }
} //end class
