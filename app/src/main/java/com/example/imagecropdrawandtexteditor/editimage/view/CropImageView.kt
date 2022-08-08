package com.example.imagecropdrawandtexteditor.editimage.view

import android.content.Context
import android.graphics.*
import com.example.imagecropdrawandtexteditor.editimage.view.CropImageView
import com.example.imagecropdrawandtexteditor.editimage.utils.PaintUtil
import android.util.AttributeSet
import com.example.imagecropdrawandtexteditor.R
import android.view.MotionEvent
import android.view.View

/**
 * 剪切图片
 *
 * @author 潘易
 */
class CropImageView : View {
    private val CIRCLE_WIDTH = 46
    private var mContext: Context? = null
    private var oldx = 0f
    private var oldy = 0f
    private var status = STATUS_IDLE
    private var selectedControllerCicle = 0
    private val backUpRect = RectF() // 上
    private val backLeftRect = RectF() // 左
    private val backRightRect = RectF() // 右
    private val backDownRect = RectF() // 下
    private val cropRect = RectF() // 剪切矩形
    private var mBackgroundPaint // 背景Paint
            : Paint? = null
    private var circleBit: Bitmap? = null
    private val circleRect = Rect()
    private var leftTopCircleRect: RectF? = null
    private var rightTopCircleRect: RectF? = null
    private var leftBottomRect: RectF? = null
    private var rightBottomRect: RectF? = null
    private val imageRect = RectF() // 存贮图片位置信息
    private val tempRect = RectF() // 临时存贮矩形数据
    var ratio = -1f // 剪裁缩放比率

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context,
        attrs,
        defStyleAttr) {
        init(context)
    }

    private fun init(context: Context) {
        mContext = context
        mBackgroundPaint = PaintUtil.newBackgroundPaint(context)
        circleBit = BitmapFactory.decodeResource(context.resources,
            R.drawable.sticker_rotate)
        circleRect[0, 0, circleBit!!.getWidth()] = circleBit!!.getHeight()
        leftTopCircleRect = RectF(0f, 0f, CIRCLE_WIDTH.toFloat(), CIRCLE_WIDTH.toFloat())
        rightTopCircleRect = RectF(leftTopCircleRect)
        leftBottomRect = RectF(leftTopCircleRect)
        rightBottomRect = RectF(leftTopCircleRect)
    }

    /**
     * 重置剪裁面
     *
     * @param rect
     */
    fun setCropRect(rect: RectF?) {
        if (rect == null) return
        imageRect.set(rect)
        cropRect.set(rect)
        scaleRect(cropRect, 0.5f)
        invalidate()
    }

    fun setRatioCropRect(rect: RectF, r: Float) {
        ratio = r
        if (r < 0) {
            setCropRect(rect)
            return
        }
        imageRect.set(rect)
        cropRect.set(rect)
        // setCropRect(rect);
        // 调整Rect
        val h: Float
        val w: Float
        if (cropRect.width() >= cropRect.height()) { // w>=h
            h = cropRect.height() / 2
            w = ratio * h
        } else { // w<h
            w = rect.width() / 2
            h = w / ratio
        } // end if
        val scaleX = w / cropRect.width()
        val scaleY = h / cropRect.height()
        scaleRect(cropRect, scaleX, scaleY)
        invalidate()
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        val w = width
        val h = height
        if (w <= 0 || h <= 0) return

        // 绘制黑色背景
        backUpRect[0f, 0f, w.toFloat()] = cropRect.top
        backLeftRect[0f, cropRect.top, cropRect.left] = cropRect.bottom
        backRightRect[cropRect.right, cropRect.top, w.toFloat()] = cropRect.bottom
        backDownRect[0f, cropRect.bottom, w.toFloat()] = h.toFloat()
        canvas.drawRect(backUpRect, mBackgroundPaint!!)
        canvas.drawRect(backLeftRect, mBackgroundPaint!!)
        canvas.drawRect(backRightRect, mBackgroundPaint!!)
        canvas.drawRect(backDownRect, mBackgroundPaint!!)

        // 绘制四个控制点
        val radius = CIRCLE_WIDTH shr 1
        leftTopCircleRect!![cropRect.left - radius, cropRect.top - radius, cropRect.left + radius] =
            cropRect.top + radius
        rightTopCircleRect!![cropRect.right - radius, cropRect.top - radius, cropRect.right + radius] =
            cropRect.top + radius
        leftBottomRect!![cropRect.left - radius, cropRect.bottom - radius, cropRect.left + radius] =
            cropRect.bottom + radius
        rightBottomRect!![cropRect.right - radius, cropRect.bottom - radius, cropRect.right + radius] =
            cropRect.bottom + radius
        canvas.drawBitmap(circleBit!!, circleRect, leftTopCircleRect!!, null)
        canvas.drawBitmap(circleBit!!, circleRect, rightTopCircleRect!!, null)
        canvas.drawBitmap(circleBit!!, circleRect, leftBottomRect!!, null)
        canvas.drawBitmap(circleBit!!, circleRect, rightBottomRect!!, null)
    }

    /**
     * 触摸事件处理
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        var ret = super.onTouchEvent(event) // 是否向下传递事件标志 true为消耗
        val action = event.action
        val x = event.x
        val y = event.y
        when (action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                val selectCircle = isSeletedControllerCircle(x, y)
                if (selectCircle > 0) { // 选择控制点
                    ret = true
                    selectedControllerCicle = selectCircle // 记录选中控制点编号
                    status = STATUS_SCALE // 进入缩放状态
                } else if (cropRect.contains(x, y)) { // 选择缩放框内部
                    ret = true
                    status = STATUS_MOVE // 进入移动状态
                } else { // 没有选择
                } // end if
            }
            MotionEvent.ACTION_MOVE -> if (status == STATUS_SCALE) { // 缩放控制
                // System.out.println("缩放控制");
                scaleCropController(x, y)
            } else if (status == STATUS_MOVE) { // 移动控制
                // System.out.println("移动控制");
                translateCrop(x - oldx, y - oldy)
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> status = STATUS_IDLE // 回归空闲状态
        }

        // 记录上一次动作点
        oldx = x
        oldy = y
        return ret
    }

    /**
     * 移动剪切框
     *
     * @param dx
     * @param dy
     */
    private fun translateCrop(dx: Float, dy: Float) {
        tempRect.set(cropRect) // 存贮原有数据，以便还原
        translateRect(cropRect, dx, dy)
        // 边界判定算法优化
        val mdLeft = imageRect.left - cropRect.left
        if (mdLeft > 0) {
            translateRect(cropRect, mdLeft, 0f)
        }
        val mdRight = imageRect.right - cropRect.right
        if (mdRight < 0) {
            translateRect(cropRect, mdRight, 0f)
        }
        val mdTop = imageRect.top - cropRect.top
        if (mdTop > 0) {
            translateRect(cropRect, 0f, mdTop)
        }
        val mdBottom = imageRect.bottom - cropRect.bottom
        if (mdBottom < 0) {
            translateRect(cropRect, 0f, mdBottom)
        }
        this.invalidate()
    }

    /**
     * 操作控制点 控制缩放
     *
     * @param x
     * @param y
     */
    private fun scaleCropController(x: Float, y: Float) {
        tempRect.set(cropRect) // 存贮原有数据，以便还原
        when (selectedControllerCicle) {
            1 -> {
                cropRect.left = x
                cropRect.top = y
            }
            2 -> {
                cropRect.right = x
                cropRect.top = y
            }
            3 -> {
                cropRect.left = x
                cropRect.bottom = y
            }
            4 -> {
                cropRect.right = x
                cropRect.bottom = y
            }
        }
        if (ratio < 0) { // 任意缩放比
            // 边界条件检测
            validateCropRect()
            invalidate()
        } else {
            // 更新剪切矩形长宽
            // 确定不变点
            when (selectedControllerCicle) {
                1, 2 -> cropRect.top = (cropRect.bottom
                        - (cropRect.right - cropRect.left) / ratio)
                3, 4 -> cropRect.bottom = ((cropRect.right - cropRect.left) / ratio
                        + cropRect.top)
            }

            // validateCropRect();
            if (cropRect.left < imageRect.left || cropRect.right > imageRect.right || cropRect.top < imageRect.top || cropRect.bottom > imageRect.bottom || cropRect.width() < CIRCLE_WIDTH || cropRect.height() < CIRCLE_WIDTH) {
                cropRect.set(tempRect)
            }
            invalidate()
        } // end if
    }

    /**
     * 边界条件检测
     *
     */
    private fun validateCropRect() {
        if (cropRect.width() < CIRCLE_WIDTH) {
            cropRect.left = tempRect.left
            cropRect.right = tempRect.right
        }
        if (cropRect.height() < CIRCLE_WIDTH) {
            cropRect.top = tempRect.top
            cropRect.bottom = tempRect.bottom
        }
        if (cropRect.left < imageRect.left) {
            cropRect.left = imageRect.left
        }
        if (cropRect.right > imageRect.right) {
            cropRect.right = imageRect.right
        }
        if (cropRect.top < imageRect.top) {
            cropRect.top = imageRect.top
        }
        if (cropRect.bottom > imageRect.bottom) {
            cropRect.bottom = imageRect.bottom
        }
    }

    /**
     * 是否选中控制点
     *
     * -1为没有
     *
     * @param x
     * @param y
     * @return
     */
    private fun isSeletedControllerCircle(x: Float, y: Float): Int {
        if (leftTopCircleRect!!.contains(x, y)) // 选中左上角
            return 1
        if (rightTopCircleRect!!.contains(x, y)) // 选中右上角
            return 2
        if (leftBottomRect!!.contains(x, y)) // 选中左下角
            return 3
        return if (rightBottomRect!!.contains(x, y)) 4 else -1
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    /**
     * 返回剪切矩形
     *
     * @return
     */
    fun getCropRect(): RectF {
        return RectF(cropRect)
    }

    companion object {
        private const val STATUS_IDLE = 1 // 空闲状态
        private const val STATUS_MOVE = 2 // 移动状态
        private const val STATUS_SCALE = 3 // 缩放状态

        /**
         * 移动矩形
         *
         * @param rect
         * @param dx
         * @param dy
         */
        private fun translateRect(rect: RectF, dx: Float, dy: Float) {
            rect.left += dx
            rect.right += dx
            rect.top += dy
            rect.bottom += dy
        }

        /**
         * 缩放指定矩形
         *
         * @param rect
         */
        private fun scaleRect(rect: RectF, scaleX: Float, scaleY: Float) {
            val w = rect.width()
            val h = rect.height()
            val newW = scaleX * w
            val newH = scaleY * h
            val dx = (newW - w) / 2
            val dy = (newH - h) / 2
            rect.left -= dx
            rect.top -= dy
            rect.right += dx
            rect.bottom += dy
        }

        /**
         * 缩放指定矩形
         *
         * @param rect
         * @param scale
         */
        private fun scaleRect(rect: RectF, scale: Float) {
            scaleRect(rect, scale, scale)
        }
    }
} // end class
