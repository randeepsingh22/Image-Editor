package com.example.imagecropdrawandtexteditor.editimage.view.imagezoom.graphic

import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.Drawable
import com.example.imagecropdrawandtexteditor.editimage.view.imagezoom.graphic.IBitmapDrawable
import java.io.InputStream

class FastBitmapDrawable( var mBitmap: Bitmap) : Drawable(), IBitmapDrawable {
     var mPaint: Paint

    constructor(res: Resources?, `is`: InputStream?) : this(BitmapFactory.decodeStream(`is`)) {}

    override fun draw(canvas: Canvas) {
        canvas.drawBitmap(mBitmap, 0.0f, 0.0f, mPaint)
    }

    override fun getOpacity(): Int {
        return PixelFormat.TRANSLUCENT
    }

    override fun setAlpha(alpha: Int) {
        mPaint.alpha = alpha
    }

    override fun setColorFilter(cf: ColorFilter?) {
        mPaint.colorFilter = cf
    }

    override fun getIntrinsicWidth(): Int {
        return mBitmap.width
    }

    override fun getIntrinsicHeight(): Int {
        return mBitmap.height
    }

    override fun getMinimumWidth(): Int {
        return mBitmap.width
    }

    override fun getMinimumHeight(): Int {
        return mBitmap.height
    }

    fun setAntiAlias(value: Boolean) {
        mPaint.isAntiAlias = value
        invalidateSelf()
    }

    @JvmName("getBitmap1")
    fun getBitmap(): Bitmap {
        return mBitmap
    }

    init {
        mPaint = Paint()
        mPaint.isDither = true
        mPaint.isFilterBitmap = true
    }

    override val bitmap: Bitmap = mBitmap
}