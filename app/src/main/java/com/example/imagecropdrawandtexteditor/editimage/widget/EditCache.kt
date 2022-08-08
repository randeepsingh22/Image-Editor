package com.example.imagecropdrawandtexteditor.editimage.widget

import kotlin.jvm.JvmOverloads
import com.example.imagecropdrawandtexteditor.editimage.widget.EditCache
import android.graphics.Bitmap
import com.example.imagecropdrawandtexteditor.editimage.widget.EditCache.ListModify
import java.util.*
import kotlin.jvm.Synchronized

/**
 * Created by panyi on 2017/11/15.
 *
 *
 * 编辑缓存  用于保存之前操作产生的位图
 */
class EditCache @JvmOverloads constructor(cacheSize: Int = EDIT_CACHE_SIZE) {
    val editCacheSize: Int
    private val mCacheList = LinkedList<Bitmap>()
    var cur = -1
        private set

    interface ListModify {
        fun onCacheListChange(cache: EditCache?)
    }

    private val mObserverList: MutableList<ListModify> = ArrayList(2)

    @get:Synchronized
    val size: Int
        get() = mCacheList.size

    fun debugLog(): String {
        val sb = StringBuffer()
        for (i in mCacheList.indices) {
            sb.append("{ " + mCacheList[i] + " }")
        }
        return sb.toString()
    }

    @Synchronized
    fun push(bitmap: Bitmap?): Boolean {
        if (bitmap == null || bitmap.isRecycled) return false
        while (!isPointToLastElem) {
            val dropBit = mCacheList.pollLast()
            freeBitmap(dropBit)
        } //end for each
        var allReadyHaveBitmap: Bitmap? = null
        for (b in mCacheList) {
            if (bitmap == b && !bitmap.isRecycled) {
                allReadyHaveBitmap = bitmap
                break
            }
        } //end for each
        if (allReadyHaveBitmap != null) {
            mCacheList.remove(allReadyHaveBitmap) //do swap
            mCacheList.addLast(allReadyHaveBitmap)
            trimCacheList()
        } else { // add new bitmap
            mCacheList.addLast(bitmap)
            trimCacheList()
        } //end if

        //指针指向最后一个元素
        cur = mCacheList.size - 1
        notifyListChange()
        return true
    }

    @get:Synchronized
    val nextCurrentBit: Bitmap?
        get() {
            cur--
            val ret = curBit
            notifyListChange()
            return ret
        }

    @get:Synchronized
    val preCurrentBit: Bitmap?
        get() {
            cur++
            val ret = curBit
            notifyListChange()
            return ret
        }

    /**
     * 可以撤销到前一步的操作
     * @return
     */
    fun checkNextBitExist(): Boolean {
        val point = cur - 1
        return point >= 0 && point < mCacheList.size
    }

    /**
     * 可取消撤销到后一操作
     * @return
     */
    fun checkPreBitExist(): Boolean {
        val point = cur + 1
        return point >= 0 && point < mCacheList.size
    }

    @Synchronized
    fun removeAll() {
        for (b in mCacheList) {
            freeBitmap(b)
        }
        mCacheList.clear()
        notifyListChange()
    }

    @get:Synchronized
    val isPointToLastElem: Boolean
        get() = cur == mCacheList.size - 1

    /**
     * 添加观察者
     *
     * @param observer
     */
    fun addObserver(observer: ListModify?) {
        if (observer != null && !mObserverList.contains(observer)) {
            mObserverList.add(observer)
        }
    }

    /**
     * 移出观察者
     *
     * @param observer
     */
    fun removeObserver(observer: ListModify?) {
        if (observer != null && mObserverList.contains(observer)) {
            mObserverList.remove(observer)
        }
    }

    protected fun notifyListChange() {
        for (observer in mObserverList) {
            observer.onCacheListChange(this)
        } //end for each
    }

    val curBit: Bitmap?
        get() {
            if (cur >= 0 && cur < mCacheList.size) {
                val bit = mCacheList[cur]
                if (bit != null && !bit.isRecycled) {
                    return bit
                }
            }
            return null
        }

    @Synchronized
    private fun trimCacheList() {
        while (mCacheList.size > editCacheSize) {
            val dropBit = mCacheList.pollFirst()
            freeBitmap(dropBit)
        } //end while
    }

    companion object {
        const val EDIT_CACHE_SIZE = 10
        fun freeBitmap(bit: Bitmap?) {
            if (bit != null && !bit.isRecycled) {
                bit.recycle()
            }
        }
    }

    init {
        var cacheSize = cacheSize
        if (cacheSize <= 0) {
            cacheSize = EDIT_CACHE_SIZE
        }
        editCacheSize = cacheSize
    }
} //end class
