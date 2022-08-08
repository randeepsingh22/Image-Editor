package com.example.imagecropdrawandtexteditor

import android.Manifest
import com.example.imagecropdrawandtexteditor.FileUtils.genEditFile
import com.example.imagecropdrawandtexteditor.editimage.EditImageActivity.Companion.start
import androidx.appcompat.app.AppCompatActivity
import com.example.imagecropdrawandtexteditor.MainActivity
import android.graphics.Bitmap
import android.os.Bundle
import com.example.imagecropdrawandtexteditor.R
import android.os.StrictMode.VmPolicy
import android.os.StrictMode
import androidx.core.app.ActivityCompat
import android.util.DisplayMetrics
import android.os.Build
import android.content.pm.PackageManager
import kotlin.Throws
import android.os.Environment
import android.content.Intent
import android.provider.MediaStore
import android.widget.Toast
import com.example.imagecropdrawandtexteditor.editimage.EditImageActivity
import com.example.imagecropdrawandtexteditor.picchooser.SelectPictureActivity
import android.app.Activity
import android.os.AsyncTask
import com.example.imagecropdrawandtexteditor.editimage.utils.BitmapUtils
import android.annotation.TargetApi
import android.net.Uri
import android.util.Log
import android.view.View
import android.widget.ImageView
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), View.OnClickListener {
    var TAG = "MAinActivity()"
    private var context: MainActivity? = null
    private var imgView: ImageView? = null
    private var openAblum: View? = null
    private var editImage //
            : View? = null
    private var mainBitmap: Bitmap? = null
    private var imageWidth = 0
    private var imageHeight //
            = 0
    private var path: String? = null
    private var mTakenPhoto //拍摄照片用于编辑
            : View? = null
    private var photoURI: Uri? = null
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val builder = VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        ActivityCompat.requestPermissions(this, arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.MANAGE_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE
        ), 1
        )
        initView()
    }

    private fun initView() {
        context = this
        val metrics = resources.displayMetrics
        imageWidth = metrics.widthPixels
        imageHeight = metrics.heightPixels
        imgView = findViewById<View>(R.id.img) as ImageView
        openAblum = findViewById(R.id.select_ablum)
        editImage = findViewById(R.id.edit_image)
        openAblum!!.setOnClickListener(this)
        editImage!!.setOnClickListener(this)
        mTakenPhoto = findViewById(R.id.take_photo)
        mTakenPhoto!!.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.take_photo -> takePhotoClick()
            R.id.edit_image -> editImageClick()
            R.id.select_ablum -> selectFromAblum()
        }
    }

    /**
     * 拍摄照片
     */
    protected fun takePhotoClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestTakePhotoPermissions()
        } else {
            doTakePhoto()
        } //end if
    }

    /**
     * 请求拍照权限
     */
    private fun requestTakePhotoPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_PERMISSON_CAMERA)
            return
        }
        doTakePhoto()
    }

    /**
     * 拍摄照片
     */
    //    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
    //            new ActivityResultContracts.StartActivityForResult(),
    //            new ActivityResultCallback<ActivityResult>() {
    //                @Override
    //                public void onActivityResult(ActivityResult result) {
    //                    if (result.getResultCode() == Activity.RESULT_OK) {
    //                        // There are no request codes
    //                        Intent data = result.getData();
    //                        imgView.setImageURI(result.getData().getData());
    //                        Toast.makeText(context, "Result of Camera "+ data.getStringExtra("output"), Toast.LENGTH_SHORT).show();
    //                    }
    //                }
    //            });
    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",  /* suffix */
            storageDir /* directory */
        )

        // Save a file: path for use with ACTION_VIEW intents
        path = image.absolutePath
        return image
    }

    //    private void doTakePhoto() {
    //        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    //
    ////        .launch(takePictureIntent);
    //        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
    //        File photoFile = FileUtils.genEditFile();
    //            Toast.makeText(context, "File Craeted " +photoFile.getPath(), Toast.LENGTH_SHORT).show();
    ////            // Continue only if the File was successfully created
    //        if (photoFile != null) {
    //            Toast.makeText(context, "File Craeted 2 " +photoFile.getPath(), Toast.LENGTH_SHORT).show();
    //
    //            photoURI =FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".fileprovider", photoFile);;;
    //
    //path = photoURI.getPath();
    //            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
    //            startActivityForResult(takePictureIntent, TAKE_PHOTO_CODE);
    //            }
    ////
    //        }
    //    }
    private fun doTakePhoto() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            val photoFile = genEditFile()
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = Uri.fromFile(photoFile)
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, TAKE_PHOTO_CODE)
            }

            //startActivityForResult(takePictureIntent, TAKE_PHOTO_CODE);
        }
    }

    /**
     * 编辑选择的图片
     *
     * @author panyi
     */
    private fun editImageClick() {
        if (path == null) {
            Toast.makeText(context, "Please Select Image To Continue Editing", Toast.LENGTH_SHORT)
                .show()
            return
        }
        val outputFile = genEditFile()
        start(this, path, outputFile!!.absolutePath, ACTION_REQUEST_EDITIMAGE)
    }

    /**
     * 从相册选择编辑图片
     */
    private fun selectFromAblum() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            openAblumWithPermissionsCheck()
        } else {
            openAblum()
        } //end if
    }

    private fun openAblum() {

//        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//        intent.addCategory(Intent.CATEGORY_OPENABLE);
//        intent.setType("image/*");
//        startActivityForResult(Intent.createChooser(intent, "Select Picture"),SELECT_GALLERY_IMAGE_CODE);
//        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//        intent.addCategory(Intent.CATEGORY_OPENABLE);
//        intent.setType("image/*");
//        startActivityForResult(Intent.createChooser(intent, "Select Picture"),SELECT_GALLERY_IMAGE_CODE);
        this@MainActivity.startActivityForResult(Intent(
            this@MainActivity, SelectPictureActivity::class.java),
            SELECT_GALLERY_IMAGE_CODE)
    }

    private fun openAblumWithPermissionsCheck() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_PERMISSON_SORAGE)
            return
        }
        openAblum()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSON_SORAGE && grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            openAblum();
            return
        } //end if
        if (requestCode == REQUEST_PERMISSON_CAMERA && grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            doTakePhoto()
            return
        }
    }

    fun getPathFromURI(contentUri: Uri?): String? {
        var res: String? = null
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = contentResolver.query(contentUri!!, proj, null, null, null)
        if (cursor!!.moveToFirst()) {
            val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            res = cursor.getString(column_index)
        }
        cursor.close()
        Log.d(TAG, "2 $res")
        return res
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            // System.out.println("RESULT_OK");
            when (requestCode) {
                SELECT_GALLERY_IMAGE_CODE -> {
                    val selectedImageUri = data!!.data
                    // Get the path from the Uri
//                    final String pathFile = getPathFromURI(selectedImageUri);
//                    Log.d(TAG,selectedImageUri.toString());
//                    if (selectedImageUri != null) {
//                        File f = null;
//                        f = new File(selectedImageUri);
//                        path = String.valueOf(Uri.fromFile(f));
//                    }
//                    Toast.makeText(context,
//                        "ImagePath is " + data.getStringArrayExtra("imgPath"),
//                        Toast.LENGTH_LONG).show(/
                    //                    Toast.makeText(context, "ImagePath is " +data.getData().getEncodedPath(), Toast.LENGTH_LONG).show();
//                    startLoadTask();
                    handleSelectFromAblum(data)
                }
                TAKE_PHOTO_CODE -> handleTakePhoto(data)
                ACTION_REQUEST_EDITIMAGE -> handleEditorImage(data)
            }
        }
    }

    /**
     * 处理拍照返回
     *
     * @param data
     */
    private fun handleTakePhoto(data: Intent?) {
        if (photoURI != null) {
            path = photoURI!!.path
            Toast.makeText(context, "After Image take Path $path", Toast.LENGTH_SHORT).show()
            startLoadTask()
        }
    }

    private fun handleEditorImage(data: Intent?) {
        var newFilePath = data!!.getStringExtra(EditImageActivity.EXTRA_OUTPUT)
        val isImageEdit = data.getBooleanExtra(EditImageActivity.IMAGE_IS_EDIT, false)
        if (isImageEdit) {
//            Toast.makeText(this, getString(R.string.save_path, newFilePath), Toast.LENGTH_LONG).show();
        } else {
            newFilePath = data.getStringExtra(EditImageActivity.FILE_PATH)
        }
        Log.d("image is edit", isImageEdit.toString() + "")
        val loadTask: LoadImageTask = LoadImageTask()
        loadTask.execute(newFilePath)
    }

    private fun handleSelectFromAblum(data: Intent?) {
        val filepath = data!!.getStringExtra("imgPath")
//                var f =  File(data.getData().toString());
//        Toast.makeText(context, "FilePAth is "+f.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        path = filepath
        // System.out.println("path---->"+path);
        startLoadTask()
    }

    private fun startLoadTask() {
        val task: LoadImageTask = LoadImageTask()
        task.execute(path)
    }

    private inner class LoadImageTask : AsyncTask<String?, Void?, Bitmap>() { // end inner class
        override fun doInBackground(vararg params: String?): Bitmap {
            return BitmapUtils.getSampledBitmap(params[0], imageWidth / 4, imageHeight / 4)

        }

        override fun onCancelled() {
            super.onCancelled()
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        override fun onCancelled(result: Bitmap) {
            super.onCancelled(result)
        }

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun onPostExecute(result: Bitmap) {
            super.onPostExecute(result)
            if (mainBitmap != null) {
                mainBitmap!!.recycle()
                mainBitmap = null
                System.gc()
            }
            mainBitmap = result
            imgView!!.setImageBitmap(mainBitmap)
        }


    }

    companion object {
        const val REQUEST_PERMISSON_SORAGE = 1
        const val REQUEST_PERMISSON_CAMERA = 2
        const val SELECT_GALLERY_IMAGE_CODE = 7
        const val TAKE_PHOTO_CODE = 8
        const val ACTION_REQUEST_EDITIMAGE = 9
        const val ACTION_STICKERS_IMAGE = 10
    }
} //end class
