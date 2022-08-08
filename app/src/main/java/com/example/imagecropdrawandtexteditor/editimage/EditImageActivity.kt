package com.example.imagecropdrawandtexteditor.editimage

import com.example.imagecropdrawandtexteditor.editimage.fragment.AddTextFragment.Companion.newInstance

import com.example.imagecropdrawandtexteditor.BaseActivity
import android.graphics.Bitmap
import com.example.imagecropdrawandtexteditor.editimage.view.imagezoom.ImageViewTouch
import android.widget.ViewFlipper
import com.example.imagecropdrawandtexteditor.editimage.view.CropImageView
import com.example.imagecropdrawandtexteditor.editimage.view.TextStickerView
import com.example.imagecropdrawandtexteditor.editimage.view.CustomPaintView
import com.example.imagecropdrawandtexteditor.editimage.view.CustomViewPager
import com.example.imagecropdrawandtexteditor.editimage.fragment.MainMenuFragment
import com.example.imagecropdrawandtexteditor.editimage.fragment.CropFragment
import com.example.imagecropdrawandtexteditor.editimage.fragment.AddTextFragment
import com.example.imagecropdrawandtexteditor.editimage.fragment.PaintFragment
import com.example.imagecropdrawandtexteditor.editimage.widget.RedoUndoController
import android.os.Bundle
import com.example.imagecropdrawandtexteditor.R

import androidx.fragment.app.FragmentPagerAdapter
import android.os.AsyncTask
import com.example.imagecropdrawandtexteditor.editimage.utils.BitmapUtils
import com.example.imagecropdrawandtexteditor.editimage.view.imagezoom.ImageViewTouchBase
import android.content.Intent
import com.example.imagecropdrawandtexteditor.editimage.utils.FileUtil
import android.app.Activity
import android.app.Dialog
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

