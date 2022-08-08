package com.example.imagecropdrawandtexteditor.picchooser

import android.R
import com.example.imagecropdrawandtexteditor.BaseActivity
import android.os.Bundle

import android.content.Intent
import androidx.fragment.app.Fragment

class SelectPictureActivity : BaseActivity() {
     var selectedImagePathArray = ""
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)

        checkInitImageLoader()
        setResult(RESULT_CANCELED)
        val newFragment: Fragment = ImagesFragment()
        val transaction = supportFragmentManager
            .beginTransaction()
        transaction.replace(R.id.content, newFragment)
        transaction.commit()
    }

    fun imageSelected(imgPath: String, imgTaken: String, imageSize: Long) {
//        if(selectedImagePathArray.contains(imgPath)){
//            selectedImagePathArray.remove(imgPath)
//        }else{
//            selectedImagePathArray.add(imgPath)
//
//        }
//        println("SelectImageActivity() ${selectedImagePathArray.size} $selectedImagePathArray")
//        if(selectedImagePathArray.size == 1){
                    returnResult(imagePAth = imgPath, imageSize = imageSize, imageTaken = imgTaken)

//        }
    }

    private fun returnResult(imagePAth: String,imageTaken: String,imageSize: Long) {
        val result = Intent()
        result.putExtra("imgPath", imagePAth)
        result.putExtra("dateTaken", imageTaken)
        result.putExtra("imageSize", imageSize)
        setResult(RESULT_OK, result)
        finish()
    }
}