package com.example.imagecropdrawandtexteditor.editimage.view

import android.content.Context
import android.graphics.*
import android.text.TextPaint
import com.example.imagecropdrawandtexteditor.editimage.view.TextStickerView
import android.widget.EditText
import com.example.imagecropdrawandtexteditor.R
import android.text.TextUtils
import com.example.imagecropdrawandtexteditor.editimage.utils.RectUtil
import com.example.imagecropdrawandtexteditor.editimage.utils.ListUtil
import android.graphics.Paint.FontMetricsInt
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.util.ArrayList

/**
 * 文本贴图处理控件
 *
 *
 * Created by panyi on 2016/6/9.
 */
class TextStickerView : View {
    //public static final int CHAR_MIN_HEIGHT = 60;
    //private String mText;
    private val mPaint = TextPaint()
    private val debugPaint = Paint()
    private val mHelpPaint = Paint()
    private val mTextRect = Rect() // warp text rect record
    private val mHelpBoxRect = RectF()
    private val mDeleteRect = Rect() //删除按钮位置
    private val mRotateRect = Rect() //旋转按钮位置
    private var mDeleteDstRect = RectF()
    private var mRotateDstRect = RectF()
    private var mDeleteBitmap: Bitmap? = null
    private var mRotateBitmap: Bitmap? = null
    private var mCurrentMode = IDLE_MODE
    private var mEditText //输入控件
            : EditText? = null
    var layout_x = 0
    var layout_y = 0
    private var last_x = 0f
    private var last_y = 0f
    var rotateAngle = 0f
    var scale = 1f
    private var isInitLayout = true
    private var isShowHelpBox = true
    var isAutoNewLine = false //是否需要自动换行
        private set
    private val mTextContents: MutableList<String?> = ArrayList(2) //存放所写的文字内容
    private var mText: String? = null
    private val mPoint = Point(0, 0)

