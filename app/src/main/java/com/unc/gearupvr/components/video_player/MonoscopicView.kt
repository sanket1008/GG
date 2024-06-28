/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Modifications from original source file: Removed the need for intent and hardcoded sample file.
 * Also removed options to show VR Image. Converted file to Kotlin.
 */

package com.unc.gearupvr.components.video_player

import android.content.Context
import android.content.Intent
import android.graphics.PointF
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.util.AttributeSet
import android.view.*
import androidx.annotation.AnyThread
import androidx.annotation.BinderThread
import androidx.annotation.RawRes
import androidx.annotation.UiThread
import com.google.vr.sdk.base.Eye.Type
import android.view.Display
import android.view.OrientationEventListener
import android.view.Surface
import android.view.WindowManager
import com.unc.gearupvr.components.video_player.rendering.SceneRenderer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.*


/**
 * Renders a GL scene in a non-VR Activity that is affected by phone orientation and touch input.
 *
 *
 * The two input components are the TYPE_GAME_ROTATION_VECTOR Sensor and a TouchListener. The GL
 * renderer combines these two inputs to render a scene with the appropriate camera orientation.
 *
 *
 * The primary complexity in this class is related to the various rotations. It is important to
 * apply the touch and sensor rotations in the correct order or the user's touch manipulations won't
 * match what they expect.
 */
