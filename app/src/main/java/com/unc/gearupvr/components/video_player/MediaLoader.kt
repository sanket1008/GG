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
import android.graphics.*
import android.media.MediaPlayer
import android.os.AsyncTask
import android.util.Log
import android.view.Surface
import androidx.annotation.AnyThread
import androidx.annotation.MainThread
import com.unc.gearupvr.components.video_player.rendering.Mesh
import com.unc.gearupvr.components.video_player.rendering.SceneRenderer
import java.io.IOException
import java.net.URLConnection
import java.security.InvalidParameterException

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
 * MediaLoader takes an Intent from the user and loads the specified media file.
 *
 *
 * The process to load media requires multiple threads since the media is read from disk on a
 * background thread, but it needs to be loaded into the GL scene only after GL initialization is
 * complete.
 *
 *
 * To keep the sample simple, this class doesn't have any support for handling multiple Intents
 * within a single Activity lifecycle.
 *
 *
 * The Intent used to launch [VideoPlayerActivity] or [VrVideoActivity] is parsed by this
 * class and the extra & data fields are extracted. The data field should have a URI useable by
 * [MediaPlayer] or [BitmapFactory]. There should also be an integer extra matching one
 * of the MEDIA_* types in [Mesh].
 *
 *
 * Example intents compatible with adb are:
 *
 *  *
 * A top-bottom stereo image in the VR Activity.
 * **adb shell am start -a android.intent.action.VIEW  \
 * -n com.google.vr.sdk.samples.video360/.VrVideoActivity \
 * -d "file:///sdcard/IMAGE.JPG" \
 * --ei stereoFormat 2
 ** *
 *
 *  *
 * A monoscopic video in the 2D Activity.
 * **adb shell am start -a android.intent.action.VIEW  \
 * -n com.google.vr.sdk.samples.video360/.VideoActivity \
 * -d "file:///sdcard/VIDEO.MP4" \
 * --ei stereoFormat 0
 ** *
 *
 *
 *
 *
 * This sample does not validiate that a given file is readable by the Android media decoders.
 * You should validate that the file plays on your target devices via
 * **adb shell am start -a android.intent.action.VIEW -t video/mpeg -d "file:///VIDEO.MP4"**
 */
class MediaLoader(private val context: Context) {
    // This can be replaced by any media player that renders to a Surface. In a real app, this
// media player would be separated from the rendering code. It is left in this class for
// simplicity.
// This should be set or cleared in a synchronized manner.
    var mediaPlayer: MediaPlayer? = null
    // This sample also supports loading images.
    var mediaImage: Bitmap? = null
    // If the video or image fails to load, a placeholder panorama is rendered with error text.
    var errorText: String? = null
    // Due to the slow loading media times, it's possible to tear down the app before mediaPlayer is
// ready. In that case, abandon all the pending work.
// This should be set or cleared in a synchronized manner.
    private var isDestroyed = false
    // The type of mesh created depends on the type of media.
    var mesh: Mesh? = null
    // The sceneRenderer is set after GL initialization is complete.
    private var sceneRenderer: SceneRenderer? = null
    // The displaySurface is configured after both GL initialization and media loading.
    private var displaySurface: Surface? = null
    // The actual work of loading media happens on a background thread.
    private var mediaLoaderTask: MediaLoaderTask? = null

    /**
     * Loads custom videos based on the Intent or load the default video. See the Javadoc for this
     * class for information on generating a custom intent via adb.
     */
    fun handleIntent(
        intent: Intent?,
        uiView: VideoUiView?
    ) { // Load the bitmap in a background thread to avoid blocking the UI thread. This operation can
// take 100s of milliseconds.
// Note that this sample doesn't cancel any pending mediaLoaderTasks since it assumes only one
// Intent will ever be fired for a single Activity lifecycle.
        mediaLoaderTask = MediaLoaderTask(uiView)
        mediaLoaderTask?.execute(intent)
    }

    /** Notifies MediaLoader that GL components have initialized.  */
    fun onGlSceneReady(sceneRenderer: SceneRenderer?) {
        this.sceneRenderer = sceneRenderer
        displayWhenReady()
    }