class EditImageActivity : BaseActivity() {
    var filePath // 需要编辑图片路径
            : String? = null
    var saveFilePath // 生成的新图片路径
            : String? = null
    private var imageWidth = 0
    private var imageHeight // 展示图片控件 宽 高
            = 0
    private var mLoadImageTask: LoadImageTask? = null
    @JvmField
    var mode = MODE_NONE // 当前操作模式
    protected var mOpTimes = 0
    protected var isBeenSaved = false
    private var mContext: EditImageActivity? = null
    var mainBit // 底层显示Bitmap
            : Bitmap? = null
        private set
    @JvmField
    var mainImage: ImageViewTouch? = null
    private var backBtn: View? = null
    @JvmField
    var bannerFlipper: ViewFlipper? = null
    private var applyBtn: View? = null
    private var saveBtn: View? = null
    @JvmField
    var mCropPanel: CropImageView? = null
    var mTextStickerView: TextStickerView? = null
    var mPaintView: CustomPaintView? = null
    @JvmField
    var bottomGallery: CustomViewPager? = null
    private var mBottomGalleryAdapter: BottomGalleryAdapter? = null
    private var mMainMenuFragment: MainMenuFragment? = null
    @JvmField
    var mCropFragment: CropFragment? = null
    @JvmField
    var mAddTextFragment: AddTextFragment? = null
    @JvmField
    var mPaintFragment: PaintFragment? = null
    private var mSaveImageTask: SaveImageTask? = null
    private var mRedoUndoController: RedoUndoController? = null
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkInitImageLoader()
        supportActionBar!!.hide()
        setContentView(R.layout.activity_image_edit)
        initView()
        data
    }

    // 保存图片路径
    private val data: Unit
        private get() {
            filePath = intent.getStringExtra(FILE_PATH)
            saveFilePath = intent.getStringExtra(EXTRA_OUTPUT) // 保存图片路径
            loadImage(filePath)
        }

    private fun initView() {
        mContext = this
        val metrics = resources.displayMetrics
        imageWidth = metrics.widthPixels / 2
        imageHeight = metrics.heightPixels / 2
        bannerFlipper = findViewById<View>(R.id.banner_flipper) as ViewFlipper
        //        bannerFlipper.setInAnimation(this, com.google.android.material.R.anim.fragment_fade_enter);
//        bannerFlipper.setOutAnimation(this, com.google.android.material.R.anim.fragment_fade_enter);
        applyBtn = findViewById(R.id.apply)
        applyBtn!!.setOnClickListener(ApplyBtnClick())
        saveBtn = findViewById(R.id.save_btn)
        saveBtn!!.setOnClickListener(SaveBtnClick())
        mainImage = findViewById<View>(R.id.main_image) as ImageViewTouch
        backBtn = findViewById(R.id.back_btn) // 退出按钮
        backBtn!!.setOnClickListener(View.OnClickListener { onBackPressed() })
        mCropPanel = findViewById<View>(R.id.crop_panel) as CropImageView
        mTextStickerView = findViewById<View>(R.id.text_sticker_panel) as TextStickerView
        mPaintView = findViewById<View>(R.id.custom_paint_view) as CustomPaintView

        // 底部gallery
        bottomGallery = findViewById<View>(R.id.bottom_gallery) as CustomViewPager
        //bottomGallery.setOffscreenPageLimit(7);
        mMainMenuFragment = MainMenuFragment.newInstance()
        mBottomGalleryAdapter = BottomGalleryAdapter(
            this.supportFragmentManager)
        mCropFragment = CropFragment.newInstance()
        mAddTextFragment = newInstance()
        mPaintFragment = PaintFragment.newInstance()
        bottomGallery!!.adapter = mBottomGalleryAdapter
        mainImage!!.setFlingListener { e1, e2, velocityX, velocityY ->
            if (velocityY  > 1) {
                closeInputMethod()
            }
        }
        mRedoUndoController = RedoUndoController(this, findViewById(R.id.redo_uodo_panel))
    }


    private fun closeInputMethod() {
        if (mAddTextFragment!!.isAdded) {
            mAddTextFragment!!.hideInput()
        }
    }

    /**
     * @author panyi
     */
    private inner class BottomGalleryAdapter(fm: FragmentManager?) : FragmentPagerAdapter(
        fm!!) {
        override fun getItem(index: Int): Fragment {
            // System.out.println("createFragment-->"+index);
            when (index) {
                MainMenuFragment.INDEX -> return mMainMenuFragment!!
                CropFragment.INDEX -> return mCropFragment!!
                AddTextFragment.INDEX -> return mAddTextFragment!!
                PaintFragment.INDEX -> return mPaintFragment!! //绘制
            }
            return MainMenuFragment.newInstance()
        }

        override fun getCount(): Int {
            return 8
        }
    } // end inner class

    fun loadImage(filepath: String?) {
        if (mLoadImageTask != null) {
            mLoadImageTask!!.cancel(true)
        }
        mLoadImageTask = LoadImageTask()
        mLoadImageTask!!.execute(filepath)
    }

    private inner class LoadImageTask : AsyncTask<String?, Void?, Bitmap>() { // end inner class
        override fun doInBackground(vararg p0: String?): Bitmap {
            return BitmapUtils.getSampledBitmap(p0[0], imageWidth,
                imageHeight)
        }

        override fun onPostExecute(result: Bitmap) {
            changeMainBitmap(result, false)
        }


    }

    override fun onBackPressed() {
        when (mode) {
            MODE_CROP -> {
                mCropFragment!!.backToMain()
                return
            }
            MODE_TEXT -> {
                mAddTextFragment!!.backToMain()
                return
            }
            MODE_PAINT -> {
                mPaintFragment!!.backToMain()
                return
            }
        }
        if (canAutoExit()) {
            onSaveTaskDone()
        } else { //图片还未被保存    弹出提示框确认
            val alertDialogBuilder = AlertDialog.Builder(this)
            alertDialogBuilder.setMessage("Do you want to Exit without Saving?")
                .setCancelable(false)
                .setPositiveButton("Confirm") { dialog, id -> mContext!!.finish() }
                .setNegativeButton("Cancel") { dialog, id -> dialog.cancel() }
            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }
    }

    private inner class ApplyBtnClick : View.OnClickListener {
        override fun onClick(v: View) {
            when (mode) {
                MODE_CROP -> mCropFragment!!.applyCropImage()
                MODE_TEXT -> mAddTextFragment!!.applyTextImage()
                MODE_PAINT -> mPaintFragment!!.savePaintImage()
                else -> {}
            }
        }
    } // end inner class

    /**
     * 保存按钮 点击退出
     *
     * @author panyi
     */
    private inner class SaveBtnClick : View.OnClickListener {
        override fun onClick(v: View) {
            if (mOpTimes == 0) { //并未修改图片
                onSaveTaskDone()
            } else {
                doSaveImage()
            }
        }
    } // end inner class

    protected fun doSaveImage() {
        if (mOpTimes <= 0) return
        if (mSaveImageTask != null) {
            mSaveImageTask!!.cancel(true)
        }
        mSaveImageTask = SaveImageTask()
        mSaveImageTask!!.execute(mainBit)
    }

    /**
     * @param newBit
     * @param needPushUndoStack
     */
    fun changeMainBitmap(newBit: Bitmap?, needPushUndoStack: Boolean) {
        if (newBit == null) return
        if (mainBit == null || mainBit != newBit) {
            if (needPushUndoStack) {
                mRedoUndoController!!.switchMainBit(mainBit, newBit)
                increaseOpTimes()
            }
            mainBit = newBit
            mainImage!!.setImageBitmap(mainBit)
            mainImage!!.displayType = ImageViewTouchBase.DisplayType.FIT_TO_SCREEN
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mLoadImageTask != null) {
            mLoadImageTask!!.cancel(true)
        }
        if (mSaveImageTask != null) {
            mSaveImageTask!!.cancel(true)
        }
        if (mRedoUndoController != null) {
            mRedoUndoController!!.onDestroy()
        }
    }

    fun increaseOpTimes() {
        mOpTimes++
        isBeenSaved = false
    }

    fun resetOpTimes() {
        isBeenSaved = true
    }

    fun canAutoExit(): Boolean {
        return isBeenSaved || mOpTimes == 0
    }

    protected fun onSaveTaskDone() {
        val returnIntent = Intent()
        returnIntent.putExtra(FILE_PATH, filePath)
        returnIntent.putExtra(EXTRA_OUTPUT, saveFilePath)
        returnIntent.putExtra(IMAGE_IS_EDIT, mOpTimes > 0)
        FileUtil.ablumUpdate(this, saveFilePath)
        setResult(RESULT_OK, returnIntent)
        finish()
    }

    /**
     * 保存图像
     * 完成后退出
     */
    private inner class SaveImageTask : AsyncTask<Bitmap?, Void?, Boolean>() { //end inner class
        private var dialog: Dialog? = null
        override fun doInBackground(vararg params: Bitmap?): Boolean {
            return if (TextUtils.isEmpty(saveFilePath)) false else BitmapUtils.saveBitmap(params[0],
                saveFilePath)
        }

        override fun onCancelled() {
            super.onCancelled()
            dialog!!.dismiss()
        }

        override fun onCancelled(result: Boolean) {
            super.onCancelled(result)
            dialog!!.dismiss()
        }

        override fun onPreExecute() {
            super.onPreExecute()
            dialog = getLoadingDialog(mContext, "Saving", false)
            dialog!!.show()
        }

        override fun onPostExecute(result: Boolean) {
            super.onPostExecute(result)
            dialog!!.dismiss()
            if (result) {
                resetOpTimes()
                onSaveTaskDone()
            } else {
                Toast.makeText(mContext, "Error while Saving Image", Toast.LENGTH_SHORT).show()
            }
        }


    }

    companion object {
        const val FILE_PATH = "file_path"
        const val EXTRA_OUTPUT = "extra_output"
        const val SAVE_FILE_PATH = "save_file_path"
        const val IMAGE_IS_EDIT = "image_is_edit"
        const val MODE_NONE = 0
        const val MODE_STICKERS = 1 // 贴图模式
        const val MODE_FILTER = 2 // 滤镜模式
        const val MODE_CROP = 3 // 剪裁模式
        const val MODE_ROTATE = 4 // 旋转模式
        const val MODE_TEXT = 5 // 文字模式
        const val MODE_PAINT = 6 //绘制模式
        const val MODE_BEAUTY = 7 //美颜模式
        @JvmStatic
        fun start(
            context: Activity,
            editImagePath: String?,
            outputPath: String?,
            requestCode: Int
        ) {
            if (TextUtils.isEmpty(editImagePath)) {
//            Toast.makeText(context, R.string.no_choose, Toast.LENGTH_SHORT).show();
                return
            }
            val it = Intent(context, EditImageActivity::class.java)
            it.putExtra(FILE_PATH, editImagePath)
            it.putExtra(EXTRA_OUTPUT, outputPath)
            context.startActivityForResult(it, requestCode)
        }
    }
} // end class