    constructor(context: Context) : super(context) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context,
        attrs,
        defStyleAttr) {
        initView(context)
    }

    fun setEditText(textView: EditText?) {
        mEditText = textView
    }

    private fun initView(context: Context) {
        debugPaint.color = Color.parseColor("#66ff0000")
        mDeleteBitmap = BitmapFactory.decodeResource(context.resources,
            R.drawable.sticker_delete)
        mRotateBitmap = BitmapFactory.decodeResource(context.resources,
            R.drawable.sticker_rotate)
        mDeleteRect[0, 0, mDeleteBitmap!!.getWidth()] = mDeleteBitmap!!.getHeight()
        mRotateRect[0, 0, mRotateBitmap!!.getWidth()] = mRotateBitmap!!.getHeight()
        mDeleteDstRect = RectF(0f,
            0f,
            (Constants.STICKER_BTN_HALF_SIZE shl 1).toFloat(),
            (Constants.STICKER_BTN_HALF_SIZE shl 1).toFloat())
        mRotateDstRect = RectF(0f,
            0f,
            (Constants.STICKER_BTN_HALF_SIZE shl 1).toFloat(),
            (Constants.STICKER_BTN_HALF_SIZE shl 1).toFloat())
        mPaint.color = Color.WHITE
        mPaint.textAlign = Paint.Align.CENTER
        mPaint.textSize = TEXT_SIZE_DEFAULT
        mPaint.isAntiAlias = true
        mPaint.textAlign = Paint.Align.LEFT
        mHelpPaint.color = Color.BLACK
        mHelpPaint.style = Paint.Style.STROKE
        mHelpPaint.isAntiAlias = true
        mHelpPaint.strokeWidth = 4f
    }

    fun setText(text: String?) {
        mText = text
        invalidate()
    }

    fun setTextColor(newColor: Int) {
        mPaint.color = newColor
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (isInitLayout) {
            isInitLayout = false
            resetView()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (TextUtils.isEmpty(mText)) return
        parseText()
        drawContent(canvas)
    }

    protected fun parseText() {
        if (TextUtils.isEmpty(mText)) return
        mTextContents.clear()
        val splits = mText!!.split("\n").toTypedArray()
        for (text in splits) {
            mTextContents.add(text)
        } //end for each
    }

    private fun drawContent(canvas: Canvas) {
        drawText(canvas)

        //draw x and rotate button
        val offsetValue = mDeleteDstRect.width().toInt() shr 1
        mDeleteDstRect.offsetTo(mHelpBoxRect.left - offsetValue, mHelpBoxRect.top - offsetValue)
        mRotateDstRect.offsetTo(mHelpBoxRect.right - offsetValue, mHelpBoxRect.bottom - offsetValue)
        RectUtil.rotateRect(mDeleteDstRect, mHelpBoxRect.centerX(),
            mHelpBoxRect.centerY(), rotateAngle)
        RectUtil.rotateRect(mRotateDstRect, mHelpBoxRect.centerX(),
            mHelpBoxRect.centerY(), rotateAngle)
        if (!isShowHelpBox) {
            return
        }
        canvas.save()
        canvas.rotate(rotateAngle, mHelpBoxRect.centerX(), mHelpBoxRect.centerY())
        canvas.drawRoundRect(mHelpBoxRect, 10f, 10f, mHelpPaint)
        canvas.restore()
        canvas.drawBitmap(mDeleteBitmap!!, mDeleteRect, mDeleteDstRect, null)
        canvas.drawBitmap(mRotateBitmap!!, mRotateRect, mRotateDstRect, null)

        //debug
//        canvas.drawRect(mRotateDstRect, debugPaint);
//        canvas.drawRect(mDeleteDstRect, debugPaint);
//        canvas.drawRect(mHelpBoxRect, debugPaint);
    }

    private fun drawText(canvas: Canvas) {
        drawText(canvas, layout_x, layout_y, scale, rotateAngle)
    }

    fun drawText(canvas: Canvas, _x: Int, _y: Int, scale: Float, rotate: Float) {
        if (ListUtil.isEmpty(mTextContents)) return
        var text_height = 0
        mTextRect[0, 0, 0] = 0 //clear
        val tempRect = Rect()
        val fontMetrics = mPaint.fontMetricsInt
        val charMinHeight = Math.abs(fontMetrics.top) + Math.abs(fontMetrics.bottom) //字体高度
        text_height = charMinHeight
        //System.out.println("top = "+fontMetrics.top +"   bottom = "+fontMetrics.bottom);
        for (i in mTextContents.indices) {
            val text = mTextContents[i]
            mPaint.getTextBounds(text, 0, text!!.length, tempRect)
            //System.out.println(i + " ---> " + tempRect.height());
            //text_height = Math.max(charMinHeight, tempRect.height());
            if (tempRect.height() <= 0) { //处理此行文字为空的情况
                tempRect[0, 0, 0] = text_height
            }
            RectUtil.rectAddV(mTextRect, tempRect, 0, charMinHeight)
        } //end for i
        mTextRect.offset(_x, _y)
        mHelpBoxRect[(mTextRect.left - PADDING).toFloat(), (mTextRect.top - PADDING
                ).toFloat(), (mTextRect.right + PADDING).toFloat()] =
            (mTextRect.bottom + PADDING).toFloat()
        RectUtil.scaleRect(mHelpBoxRect, scale)
        canvas.save()
        canvas.scale(scale, scale, mHelpBoxRect.centerX(), mHelpBoxRect.centerY())
        canvas.rotate(rotate, mHelpBoxRect.centerX(), mHelpBoxRect.centerY())

        //canvas.drawRect(mTextRect, debugPaint);
        //float left = mHelpBoxRect.left - mTextRect.left;
        //float right = mHelpBoxRect.right - mTextRect.right;

        //System.out.println("left = "+left +"   right = "+right);
        var draw_text_y = _y + (text_height shr 1) + PADDING
        for (i in mTextContents.indices) {
            canvas.drawText(mTextContents[i]!!, _x.toFloat(), draw_text_y.toFloat(), mPaint)
            draw_text_y += text_height
        } //end for i
        canvas.restore()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        var ret = super.onTouchEvent(event) // 是否向下传递事件标志 true为消耗
        val action = event.action
        val x = event.x
        val y = event.y
        when (action) {
            MotionEvent.ACTION_DOWN -> {
                if (mDeleteDstRect.contains(x, y)) { // 删除模式
                    isShowHelpBox = true
                    mCurrentMode = DELETE_MODE
                } else if (mRotateDstRect.contains(x, y)) { // 旋转按钮
                    isShowHelpBox = true
                    mCurrentMode = ROTATE_MODE
                    last_x = mRotateDstRect.centerX()
                    last_y = mRotateDstRect.centerY()
                    ret = true
                } else if (detectInHelpBox(x, y)) { // 移动模式
                    isShowHelpBox = true
                    mCurrentMode = MOVE_MODE
                    last_x = x
                    last_y = y
                    ret = true
                } else {
                    isShowHelpBox = false
                    invalidate()
                } // end if
                if (mCurrentMode == DELETE_MODE) { // 删除选定贴图
                    mCurrentMode = IDLE_MODE // 返回空闲状态
                    clearTextContent()
                    invalidate()
                } // end if
            }
            MotionEvent.ACTION_MOVE -> {
                ret = true
                if (mCurrentMode == MOVE_MODE) { // 移动贴图
                    mCurrentMode = MOVE_MODE
                    val dx = x - last_x
                    val dy = y - last_y
                    layout_x += dx.toInt()
                    layout_y += dy.toInt()
                    invalidate()
                    last_x = x
                    last_y = y
                } else if (mCurrentMode == ROTATE_MODE) { // 旋转 缩放文字操作
                    mCurrentMode = ROTATE_MODE
                    val dx = x - last_x
                    val dy = y - last_y
                    updateRotateAndScale(dx, dy)
                    invalidate()
                    last_x = x
                    last_y = y
                }
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                ret = false
                mCurrentMode = IDLE_MODE
            }
        }
        return ret
    }

    /**
     * 考虑旋转情况下 点击点是否在内容矩形内
     *
     * @param x
     * @param y
     * @return
     */
    private fun detectInHelpBox(x: Float, y: Float): Boolean {
        //mRotateAngle
        mPoint[x.toInt()] = y.toInt()
        //旋转点击点
        RectUtil.rotatePoint(mPoint, mHelpBoxRect.centerX(), mHelpBoxRect.centerY(), -rotateAngle)
        return mHelpBoxRect.contains(mPoint.x.toFloat(), mPoint.y.toFloat())
    }

    fun clearTextContent() {
        if (mEditText != null) {
            mEditText!!.text = null
        }
        //setText(null);
    }

    /**
     * 旋转 缩放 更新
     *
     * @param dx
     * @param dy
     */
    fun updateRotateAndScale(dx: Float, dy: Float) {
        val c_x = mHelpBoxRect.centerX()
        val c_y = mHelpBoxRect.centerY()
        val x = mRotateDstRect.centerX()
        val y = mRotateDstRect.centerY()
        val n_x = x + dx
        val n_y = y + dy
        val xa = x - c_x
        val ya = y - c_y
        val xb = n_x - c_x
        val yb = n_y - c_y
        val srcLen = Math.sqrt((xa * xa + ya * ya).toDouble()).toFloat()
        val curLen = Math.sqrt((xb * xb + yb * yb).toDouble()).toFloat()
        val scale = curLen / srcLen // 计算缩放比
        this.scale *= scale
        val newWidth = mHelpBoxRect.width() * this.scale
        if (newWidth < 70) {
            this.scale /= scale
            return
        }
        val cos = ((xa * xb + ya * yb) / (srcLen * curLen)).toDouble()
        if (cos > 1 || cos < -1) return
        var angle = Math.toDegrees(Math.acos(cos)).toFloat()
        val calMatrix = xa * yb - xb * ya // 行列式计算 确定转动方向
        val flag = if (calMatrix > 0) 1 else -1
        angle = flag * angle
        rotateAngle += angle
    }

    fun resetView() {
        layout_x = measuredWidth / 2
        layout_y = measuredHeight / 2
        rotateAngle = 0f
        scale = 1f
        mTextContents.clear()
    }

    fun setAutoNewline(isAuto: Boolean) {
        if (isAutoNewLine != isAuto) {
            isAutoNewLine = isAuto
            postInvalidate()
        }
    }

    companion object {
        const val TEXT_SIZE_DEFAULT = 80f
        const val PADDING = 32

        //public static final int PADDING = 0;
        const val TEXT_TOP_PADDING = 10

        //控件的几种模式
        private const val IDLE_MODE = 2 //正常
        private const val MOVE_MODE = 3 //移动模式
        private const val ROTATE_MODE = 4 //旋转模式
        private const val DELETE_MODE = 5 //删除模式
    }
} //end class
