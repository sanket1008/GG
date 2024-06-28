package com.unc.gearupvr.components.video_player

import android.Manifest.permission
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.vr.ndk.base.DaydreamApi
import com.unc.gearupvr.R
import com.unc.gearupvr.components.video_player.rendering.Mesh
import com.unc.gearupvr.model.Video
import java.io.Serializable

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
 * Basic Activity to hold [MonoscopicView] and render a 360 video in 2D.
 *
 * Most of this Activity's code is related to Android & VR permission handling. The real work is in
 * MonoscopicView.
 *
 * The default intent for this Activity will load a 360 placeholder panorama. For more options on
 * how to load other media using a custom Intent, see [MediaLoader].
 */
class VideoPlayerActivity : Activity() {
    private var videoView: MonoscopicView? = null
    /**
     * Checks that the appropriate permissions have been granted. Otherwise, the sample will wait
     * for the user to grant the permission.
     *
     * @param savedInstanceState unused in this sample but it could be used to track video position
     */
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.video_activity)
        // Configure the MonoscopicView which will render the video and UI.
        videoView = findViewById<View>(R.id.video_view) as MonoscopicView
        val videoUi =
            findViewById<View>(R.id.video_ui_view) as VideoUiView
        videoUi.setVrIconClickListener(
            View.OnClickListener {
                // Convert the Intent used to launch the 2D Activity into one that can launch the VR
                // Activity. This flow preserves the extras and data in the Intent.
                val api = DaydreamApi.create(this@VideoPlayerActivity)
                if (api != null) { // Launch the VR Activity with the proper intent.
                    val intent = DaydreamApi.createVrIntent(
                        ComponentName(this@VideoPlayerActivity, VrVideoActivity::class.java)
                    )
                    intent.data = getIntent().data
                    intent.putExtra(
                        MediaLoader.MEDIA_FORMAT_KEY,
                        getIntent().getIntExtra(
                            MediaLoader.MEDIA_FORMAT_KEY,
                            Mesh.MEDIA_MONOSCOPIC
                        )
                    )
                    api.launchInVr(intent)
                    api.close()
                } else { // Fall back for devices that don't have Google VR Services. This flow should only
                    // be used for older Cardboard devices.
                    val intent = Intent(intent).setClass(
                        this@VideoPlayerActivity,
                        VrVideoActivity::class.java
                    )
                    intent.removeCategory(Intent.CATEGORY_LAUNCHER)
                    intent.flags = 0 // Clear any flags from the previous intent.
                    startActivity(intent)
                }
                // See VrVideoActivity's launch2dActivity() for more info about why this finish() call
                // may be required.
                finish()
            })

        try {
            videoView?.initialize(videoUi)
        } catch (error: Exception) {
            error.printStackTrace()

            val toast = Toast.makeText(
                applicationContext,
                getString(R.string.vr_video_compatibility_error),
                Toast.LENGTH_LONG
            )
            toast.setMargin(50F, 50F)
            toast.show()
            finish()
        }

        // Boilerplate for checking runtime permissions in Android.
        if (ContextCompat.checkSelfPermission(this, permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            val button = findViewById<View>(R.id.permission_button)
            button.setOnClickListener {
                ActivityCompat.requestPermissions(
                    this@VideoPlayerActivity,
                    arrayOf(permission.READ_EXTERNAL_STORAGE),
                    READ_EXTERNAL_STORAGE_PERMISSION_ID
                )
            }
            // The user can click the button to request permission but we will also click on their behalf
// when the Activity is created.
            button.callOnClick()
        } else { // Permission has already been granted.
            initializeActivity()
        }
    }

    /** Handles the user accepting the permission.  */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        results: IntArray
    ) {
        if (requestCode == READ_EXTERNAL_STORAGE_PERMISSION_ID) {
            if (results.isNotEmpty() && results[0] == PackageManager.PERMISSION_GRANTED) {
                initializeActivity()
            }
        }
    }

    /**
     * Normal apps don't need this. However, since we use adb to interact with this sample, we
     * want any new adb Intents to be routed to the existing Activity rather than launching a new
     * Activity.
     */
    override fun onNewIntent(intent: Intent) { // Save the new Intent which may contain a new Uri. Then tear down & recreate this Activity to
// load that Uri.
        setIntent(intent)
        recreate()
    }

    /** Initializes the Activity only if the permission has been granted.  */
    private fun initializeActivity() {
        val root = findViewById<View>(R.id.activity_root) as ViewGroup
        for (i in 0 until root.childCount) {
            root.getChildAt(i).visibility = View.VISIBLE
        }
        findViewById<View>(R.id.permission_button).visibility = View.GONE
        videoView?.loadMedia(intent)
    }

    override fun onResume() {
        super.onResume()
        videoView?.onResume()
    }

    override fun onPause() { // MonoscopicView is a GLSurfaceView so it needs to pause & resume rendering. It's also
// important to pause MonoscopicView's sensors & the video player.
        videoView?.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        videoView?.destroy()
        super.onDestroy()
    }

    companion object {
        private const val READ_EXTERNAL_STORAGE_PERMISSION_ID = 1
        private const val VIDEO_OBJECT: String = "video_object"

        fun getIntent(context: Context, video: Video): Intent {
            val intent = Intent(context, VideoPlayerActivity::class.java)

            val uri = Uri.parse(video.url)
            val contentResolver = context.contentResolver
            val mime = MimeTypeMap.getSingleton()
            val type = mime.getExtensionFromMimeType(contentResolver.getType(uri))
            intent.setDataAndType(uri, type)
            intent.putExtra(MediaLoader.MEDIA_FORMAT_KEY, Mesh.MEDIA_MONOSCOPIC)
            intent.putExtra(VIDEO_OBJECT, video as? Serializable)
            return intent
        }
    }
}