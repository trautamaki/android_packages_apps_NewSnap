package org.snappier.camera.camera

import android.util.Log
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import org.snappier.camera.Configuration
import org.snappier.camera.MainActivity
import org.snappier.camera.ViewFinder
import org.snappier.camera.capturer.Capturer
import kotlinx.android.synthetic.main.activity_main.*

abstract class Camera(
    private val activity: MainActivity) {
    open var cameraModeId = Configuration.ID_PICTURE_CAMERA

    var lensFacing: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    lateinit var capturer: Capturer
    lateinit var viewFinder: ViewFinder

    protected var camera: Camera? = null

    abstract fun buildCapturer(): Capturer

    fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(activity.applicationContext)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            capturer = buildCapturer()
            capturer.setCapturerListener(activity)

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Build viewfinder
                viewFinder = ViewFinder(activity, activity.preview_view)

                // Bind use cases to camera
                camera = cameraProvider.bindToLifecycle(
                    activity, lensFacing, viewFinder.preview, capturer.getCapture()
                )

                camera?.cameraInfo?.hasFlashUnit()?.let { activity.options_bar.updateOptions(it) }
                viewFinder.camera = camera
            } catch (exc: Exception) {
                Log.e(MainActivity.TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(activity.applicationContext))
    }
}