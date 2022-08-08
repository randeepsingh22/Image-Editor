package com.example.imagecropdrawandtexteditor.editimage.fragment

import com.example.imagecropdrawandtexteditor.editimage.EditImageActivity
import android.os.Bundle
import androidx.fragment.app.Fragment

abstract class BaseEditFragment : Fragment() {
    @JvmField
    protected var activity: EditImageActivity? = null
    protected fun ensureEditActivity(): EditImageActivity? {
        if (activity == null) {
            activity = getActivity() as EditImageActivity?
        }
        return activity
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        ensureEditActivity()
    }

    abstract fun onShow()
    abstract fun backToMain()
} //end class
