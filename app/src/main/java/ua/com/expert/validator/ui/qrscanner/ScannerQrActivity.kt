package ua.com.expert.validator.ui.qrscanner

import android.content.pm.PackageManager
import android.hardware.Camera
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import android.view.View
import com.journeyapps.barcodescanner.CaptureManager
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.journeyapps.barcodescanner.camera.CameraSettings
import kotlinx.android.synthetic.main.activity_qr_scanner.*
import ua.com.expert.validator.R

class ScannerQrActivity : AppCompatActivity(), DecoratedBarcodeView.TorchListener {


    private lateinit var capture: CaptureManager
    private lateinit var settings: CameraSettings
    private var lightOn = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qr_scanner)

        zxing_barcode_scanner.setTorchListener(this)
        settings = zxing_barcode_scanner.barcodeView.cameraSettings
        capture = CaptureManager(this, zxing_barcode_scanner)
        capture!!.initializeFromIntent(intent, savedInstanceState)
        capture!!.decode()
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            switch_flashlight.setOnClickListener { v -> switchFlashlight(v) }
        } else {
            switch_flashlight.visibility = View.GONE
        }

        if (Camera.getNumberOfCameras() == 1) {
            switch_camera.visibility = View.GONE
        } else {
            switch_camera.visibility = View.VISIBLE
            switch_camera.setOnClickListener { v -> switchCamera(v) }
        }
        showButtonFlash()
    }

    override fun onResume() {
        super.onResume()
        capture!!.onResume()
    }

    override fun onPause() {
        super.onPause()
        capture!!.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        capture!!.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        capture!!.onSaveInstanceState(outState)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return zxing_barcode_scanner!!.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event)
    }

    fun switchFlashlight(view: View) {
        if (!lightOn) {
            zxing_barcode_scanner!!.setTorchOn()
            lightOn = true
        } else {
            zxing_barcode_scanner!!.setTorchOff()
            lightOn = false
        }
    }

    fun switchCamera(view: View) {


        if (zxing_barcode_scanner!!.barcodeView.isPreviewActive) {
            zxing_barcode_scanner!!.pause()
        }

        //swap the id of the camera to be used
        if (settings!!.requestedCameraId === Camera.CameraInfo.CAMERA_FACING_BACK) {
            settings!!.requestedCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT
        } else {
            settings!!.requestedCameraId = Camera.CameraInfo.CAMERA_FACING_BACK
        }
        zxing_barcode_scanner!!.barcodeView.cameraSettings = settings

        zxing_barcode_scanner!!.resume()

        showButtonFlash()
    }

    override fun onTorchOn() {
        switch_flashlight!!.background = getResources().getDrawable(R.drawable.ic_flash_off)
    }

    override fun onTorchOff() {
        switch_flashlight!!.background = getResources().getDrawable(R.drawable.ic_flash_on)
    }

    private fun showButtonFlash() {
        //        Camera mCamera = Camera.open(0);
        //        Camera.Parameters params = mCamera.getParameters();
        //        List<String> flashModes = params.getSupportedFlashModes();
        //        if (flashModes == null) {
        //            switchFlashlight.setVisibility(View.GONE);
        //        }
        //
        //        for (String flashMode : flashModes) {
        //            if (Camera.Parameters.FLASH_MODE_ON.equals(flashMode)) {
        //                switchFlashlight.setVisibility(View.VISIBLE);
        //            }
        //        }
        //
        //        switchFlashlight.setVisibility(View.GONE);
    }
}
