package com.example.imagecropdrawandtexteditor.editimage.widget


import com.example.imagecropdrawandtexteditor.editimage.EditImageActivity

import com.example.imagecropdrawandtexteditor.editimage.widget.EditCache.ListModify
import android.graphics.Bitmap
import android.view.View
import com.example.imagecropdrawandtexteditor.R

/**
 * Created by panyi on 2017/11/15.
 *
 *
 * 前一步 后一步操作类
 */
class RedoUndoController(private val mActivity: EditImageActivity, private val mRootView: View) :
    View.OnClickListener {
    private val mUndoBtn
            : View = mRootView.findViewById(R.id.uodo_btn)
    private val mRedoBtn
            : View
    private val mEditCache: EditCache? = EditCache() //保存前一次操作内容 用于撤销操作
    private val mObserver: ListModify = object : ListModify {
        override fun onCacheListChange(cache: EditCache?) {
            updateBtns()
        }
    }

    fun switchMainBit(mainBitmap: Bitmap?, newBit: Bitmap?) {
        if (mainBitmap == null || mainBitmap.isRecycled) return
        mEditCache!!.push(mainBitmap)
        mEditCache.push(newBit)
    }

    override fun onClick(v: View) {
        if (v === mUndoBtn) {
            undoClick()
        } else if (v === mRedoBtn) {
            redoClick()
        } //end if
    }

    /**
     * 撤销操作
     */
    protected fun undoClick() {
        //System.out.println("Undo!!!");
        val lastBitmap = mEditCache!!.nextCurrentBit
        if (lastBitmap != null && !lastBitmap.isRecycled) {
            mActivity.changeMainBitmap(lastBitmap, false)
        }
    }

    /**
     * 取消撤销
     */
    protected fun redoClick() {
        //System.out.println("Redo!!!");
        val preBitmap = mEditCache!!.preCurrentBit
        if (preBitmap != null && !preBitmap.isRecycled) {
            mActivity.changeMainBitmap(preBitmap, false)
        }
    }

    /**
     * 根据状态更新按钮显示
     */
    fun updateBtns() {
        //System.out.println("缓存Size = " + mEditCache.getSize() + "  current = " + mEditCache.getCur());
        //System.out.println("content = " + mEditCache.debugLog());
        mUndoBtn.visibility =
            if (mEditCache!!.checkNextBitExist()) View.VISIBLE else View.INVISIBLE
        mRedoBtn.visibility =
            if (mEditCache.checkPreBitExist()) View.VISIBLE else View.INVISIBLE
    }

    fun onDestroy() {
        if (mEditCache != null) {
            mEditCache.removeObserver(mObserver)
            mEditCache.removeAll()
        }
    }

    init {
        mRedoBtn = mRootView.findViewById(R.id.redo_btn)
        mUndoBtn.setOnClickListener(this)
        mRedoBtn.setOnClickListener(this)
        updateBtns()
        mEditCache!!.addObserver(mObserver)
    }
} //end class