class MonoscopicView
    (context: Context, attributeSet: AttributeSet) : GLSurfaceView(context, attributeSet) {
    // We handle all the sensor orientation detection ourselves.
    private lateinit var sensorManager: SensorManager
    private lateinit var orientationSensor: Sensor
    private lateinit var phoneOrientationListener: PhoneOrientationListener
    private lateinit var displayOrientationListener: DisplayOrientationListener
    private var displayRotationDegrees: Int = 0

    private lateinit var mediaLoader: MediaLoader
    private lateinit var renderer: Renderer
    private lateinit var touchTracker: TouchTracker
    private lateinit var uiView: VideoUiView

    init {
        preserveEGLContextOnPause = true
    }

    /**
     * Finishes initialization. This should be called immediately after the View is inflated.
     *
     * @param uiView the video UI that should be bound to the underlying SceneRenderer
     */
    fun initialize(uiView: VideoUiView) {
        this.uiView = uiView
        mediaLoader = MediaLoader(context)

        // Configure OpenGL.
        renderer =
            Renderer(uiView, mediaLoader)
        setEGLContextClientVersion(2)
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY

        // Configure sensors and touch.
        sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        // TYPE_GAME_ROTATION_VECTOR is the easiest sensor since it handles all the complex math for
        // fusion. It's used instead of TYPE_ROTATION_VECTOR since the latter uses the magnetometer on
        // devices. When used indoors, the magnetometer can take some time to settle depending on the
        // device and amount of metal in the environment.
        orientationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)!!
        phoneOrientationListener = PhoneOrientationListener()

        // When a phone rotates from portrait <-> landscape or portrait <-> reverse landscape, this flow
        // is used. However, this flow isn't used for landscape <-> reverse landscape changes. For that
        // case, displayOrientationListener's onOrientationChanged callback is used.
        displayOrientationListener = DisplayOrientationListener(context)
        displayOrientationListener.recomputeDisplayOrientation()


        touchTracker = TouchTracker(renderer)
        setOnTouchListener(touchTracker)
    }

    /** Starts the sensor & video only when this View is active.  */
    override fun onResume() {
        super.onResume()
        // Use the fastest sensor readings.
        sensorManager.registerListener(
            phoneOrientationListener, orientationSensor, SensorManager.SENSOR_DELAY_FASTEST
        )
        displayOrientationListener.enable()
        mediaLoader.resume()
    }

    /** Stops the sensors & video when the View is inactive to avoid wasting battery.  */
    override fun onPause() {
        mediaLoader.pause()
        sensorManager.unregisterListener(phoneOrientationListener)
        displayOrientationListener.disable()
        super.onPause()
    }

    /** Destroys the underlying resources. If this is not called, the MediaLoader may leak.  */
    fun destroy() {
        uiView.setMediaPlayer(null)
        mediaLoader.destroy()
    }

    /** Calls MediaLoader to load the appropriate media.
     * @param video: video to use from res/raw
     * @param horizontalDegrees: horizontal view angle. This can be set to 180 for 180 style videos.
     */
    suspend fun loadMedia(@RawRes video: Int, horizontalDegrees: Float) {
        //mediaLoader.loadVrVideo(uiView, video, horizontalDegrees)
    }

    /** Parses the Intent and loads the appropriate media.  */
    fun loadMedia(intent: Intent?) {
        mediaLoader.handleIntent(intent, uiView)
    }

    /** Detects sensor events and saves them as a matrix.  */
    private inner class PhoneOrientationListener : SensorEventListener {
        private val phoneInWorldSpaceMatrix = FloatArray(16)
        private val remappedPhoneMatrix = FloatArray(16)
        private val angles = FloatArray(3)

        @BinderThread
        override fun onSensorChanged(event: SensorEvent) {
            SensorManager.getRotationMatrixFromVector(phoneInWorldSpaceMatrix, event.values)

            // Extract the phone's roll and pass it on to touchTracker & renderer. Remapping is required
            // since we need the calculated roll of the phone to be independent of the phone's pitch &
            // yaw. Any operation that decomposes rotation to Euler angles needs to be performed
            // carefully.
            SensorManager.remapCoordinateSystem(
                phoneInWorldSpaceMatrix,
                SensorManager.AXIS_X, SensorManager.AXIS_MINUS_Z,
                remappedPhoneMatrix
            )
            SensorManager.getOrientation(remappedPhoneMatrix, angles)
            val roll = angles[2]
            touchTracker.setRoll((roll - Math.toRadians(displayRotationDegrees.toDouble())).toFloat())

            // Rotate from Android coordinates to OpenGL coordinates. Android's coordinate system
            // assumes Y points North and Z points to the sky. OpenGL has Y pointing up and Z pointing
            // toward the user.
            Matrix.rotateM(phoneInWorldSpaceMatrix, 0, 90f, 1f, 0f, 0f)
            renderer.setDeviceOrientation(phoneInWorldSpaceMatrix, roll)
        }

        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }

    /** Detects coarse-grained sensor events and saves them as a matrix.  */
    private inner class DisplayOrientationListener(val context: Context?) :
        OrientationEventListener(context) {
        /**
         * This is called when Android's sensors detect rotation of the screen. It is specifically
         *
         * @param orientationDegrees the current orientation with a precision of single digit degrees.
         * This is clockwise with respect to the device's natural orientation.
         */
        @BinderThread
        override fun onOrientationChanged(orientationDegrees: Int) {
            val counterClockwiseOrientation = 360 - orientationDegrees
            val roundedOrientation = (counterClockwiseOrientation + 45) % 360 / 90 * 90
            if (abs(displayRotationDegrees - roundedOrientation) > 90) {
                recomputeDisplayOrientation()
            }
        }

        @AnyThread
        fun recomputeDisplayOrientation() {

            val windowManager =
                context?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display: Display = windowManager.defaultDisplay
            displayRotationDegrees = when (display.rotation) {
                Surface.ROTATION_90 -> 90
                Surface.ROTATION_180 -> 180
                Surface.ROTATION_270 -> 270
                else -> 0
            }
            renderer.setDisplayRotation(displayRotationDegrees)
        }
    }

    /**
     * Basic touch input system.
     *
     *
     * Mixing touch input and gyro input results in a complicated UI so this should be used
     * carefully. This touch system implements a basic (X, Y) -> (yaw, pitch) transform. This works
     * for basic UI but fails in edge cases where the user tries to drag scene up or down. There is no
     * good UX solution for this. The least bad solution is to disable pitch manipulation and only let
     * the user adjust yaw. This example tries to limit the awkwardness by restricting pitch
     * manipulation to +/- 45 degrees.
     *
     *
     * It is also important to get the order of operations correct. To match what users expect,
     * touch interaction manipulates the scene by rotating the world by the yaw offset and tilting the
     * camera by the pitch offset. If the order of operations is incorrect, the sensors & touch
     * rotations will have strange interactions. The roll of the phone is also tracked so that the
     * x & y are correctly mapped to yaw & pitch no matter how the user holds their phone.
     *
     *
     * This class doesn't handle any scrolling inertia but Android's
     * https://developer.android.com/reference/androidx/recyclerview/widget/RecyclerView.OnFlingListener
     * can be used with this code for a nicer UI. An even more advanced UI would reproject the user's
     * touch point into 3D and drag the Mesh as the user moves their finger. However, that requires
     * quaternion interpolation and is beyond the scope of this sample.
     */
    private class TouchTracker(private val renderer: Renderer) : OnTouchListener {
        // With every touch event, update the accumulated degrees offset by the new pixel amount.
        private val previousTouchPointPx = PointF()
        private val accumulatedTouchOffsetDegrees = PointF()
        // The conversion from touch to yaw & pitch requires compensating for device roll. This is set
        // on the sensor thread and read on the UI thread.
        @Volatile
        private var roll = 0f

        /**
         * Converts ACTION_MOVE events to pitch & yaw events while compensating for device roll.
         */
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            return when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Initialize drag gesture.
                    previousTouchPointPx.set(event.x, event.y)
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    // Calculate the touch delta in screen space.
                    val touchX = (event.x - previousTouchPointPx.x) / PX_PER_DEGREES
                    val touchY = (event.y - previousTouchPointPx.y) / PX_PER_DEGREES
                    previousTouchPointPx.set(event.x, event.y)

                    val r = roll  // Copy volatile state.
                    val cr = cos(r)
                    val sr = sin(r)
                    // To convert from screen space to the 3D space, we need to adjust the drag vector based
                    // on the roll of the phone. This is standard rotationMatrix(roll) * vector math but has
                    // an inverted y-axis due to the screen-space coordinates vs GL coordinates.
                    // Handle yaw.
                    accumulatedTouchOffsetDegrees.x -= (cr * touchX) - (sr * touchY)
                    // Handle pitch and limit it to 45 degrees.
                    accumulatedTouchOffsetDegrees.y += (sr * touchX) + (cr * touchY)
                    accumulatedTouchOffsetDegrees.y = max(
                        -MAX_PITCH_DEGREES,
                        min(MAX_PITCH_DEGREES, accumulatedTouchOffsetDegrees.y)
                    )

                    renderer.setPitchOffset(accumulatedTouchOffsetDegrees.y)
                    renderer.setYawOffset(accumulatedTouchOffsetDegrees.x)
                    true //handled event
                }
                else -> return false
            }
        }

        @BinderThread
        fun setRoll(roll: Float) {
            // We compensate for roll by rotating in the opposite direction.
            this.roll = -roll
        }

        companion object {
            // Arbitrary touch speed number. This should be tweaked so the scene smoothly follows the
            // finger or derived from DisplayMetrics.
            const val PX_PER_DEGREES = 25f
            // Touch input won't change the pitch beyond +/- 45 degrees. This reduces awkward situations
            // where the touch-based pitch and gyro-based pitch interact badly near the poles.
            const val MAX_PITCH_DEGREES = 45f
        }
    }

    /**
     * Standard GL Renderer implementation. The notable code is the matrix multiplication in
     * onDrawFrame and updatePitchMatrix.
     */
    private class Renderer(
        private val uiView: VideoUiView?,
        private val mediaLoader: MediaLoader
    ) : GLSurfaceView.Renderer {
        private val scene = SceneRenderer(null, null, null, null)
        private val projectionMatrix = FloatArray(16)

        // There is no model matrix for this scene so viewProjectionMatrix is used for the mvpMatrix.
        private val viewProjectionMatrix = FloatArray(16)

        // Device orientation is derived from sensor data. This is accessed in the sensor's thread and
        // the GL thread.
        private val deviceOrientationMatrix = FloatArray(16)

        //Adding landscape to matrix
        private val displayRotationMatrix = FloatArray(16)

        // Optional pitch and yaw rotations are applied to the sensor orientation. These are accessed on
        // the UI, sensor and GL Threads.
        private val touchPitchMatrix = FloatArray(16)
        private val touchYawMatrix = FloatArray(16)
        private var touchPitch: Float = 0f
        private var deviceRoll: Float = 0f

        // viewMatrix = touchPitch * deviceOrientation * touchYaw.
        private val viewMatrix = FloatArray(16)
        private val tempMatrix = FloatArray(16)

        init {
            Matrix.setIdentityM(deviceOrientationMatrix, 0)
            Matrix.setIdentityM(displayRotationMatrix, 0)
            Matrix.setIdentityM(touchPitchMatrix, 0)
            Matrix.setIdentityM(touchYawMatrix, 0)
        }

        override fun onSurfaceCreated(gl: GL10, config: EGLConfig) {

            //[.glInit] is used to finish initializing the object on the GL thread.
            scene.glInit()
            if (uiView != null) {
                scene.externalFrameListener = uiView.frameListener
            }
            mediaLoader.onGlSceneReady(scene)
        }

        override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
            GLES20.glViewport(0, 0, width, height)

            val aspectRatio = width.toFloat() / height
            val verticalFovDegrees: Float =
                if (aspectRatio < 1) { // For portrait mode, use the max FOV for the vertical FOV.
                    FIELD_OF_VIEW_DEGREES
                } else { // When in landscape mode, we need to compute the vertical FOV to pass into
                    // Matrix.perspectiveM. As a quick calculation we could use
                    // verticalFovDegrees = FIELD_OF_VIEW_DEGREES / aspectRatio. However, this results in an
                    // incorrect FOV for large values of FIELD_OF_VIEW_DEGREES. The correct calculation should
                    // compute the ratios of the tan of the vertical & horizontal FOVs.
                    val horizontalHalfFovRadians =
                        Math.toRadians((FIELD_OF_VIEW_DEGREES / 2).toDouble())
                    val horizontalHalfFovTanAngle =
                        tan(horizontalHalfFovRadians)
                    val verticalHalfFovTanAngle: Double = horizontalHalfFovTanAngle / aspectRatio
                    val verticalHalfFovRadians =
                        atan(verticalHalfFovTanAngle)
                    Math.toDegrees(2 * verticalHalfFovRadians).toFloat()
                }

            Matrix.perspectiveM(
                projectionMatrix,
                0,
                verticalFovDegrees,//FIELD_OF_VIEW_DEGREES,
                aspectRatio,
                Z_NEAR,
                Z_FAR
            )
        }

        override fun onDrawFrame(gl: GL10) {
            // Combine touch & sensor data.
            // Orientation = pitch * sensor * yaw since that is closest to what most users expect the
            // behavior to be.
            synchronized(this) {
                Matrix.multiplyMM(tempMatrix, 0, deviceOrientationMatrix, 0, touchYawMatrix, 0)
                Matrix.multiplyMM(viewMatrix, 0, touchPitchMatrix, 0, tempMatrix, 0)
                Matrix.multiplyMM(tempMatrix, 0, displayRotationMatrix, 0, viewMatrix, 0)
            }

            Matrix.multiplyMM(viewProjectionMatrix, 0, projectionMatrix, 0, tempMatrix, 0)
            scene.glDrawFrame(viewProjectionMatrix, Type.MONOCULAR)
        }

        /** Adjusts the GL camera's rotation based on device rotation. Runs on the sensor thread.  */
        @BinderThread
        @Synchronized
        fun setDeviceOrientation(matrix: FloatArray, deviceRoll: Float) {
            System.arraycopy(matrix, 0, deviceOrientationMatrix, 0, deviceOrientationMatrix.size)
            this.deviceRoll = -deviceRoll
            updatePitchMatrix()
        }

        /**
         * Adjust the rendered scene to handle portrait, landscape, etc display rotations.
         *
         * @param displayRotationDegrees should be a multiple of 90 degrees.
         */
        @Synchronized
        fun setDisplayRotation(displayRotationDegrees: Int) {
            Matrix.setRotateM(
                displayRotationMatrix,
                0,
                displayRotationDegrees.toFloat(),
                0f,
                0f,
                1f
            )
        }

        /**
         * Updates the pitch matrix after a physical rotation or touch input. The pitch matrix rotation
         * is applied on an axis that is dependent on device rotation so this must be called after
         * either touch or sensor update.
         */
        @AnyThread
        private fun updatePitchMatrix() {
            // The camera's pitch needs to be rotated along an axis that is parallel to the real world's
            // horizon. This is the <1, 0, 0> axis after compensating for the device's roll.
            Matrix.setRotateM(
                touchPitchMatrix,
                0,
                -touchPitch,
                cos(deviceRoll),
                sin(deviceRoll),
                0f
            )
        }

        /** Set the pitch offset matrix.  */
        @UiThread
        @Synchronized
        fun setPitchOffset(pitchDegrees: Float) {
            touchPitch = pitchDegrees
            updatePitchMatrix()
        }

        /** Set the yaw offset matrix.  */
        @UiThread
        @Synchronized
        fun setYawOffset(yawDegrees: Float) {
            Matrix.setRotateM(touchYawMatrix, 0, -yawDegrees, 0f, 1f, 0f)
        }

        companion object {

            // Arbitrary vertical field of view. Adjust as desired.
            private const val FIELD_OF_VIEW_DEGREES = 90f
            private const val Z_NEAR = .1f
            private const val Z_FAR = 100f
        }
    }
}