    /**
     * Helper class to media loading. This accesses the disk and decodes images so it needs to run in
     * the background.
     */
    private inner class MediaLoaderTask(private val uiView: VideoUiView?) :
        AsyncTask<Intent?, Void?, Void?>() {
        @Deprecated("Deprecated in Java")
        override fun doInBackground(vararg intent: Intent?): Void? {
            if (intent.isEmpty() || intent[0]?.data == null) { // This happens if the Activity wasn't started with the right intent.
                errorText = "No URI specified. Using default panorama."
                Log.e(TAG, errorText ?: return null)
                return null
            }
            // Extract the stereoFormat from the Intent's extras.
            var stereoFormat = intent[0]
                ?.getIntExtra(MEDIA_FORMAT_KEY, Mesh.MEDIA_MONOSCOPIC)
            if (stereoFormat != Mesh.MEDIA_STEREO_LEFT_RIGHT
                && stereoFormat != Mesh.MEDIA_STEREO_TOP_BOTTOM
            ) {
                stereoFormat = Mesh.MEDIA_MONOSCOPIC
            }
            mesh = Mesh.createUvSphere(
                SPHERE_RADIUS_METERS,
                DEFAULT_SPHERE_ROWS,
                DEFAULT_SPHERE_COLUMNS,
                DEFAULT_SPHERE_VERTICAL_DEGREES,
                DEFAULT_SPHERE_HORIZONTAL_DEGREES,
                stereoFormat
            )
            // Based on the Intent's data, load the appropriate media from disk.

            /**
             * Loads a sample VR video in 2D.
             */

            (intent[0]?.data)?.let { uri ->
                try {
                    val type =
                        URLConnection.guessContentTypeFromName(uri.path)
                    when {
                        type == null -> {
                            throw InvalidParameterException("Unknown file type: $uri")
                        }
                        type.startsWith("image") -> { // Decoding a large image can take 100+ ms.
                            mediaImage = BitmapFactory.decodeFile(uri.path)
                        }
                        type.startsWith("video") -> {
                            val mp = MediaPlayer.create(context, uri)
                            synchronized(this@MediaLoader) {
                                // This needs to be synchronized with the methods that could clear mediaPlayer.
                                mediaPlayer = mp
                            }
                        }
                        else -> {
                            throw InvalidParameterException("Unsupported MIME type: $type")
                        }
                    }
                } catch (e: IOException) {
                    errorText = String.format("Error loading file [%s]: %s", uri.path, e)
                    Log.e(TAG, errorText!!)
                } catch (e: InvalidParameterException) {
                    errorText = String.format("Error loading file [%s]: %s", uri.path, e)
                    Log.e(TAG, errorText!!)
                }
            }

            displayWhenReady()
            return null
        }

        @Deprecated("Deprecated in Java")
        public override fun onPostExecute(unused: Void?) { // Set or clear the UI's mediaPlayer on the UI thread.
            uiView?.setMediaPlayer(mediaPlayer)
        }

    }

    /**
     * Creates the 3D scene and load the media after sceneRenderer & mediaPlayer are ready. This can
     * run on the GL Thread or a background thread.
     */
    @AnyThread
    @Synchronized
    private fun displayWhenReady() {
        if (isDestroyed) { // This only happens when the Activity is destroyed immediately after creation.
            if (mediaPlayer != null) {
                mediaPlayer?.release()
                mediaPlayer = null
            }
            return
        }
        if (displaySurface != null) { // Avoid double initialization caused by sceneRenderer & mediaPlayer being initialized before
// displayWhenReady is executed.
            return
        }
        if (errorText == null && mediaImage == null && mediaPlayer == null || sceneRenderer == null) { // Wait for everything to be initialized.
            return
        }
        // The important methods here are the setSurface & lockCanvas calls. These will have to happen
// after the GLView is created.
        when {
            mediaPlayer != null -> { // For videos, attach the displaySurface and mediaPlayer.
                displaySurface = mesh?.let {
                    sceneRenderer?.createDisplay(
                        (mediaPlayer ?: return).videoWidth, (mediaPlayer ?: return).videoHeight, it
                    )
                }
                mediaPlayer?.setSurface(displaySurface)
                // Start playback.
                mediaPlayer?.isLooping = true
                mediaPlayer?.start()
            }
            mediaImage != null -> { // For images, acquire the displaySurface and draw the bitmap to it. Since our Mesh class uses
                // an GL_TEXTURE_EXTERNAL_OES texture, it's possible to perform this decoding and rendering of
                // a bitmap in the background without stalling the GL thread. If the Mesh used a standard
                // GL_TEXTURE_2D, then it's possible to stall the GL thread for 100+ ms during the
                // glTexImage2D call when loading 4k x 4k panoramas and copying the bitmap's data.
                displaySurface = mesh?.let {
                    sceneRenderer?.createDisplay(
                        (mediaImage ?: return).width, (mediaImage ?: return).height, it
                    )
                }
                val c = displaySurface?.lockCanvas(null)
                c?.drawBitmap(mediaImage ?: return, 0f, 0f, null)
                (displaySurface ?: return).unlockCanvasAndPost(c)
            }
            else -> { // Handle the error case by creating a placeholder panorama.
                mesh = Mesh.createUvSphere(
                    SPHERE_RADIUS_METERS,
                    DEFAULT_SPHERE_ROWS,
                    DEFAULT_SPHERE_COLUMNS,
                    DEFAULT_SPHERE_VERTICAL_DEGREES,
                    DEFAULT_SPHERE_HORIZONTAL_DEGREES,
                    Mesh.MEDIA_MONOSCOPIC
                )
                // 4k x 2k is a good default resolution for monoscopic panoramas.
                displaySurface = sceneRenderer?.createDisplay(
                    2 * DEFAULT_SURFACE_HEIGHT_PX,
                    DEFAULT_SURFACE_HEIGHT_PX,
                    mesh ?: return
                )
                // Render placeholder grid and error text.
                val c = (displaySurface ?: return).lockCanvas(null)
                renderEquirectangularGrid(c, errorText)
                (displaySurface ?: return).unlockCanvasAndPost(c)
            }
        }
    }

