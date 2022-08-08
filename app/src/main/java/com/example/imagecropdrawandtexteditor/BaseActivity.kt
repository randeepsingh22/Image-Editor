package com.example.imagecropdrawandtexteditor

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import com.example.imagecropdrawandtexteditor.BaseActivity
import android.app.ProgressDialog
import android.content.Context

open class BaseActivity : AppCompatActivity() {
    protected fun checkInitImageLoader() {

    }

    companion object {



        @JvmStatic
        fun getLoadingDialog(
            context: Context?, title: String?,
            canCancel: Boolean
        ): Dialog {
            val dialog = ProgressDialog(context)
            dialog.setCancelable(canCancel)
            dialog.setMessage(title)
            return dialog
        }
    }
} //end class
