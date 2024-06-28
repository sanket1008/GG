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
 * Modifications from original source file: Converted to Kotlin.
 */

package com.unc.gearupvr.components.video_player.rendering

import android.content.Context
import android.graphics.SurfaceTexture
import android.graphics.SurfaceTexture.OnFrameAvailableListener
import android.opengl.GLES20
import android.opengl.Matrix
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.util.Pair
import android.view.InputDevice
import android.view.MotionEvent
import android.view.Surface
import android.view.ViewGroup
import androidx.annotation.AnyThread
import androidx.annotation.BinderThread
import androidx.annotation.MainThread
import androidx.annotation.Nullable
import com.google.vr.sdk.controller.Orientation
import com.unc.gearupvr.components.video_player.VideoUiView
import com.unc.gearupvr.components.video_player.rendering.Utils.checkGlError
import java.util.concurrent.atomic.AtomicBoolean

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
*/

/**
 * Controls and renders the GL Scene.
 *
 *
 * This class is shared between MonoscopicView & VrVideoActivity. It renders the display mesh, UI
 * and controller reticle as required. It also has basic Controller input which allows the user to
 * interact with [VideoUiView] while in VR.
 */
class SceneRenderer internal constructor(
    // These are only valid if createForVR() has been called. In the 2D Activity, these are null
// since the UI is rendered in the standard Android layout.
    private val canvasQuad: CanvasQuad?,
    private val videoUiView: VideoUiView?,
    private val uiHandler: Handler?,
    externalFrameListener: OnFrameAvailableListener?
) {
    // This is the primary interface between the Media Player and the GL Scene.
    private var displayTexture: SurfaceTexture? = null
    private val frameAvailable =
        AtomicBoolean()
    // Used to notify clients that displayTexture has a new frame. This requires synchronized access.
    @Nullable
    var externalFrameListener: OnFrameAvailableListener?
    // GL components for the mesh that display the media. displayMesh should only be accessed on the
// GL Thread, but requestedDisplayMesh needs synchronization.
    @Nullable
    private var displayMesh: Mesh? = null
    @Nullable
    private var requestedDisplayMesh: Mesh? = null
    private var displayTexId = 0
    // Controller components.
//    private val reticle = Reticle()
    @Nullable
    private var controllerOrientation: Orientation? = null
    // This is accessed on the binder & GL Threads.
    private val controllerOrientationMatrix = FloatArray(16)

    /**
     * Performs initialization on the GL thread. The scene isn't fully initialized until
     * glConfigureScene() completes successfully.
     */
    fun glInit() {
        checkGlError()
        Matrix.setIdentityM(controllerOrientationMatrix, 0)
        // Set the background frame color. This is only visible if the display mesh isn't a full sphere.
        GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f)
        checkGlError()
        // Create the texture used to render each frame of video.
        displayTexId = Utils.glCreateExternalTexture()
        displayTexture = SurfaceTexture(displayTexId)
        checkGlError()
        // When the video decodes a new frame, tell the GL thread to update the image.
        displayTexture?.setOnFrameAvailableListener { surfaceTexture ->
            frameAvailable.set(true)
            synchronized(this@SceneRenderer) {
                if (externalFrameListener != null) {
                    externalFrameListener?.onFrameAvailable(surfaceTexture)
                }
            }
        }
        canvasQuad?.glInit()
//        reticle.glInit()
    }

    /**
     * Creates the Surface & Mesh used by the MediaPlayer to render video.
     *
     * @param width passed to [SurfaceTexture.setDefaultBufferSize]
     * @param height passed to [SurfaceTexture.setDefaultBufferSize]
     * @param mesh [Mesh] used to display video
     * @return a Surface that can be passed to [android.media.MediaPlayer.setSurface]
     */
    @AnyThread
    @Nullable
    @Synchronized
    fun createDisplay(width: Int, height: Int, mesh: Mesh?): Surface? {
        if (displayTexture == null) {
            Log.e(
                TAG,
                ".createDisplay called before GL Initialization completed."
            )
            return null
        }
        requestedDisplayMesh = mesh
        displayTexture?.setDefaultBufferSize(width, height)
        return Surface(displayTexture)
    }

    /**
     * Configures any late-initialized components.
     *
     *
     * Since the creation of the Mesh can depend on disk access, this configuration needs to run
     * during each drawFrame to determine if the Mesh is ready yet. This also supports replacing an
     * existing mesh while the app is running.
     *
     * @return true if the scene is ready to be drawn
     */
    @Synchronized
    private fun glConfigureScene(): Boolean {
        if (displayMesh == null && requestedDisplayMesh == null) { // The scene isn't ready and we don't have enough information to configure it.
            return false
        }
        // The scene is ready and we don't need to change it so we can glDraw it.
        if (requestedDisplayMesh == null) {
            return true
        }
        // Configure or reconfigure the scene.
        if (displayMesh != null) { // Reconfiguration.
            displayMesh!!.glShutdown()
        }
        displayMesh = requestedDisplayMesh
        requestedDisplayMesh = null
        displayMesh!!.glInit(displayTexId)
        return true
    }

    /**
     * Draws the scene with a given eye pose and type.
     *
     * @param viewProjectionMatrix 16 element GL matrix.
     * @param eyeType an [com.google.vr.sdk.base.Eye.Type] value
     */
    fun glDrawFrame(viewProjectionMatrix: FloatArray?, eyeType: Int) {
        if (!glConfigureScene()) { // displayMesh isn't ready.
            return
        }
        // glClear isn't strictly necessary when rendering fully spherical panoramas, but it can improve
// performance on tiled renderers by causing the GPU to discard previous data.
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        checkGlError()
        // The uiQuad uses alpha.
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        GLES20.glEnable(GLES20.GL_BLEND)
        if (frameAvailable.compareAndSet(true, false)) {
            displayTexture?.updateTexImage()
            checkGlError()
        }
        displayMesh?.glDraw(viewProjectionMatrix ?: return, eyeType)
        //media controller is removed from VR mode
//        if (videoUiView != null) {
//            canvasQuad?.glDraw(viewProjectionMatrix, videoUiView.alpha)
//        }
//        reticle.glDraw(viewProjectionMatrix, controllerOrientationMatrix)
    }

    /** Cleans up the GL resources.  */
    fun glShutdown() {
        if (displayMesh != null) {
            displayMesh?.glShutdown()
        }
        canvasQuad?.glShutdown()
//        reticle.glShutdown()
    }

    /** Updates the Reticle's position with the latest Controller pose.  */
    @BinderThread
    @Synchronized
    fun setControllerOrientation(currentOrientation: Orientation?) {
        controllerOrientation = currentOrientation
        controllerOrientation?.toRotationMatrix(controllerOrientationMatrix)
    }

    /**
     * Processes Daydream Controller clicks and dispatches the event to [VideoUiView] as a
     * synthetic [MotionEvent].
     *
     *
     * This is a minimal input system that works because CanvasQuad is a simple rectangle with a
     * hardcoded location. If the quad had a transformation matrix, then those transformations would
     * need to be used when converting from the Controller's pose to a 2D click event.
     */
    @MainThread
    fun handleClick() {
        if (videoUiView?.alpha ?: 0 == 0F) { // When the UI is hidden, clicking anywhere will make it visible.
            toggleUi()
            return
        }
        if (controllerOrientation == null) { // Race condition between click & pose events.
            return
        }
        val clickTarget = CanvasQuad.translateClick(controllerOrientation ?: return)
        if (clickTarget == null) { // When the click is outside of the View, hide the UI.
            toggleUi()
            return
        }
        // The actual processing of the synthetic event needs to happen in the UI thread.
        uiHandler?.post {
            // Generate a pair of down/up events to make the Android View processing handle the
            // click.
            val now = SystemClock.uptimeMillis()
            val down = MotionEvent.obtain(
                now,
                now,  // Timestamps.
                MotionEvent.ACTION_DOWN,
                clickTarget.x,
                clickTarget.y,
                1f,
                1f,
                0,
                1f,
                1f,
                0,
                0
            ) // Unused config data.
            down.source = InputDevice.SOURCE_GAMEPAD
            videoUiView?.dispatchTouchEvent(down)
            // Clone the down event but change action.
            val up = MotionEvent.obtain(down)
            up.action = MotionEvent.ACTION_UP
            videoUiView?.dispatchTouchEvent(up)
        }
    }

    /** Uses Android's animation system to fade in/out when the user wants to show/hide the UI.  */
    @AnyThread
    fun toggleUi() { // This can be trigged via a controller action so switch to main thread to manipulate the View.
        uiHandler?.post {
            if (videoUiView?.alpha ?: 0 == 0F) {
                videoUiView?.animate()?.alpha(1F)?.start()
            } else {
                videoUiView?.animate()?.alpha(0F)?.start()
            }
        }
    }

    /**
     * Binds a listener used by external clients that need to know when a new video frame is ready.
     * This is used by MonoscopicView to update the video position slider each frame.
     */
    @AnyThread
    @Synchronized
    fun setVideoFrameListener(videoFrameListener: OnFrameAvailableListener?) {
        externalFrameListener = videoFrameListener
    }

    companion object {
        private const val TAG = "SceneRenderer"
        /**
         * Creates a SceneRenderer for 2D but does not initialize it. [.glInit] is used to finish
         * initializing the object on the GL thread.
         */
        fun createFor2D(): SceneRenderer {
            return SceneRenderer(
                null,
                null,
                null,
                null
            )
        }

        /**
         * Creates a SceneRenderer for VR but does not initialize it. [.glInit] is used to finish
         * initializing the object on the GL thread.
         *
         *
         * The also creates a [VideoUiView] that is bound to the VR scene. The View is backed by
         * a [CanvasQuad] and is meant to be rendered in a VR scene.
         *
         * @param context the [Context] used to initialize the [VideoUiView]
         * @param parent the new view is attached to the parent in order to properly handle Android
         * events
         * @return a SceneRender configured for VR and a bound [VideoUiView] that can be treated
         * similar to a View returned from findViewById.
         */
        @MainThread
        fun createForVR(
            context: Context?,
            parent: ViewGroup?
        ): Pair<SceneRenderer, VideoUiView> {
            val canvasQuad = CanvasQuad()
            val videoUiView: VideoUiView =
                parent?.let { VideoUiView.createForOpenGl(context, it, canvasQuad) }!!
            val externalFrameListener: OnFrameAvailableListener =
                videoUiView.frameListener
            val scene =
                SceneRenderer(
                    canvasQuad,
                    videoUiView,
                    Handler(Looper.getMainLooper()),
                    externalFrameListener
                )
            return Pair.create(scene, videoUiView)
        }
    }

    /**
     * Constructs the SceneRenderer with the given values.
     */
/* package */
    init {
        this.externalFrameListener = externalFrameListener
    }
}