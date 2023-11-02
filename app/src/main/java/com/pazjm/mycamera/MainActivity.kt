package com.pazjm.mycamera

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.camera.core.CameraProvider
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.util.Log
import android.view.Surface.ROTATION_0
import android.view.Surface.ROTATION_180
import android.widget.ImageButton
import android.widget.Switch
import android.widget.Toast
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector.LENS_FACING_BACK
import androidx.camera.core.CameraSelector.LENS_FACING_FRONT
import androidx.camera.core.CameraSelector.LensFacing
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ReportFragment.Companion.reportFragment
import com.google.android.material.button.MaterialButton
import com.google.common.util.concurrent.ListenableFuture
import java.io.ByteArrayOutputStream
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var cameraProviderFuture:ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraPreviewView: PreviewView
    private lateinit var reqPermissionBtn:MaterialButton
    private lateinit var camSwitchBtn:ImageButton
    private lateinit var flashControlBtn:ImageButton
    private lateinit var camCaptureBtn:ImageButton

    private lateinit var camera: Camera
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var imageCapture: ImageCapture
    private var lensFacing = LENS_FACING_BACK
    private var enableFlash = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        cameraPreviewView = findViewById(R.id.cameraPreview)
        reqPermissionBtn = findViewById(R.id.reqPermissionBtn)
        camSwitchBtn = findViewById(R.id.camSwtichBtn)
        flashControlBtn = findViewById(R.id.flashControlBtn)
        camCaptureBtn = findViewById(R.id.camCaptureBtn)

        reqPermissionBtn.setOnClickListener {
            requestPermission()
        }

        //request for provider
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        //swtich camera
        camSwitchBtn.setOnClickListener {
            switchCamera()
        }

        flashControlBtn.setOnClickListener {
            flashToggle(!enableFlash)
        }

        camCaptureBtn.setOnClickListener {
            capture()

        }

        //call permission button
        reqPermissionBtn.callOnClick()
    }

    private fun capture(){
        if(!this::imageCapture.isInitialized){
            return
        }

        val file = File(applicationContext.filesDir, "current_img")
        file.delete().apply {
            val metadata = ImageCapture.Metadata()
            metadata.isReversedHorizontal = true
            val outputFileOptions = ImageCapture.OutputFileOptions.Builder(file).setMetadata(metadata).build()

            imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(this@MainActivity),
                object: ImageCapture.OnImageSavedCallback{
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        val intent = Intent(this@MainActivity, ImageCapturedAct::class.java)
                        startActivity(intent)
                    }

                    override fun onError(exception: ImageCaptureException) {
                        Toast.makeText(applicationContext, exception.message, Toast.LENGTH_LONG).show()
                    }

                })
        }

    }



    @SuppressLint("UseCompatLoadingForDrawables")
    private fun flashToggle(enabled: Boolean){
        if(!this::camera.isInitialized){
            return
        }
        else if(lensFacing != LENS_FACING_BACK){
           // Toast.makeText(this, "Torch available at the back camera only", Toast.LENGTH_SHORT).show()
            return
        }

        camera.cameraControl.enableTorch(enabled)
        when(enabled){
            true->{
                flashControlBtn.setImageDrawable(getDrawable(R.drawable.baseline_flash_on_24))
            }
            false->{
                flashControlBtn.setImageDrawable(getDrawable(R.drawable.baseline_flash_off_24))
            }

        }
        enableFlash = enabled
    }

    private fun switchCamera(){
        if(!this::cameraProvider.isInitialized){
            return
        }

        if(lensFacing==CameraSelector.LENS_FACING_FRONT) {
            lensFacing = LENS_FACING_BACK
        }
        else if(lensFacing == LENS_FACING_BACK){
            lensFacing = CameraSelector.LENS_FACING_FRONT
        }
        else{
            lensFacing = LENS_FACING_BACK
        }

        cameraProvider.unbindAll().apply {
            launchCamera()
        }
    }

    private fun launchCamera(){
        //disable also reqPermission Btn
        reqPermissionBtn.toggleStatus(false)

        cameraProviderFuture.addListener(Runnable {
            cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)
            flashToggle(enableFlash)
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider) {
        val preview: Preview = Preview.Builder().build()
        imageCapture = ImageCapture.Builder().build()
        val cameraSelector = CameraSelector.Builder().requireLensFacing(lensFacing)
            .build()

        camera = cameraProvider.bindToLifecycle(this as LifecycleOwner,cameraSelector, preview, imageCapture)

        preview.setSurfaceProvider(cameraPreviewView.surfaceProvider)

    }

    private fun checkPermission():Boolean{
        return (ContextCompat.checkSelfPermission(this, REQUIRED_PERMISSION)==PackageManager.PERMISSION_GRANTED)
    }

    private fun requestPermission(){
        if (checkPermission()){
            //already granted
            launchCamera()
        }
        else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMSSIONS_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode== PERMSSIONS_CODE){
            //check
            for(i in grantResults.indices){
                if(grantResults[i]==PackageManager.PERMISSION_GRANTED){
                    launchCamera()
                }
            }
        }
        else{
            Toast.makeText(this, "Camera is needed for this app to function", Toast.LENGTH_SHORT).show()
        }
    }

    private fun MaterialButton.toggleStatus(enabled:Boolean){
        this.isEnabled = enabled
        this.visibility = if(enabled) View.VISIBLE else View.GONE
    }

    companion object{
        private  val REQUIRED_PERMISSION =
         android.Manifest.permission.CAMERA

        private  val REQUIRED_PERMISSIONS =
            mutableListOf<String>(android.Manifest.permission.CAMERA).toTypedArray()

        private  val PERMSSIONS_CODE = 7
    }
}