package com.example.imagecropdrawandtexteditor.editimage.fragment

import android.content.Context
import com.example.imagecropdrawandtexteditor.editimage.fragment.BaseEditFragment
import android.text.TextWatcher
import android.widget.EditText
import com.example.imagecropdrawandtexteditor.editimage.view.TextStickerView
import android.widget.CheckBox
import com.example.imagecropdrawandtexteditor.editimage.ui.ColorPicker
import com.example.imagecropdrawandtexteditor.editimage.fragment.AddTextFragment.SaveTextStickerTask
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.imagecropdrawandtexteditor.R
import com.example.imagecropdrawandtexteditor.editimage.fragment.AddTextFragment.SelectColorBtnClick
import android.text.Editable
import com.example.imagecropdrawandtexteditor.editimage.EditImageActivity
import com.example.imagecropdrawandtexteditor.editimage.fragment.MainMenuFragment
import com.example.imagecropdrawandtexteditor.editimage.task.StickerTask
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageView
import com.example.imagecropdrawandtexteditor.editimage.ModuleConfig

@Suppress("DEPRECATION")
class AddTextFragment : BaseEditFragment(), TextWatcher {
    private var mainView: View? = null
    private var backToMenu // 返回主菜单
            : View? = null
    private var mInputText //输入框
            : EditText? = null
    private var mTextColorSelector //颜色选择器
            : ImageView? = null
    private var mTextStickerView // 文字贴图显示控件
            : TextStickerView? = null
    private var mAutoNewLineCheck: CheckBox? = null
    private var mColorPicker: ColorPicker? = null
    private var mTextColor = Color.WHITE
    private var imm: InputMethodManager? = null
    private var mSaveTask: SaveTextStickerTask? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        imm = getActivity()!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        mainView = inflater.inflate(R.layout.fragment_edit_image_add_text, null)
        return mainView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mTextStickerView =
            getActivity()!!.findViewById<View>(R.id.text_sticker_panel) as TextStickerView
        backToMenu = mainView!!.findViewById(R.id.back_to_main)
        mInputText = mainView!!.findViewById<View>(R.id.text_input) as EditText
        mTextColorSelector = mainView!!.findViewById<View>(R.id.text_color) as ImageView
        mAutoNewLineCheck = mainView!!.findViewById<View>(R.id.check_auto_newline) as CheckBox
        backToMenu!!.setOnClickListener(BackToMenuClick())
        mColorPicker = ColorPicker(getActivity(), 255, 0, 0)
        mTextColorSelector!!.setOnClickListener(SelectColorBtnClick())
        mInputText!!.addTextChangedListener(this)
        mTextStickerView!!.setEditText(mInputText)

        //统一颜色设置
        mTextColorSelector!!.setBackgroundColor(mColorPicker!!.color)
        mTextStickerView!!.setTextColor(mColorPicker!!.color)
    }

    override fun afterTextChanged(s: Editable) {
        //mTextStickerView change
        val text = s.toString().trim { it <= ' ' }
        mTextStickerView!!.setText(text)
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}


    private inner class SelectColorBtnClick : View.OnClickListener {
        override fun onClick(v: View) {
            mColorPicker!!.show()
            val okColor = mColorPicker!!.findViewById<View>(R.id.okColorButton) as Button
            okColor.setOnClickListener {
                changeTextColor(mColorPicker!!.color)
                mColorPicker!!.dismiss()
            }
        }
    } //end inner class


    private fun changeTextColor(newColor: Int) {
        mTextColor = newColor
        mTextColorSelector!!.setBackgroundColor(mTextColor)
        mTextStickerView!!.setTextColor(mTextColor)
    }

    fun hideInput() {
        if (getActivity() != null && getActivity()!!.currentFocus != null && isInputMethodShow) {
            imm!!.hideSoftInputFromWindow(getActivity()!!.currentFocus!!.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }

    val isInputMethodShow: Boolean
        get() = imm!!.isActive

    private inner class BackToMenuClick : View.OnClickListener {
        override fun onClick(v: View) {
            backToMain()
        }
    } // end class


    override fun backToMain() {
        hideInput()
        activity!!.mode = EditImageActivity.MODE_NONE
        activity!!.bottomGallery!!.currentItem = MainMenuFragment.INDEX
        activity!!.mainImage!!.visibility = View.VISIBLE
        activity!!.bannerFlipper!!.showPrevious()
        mTextStickerView!!.visibility = View.GONE
    }

    override fun onShow() {
        activity!!.mode = EditImageActivity.MODE_TEXT
        activity!!.mainImage!!.setImageBitmap(activity!!.mainBit)
        activity!!.bannerFlipper!!.showNext()
        mTextStickerView!!.visibility = View.VISIBLE
        mInputText!!.clearFocus()
    }

    /**
     * 保存贴图图片
     */
    fun applyTextImage() {
        if (mSaveTask != null) {
            mSaveTask!!.cancel(true)
        }

        //启动任务
        mSaveTask = SaveTextStickerTask(activity)
        mSaveTask!!.execute(activity!!.mainBit)
    }

    /**
     * 文字合成任务
     * 合成最终图片
     */
    private inner class SaveTextStickerTask(activity: EditImageActivity?) : StickerTask(activity) {
        override fun handleImage(canvas: Canvas, m: Matrix) {
            val f = FloatArray(9)
            m.getValues(f)
            val dx = f[Matrix.MTRANS_X].toInt()
            val dy = f[Matrix.MTRANS_Y].toInt()
            val scale_x = f[Matrix.MSCALE_X]
            val scale_y = f[Matrix.MSCALE_Y]
            canvas.save()
            canvas.translate(dx.toFloat(), dy.toFloat())
            canvas.scale(scale_x, scale_y)
            //System.out.println("scale = " + scale_x + "       " + scale_y + "     " + dx + "    " + dy);
            mTextStickerView!!.drawText(canvas,
                mTextStickerView!!.layout_x,
                mTextStickerView!!.layout_y,
                mTextStickerView!!.scale,
                mTextStickerView!!.rotateAngle)
            canvas.restore()
        }

        override fun onPostResult(result: Bitmap) {
            mTextStickerView!!.clearTextContent()
            mTextStickerView!!.resetView()
            activity!!.changeMainBitmap(result, true)
            backToMain()
        }
    } //end inner class

    override fun onDestroy() {
        super.onDestroy()
        if (mSaveTask != null && !mSaveTask!!.isCancelled) {
            mSaveTask!!.cancel(true)
        }
    }

    companion object {
        const val INDEX = ModuleConfig.INDEX_ADDTEXT
        val TAG = AddTextFragment::class.java.name
        @JvmStatic
        fun newInstance(): AddTextFragment {
            return AddTextFragment()
        }
    }
} // end class
