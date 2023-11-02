package com.pazjm.mycamera

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import java.io.File
import java.io.FileReader

class ImageCapturedAct : AppCompatActivity() {
    private lateinit var backBtn:Button
    private lateinit var downloadBtn:Button
    private lateinit var capturedPreview: ImageView

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

    fun byteArrayToBitmap(){

    }
}