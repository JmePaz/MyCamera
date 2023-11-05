package com.pazjm.mycamera

import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.graphics.drawable.toBitmap
import java.io.ByteArrayOutputStream
import java.io.File
import java.lang.NullPointerException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ImageCapturedAct : AppCompatActivity() {
    private lateinit var backBtn:Button
    private lateinit var downloadBtn:Button
    private lateinit var capturedPreview: ImageView
    private var hasPermission: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_captured)
        //set all components
        backBtn = findViewById(R.id.backBtn)
        downloadBtn = findViewById(R.id.downloadBtn)
        capturedPreview = findViewById(R.id.capturedPreview)


        //read captured Image
        readCapturedImg().apply {
            capturedPreview.setImageBitmap(this)
        }

        //button listener
        backBtn.setOnClickListener {
            goBack()
        }

        downloadBtn.setOnClickListener {
         saveImg()
        }

    }

    private fun saveImg(){
        if(Build.VERSION.SDK_INT<=Build.VERSION_CODES.P &&!request_permission()){
            return
        }

        if(Build.VERSION.SDK_INT<=Build.VERSION_CODES.P){
            saveImgBelowSDK28()
        }
        else{
            saveImgByMediaStore()
        }
    }


    private fun saveImgByMediaStore(){
        val values = ContentValues()
        values.put(MediaStore.Images.Media.DISPLAY_NAME, "${getCurrDateTime()}.jpg")
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg")
        val uri =
            contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return
        val byteArray = ByteArrayOutputStream()
        capturedPreview.drawable.toBitmap()
            .compress(Bitmap.CompressFormat.JPEG, 20, byteArray)
        contentResolver.openOutputStream(uri).use {
            it?.write(byteArray.toByteArray())
        }.apply {
            Toast.makeText(baseContext, "Saved", Toast.LENGTH_LONG).show()
            byteArray.close()
        }

    }

    private fun saveImgBelowSDK28(){
        try {
            val folder = File(Environment.getExternalStorageDirectory(), "mycamera")
            if(!folder.exists())folder.mkdirs()

            val file = File(folder,"${getCurrDateTime()}.jpg" )

            file.outputStream().use {
                val byteArray = ByteArrayOutputStream()
                capturedPreview.drawable.toBitmap()
                    .compress(Bitmap.CompressFormat.JPEG, 20, byteArray)
                it.write(byteArray.toByteArray())
            }.apply {
                Toast.makeText(baseContext, "Saved at ${file.absolutePath}", Toast.LENGTH_LONG).show()
            }
        }
        catch (e:Exception){
            Toast.makeText(this, "$e", Toast.LENGTH_LONG).show()
            return
        }
    }


    private fun getCurrDateTime():String{
        val dateTime = Calendar.getInstance().time
        val formatter = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        return formatter.format(dateTime)
    }

    private fun goBack(){
        val intent = Intent(this@ImageCapturedAct, MainActivity::class.java)
        startActivity(intent)
    }

    private fun readCapturedImg():Bitmap{
        val file = File(applicationContext.filesDir, "current_img")
        val bytes = file.readBytes()
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

    }
    private fun check_permission():Boolean{
        return ActivityCompat.checkSelfPermission(this, REQUIRED_PERMISSIONS[0])==PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, REQUIRED_PERMISSIONS[1])==PackageManager.PERMISSION_GRANTED
    }

    private fun request_permission():Boolean{
        hasPermission = if(!check_permission()){
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSION_CODE)
            false
        } else{
            true
        }
        return  hasPermission
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode== PERMISSION_CODE){
            var permissionStat = true
            for( result  in grantResults){
                permissionStat = permissionStat&&(result==PackageManager.PERMISSION_GRANTED)

            }

            if(permissionStat){
                hasPermission = true
                saveImg()
            }
        }
    }



    companion object{
        private val REQUIRED_PERMISSIONS = arrayOf(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        private const val PERMISSION_CODE = 7
    }


}