    @MainThread
    @Synchronized
    fun pause() {
        if (mediaPlayer != null) {
            mediaPlayer?.pause()
        }
    }

    @MainThread
    @Synchronized
    fun resume() {
        if (mediaPlayer != null) {
            mediaPlayer?.start()
        }
    }

    /** Tears down MediaLoader and prevents further work from happening.  */
    @MainThread
    @Synchronized
    fun destroy() {
        if (mediaPlayer != null) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        }
        isDestroyed = true
    }

    companion object {
        private const val TAG = "MediaLoader"
        const val MEDIA_FORMAT_KEY = "stereoFormat"
        private const val DEFAULT_SURFACE_HEIGHT_PX = 2048
        /** A spherical mesh for video should be large enough that there are no stereo artifacts.  */
        private const val SPHERE_RADIUS_METERS = 50F
        /** These should be configured based on the video type. But this sample assumes 360 video.  */
        private const val DEFAULT_SPHERE_VERTICAL_DEGREES = 180F
        private const val DEFAULT_SPHERE_HORIZONTAL_DEGREES = 360F
        /** The 360 x 180 sphere has 15 degree quads. Increase these if lines in your video look wavy.  */
        private const val DEFAULT_SPHERE_ROWS = 36
        private const val DEFAULT_SPHERE_COLUMNS = 72
        /**
         * Renders a placeholder grid with optional error text.
         */
        private fun renderEquirectangularGrid(
            canvas: Canvas,
            message: String?
        ) { // Configure the grid. Each square will be 15 x 15 degrees.
            val width = canvas.width
            val height = canvas.height
            // This assumes a 4k resolution.
            val majorWidth = width / 256
            val minorWidth = width / 1024
            val paint = Paint()
            // Draw a black ground & gray sky background
            paint.color = Color.BLACK
            canvas.drawRect(0f, height / 2.toFloat(), width.toFloat(), height.toFloat(), paint)
            paint.color = Color.GRAY
            canvas.drawRect(0f, 0f, width.toFloat(), height / 2.toFloat(), paint)
            // Render the grid lines.
            paint.color = Color.WHITE
            for (i in 0 until DEFAULT_SPHERE_COLUMNS) {
                val x = width * i / DEFAULT_SPHERE_COLUMNS
                paint.strokeWidth = if (i % 3 == 0) majorWidth.toFloat() else minorWidth.toFloat()
                canvas.drawLine(x.toFloat(), 0f, x.toFloat(), height.toFloat(), paint)
            }
            for (i in 0 until DEFAULT_SPHERE_ROWS) {
                val y = height * i / DEFAULT_SPHERE_ROWS
                paint.strokeWidth = if (i % 3 == 0) majorWidth.toFloat() else minorWidth.toFloat()
                canvas.drawLine(0f, y.toFloat(), width.toFloat(), y.toFloat(), paint)
            }
            // Render optional text.
            if (message != null) {
                paint.textSize = height / 64.toFloat()
                paint.color = Color.RED
                val textWidth = paint.measureText(message)
                canvas.drawText(
                    message,
                    width / 2 - textWidth / 2,  // Horizontally center the text.
                    9 * height / 16.toFloat(),  // Place it slightly below the horizon for better contrast.
                    paint
                )
            }
        }
    }

}