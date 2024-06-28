package com.unc.gearupvr.components.video_player.rendering

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

import android.opengl.GLES20
import android.opengl.Matrix
import com.google.vr.sdk.controller.Orientation
import com.unc.gearupvr.components.video_player.rendering.Utils.checkGlError

/**
 * Renders a reticle in VR for the Daydream Controller.
 *
 *
 * This is a minimal example that renders a circle at 1 meter from the user based on the rotation
 * of the controller.
 */
internal class Reticle {
    // Program-related GL items. These are only valid if program != 0.
    private var program = 0
    private var mvpMatrixHandle = 0
    private var positionHandle = 0
    // The reticle doesn't have a real modelMatrix. Its distance is baked into the mesh and it
// uses a rotation matrix when rendered.
    private val modelViewProjectionMatrix = FloatArray(16)

    /** Finishes initialization of this object on the GL thread.  */
    fun glInit() {
        if (program != 0) {
            return
        }
        program = Utils.compileProgram(
            vertexShaderCode,
            fragmentShaderCode
        )
        mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMvpMatrix")
        positionHandle = GLES20.glGetAttribLocation(program, "aPosition")
        checkGlError()
    }

    /**
     * Renders the reticle.
     *
     * @param viewProjectionMatrix Scene's view projection matrix.
     * @param orientation Rotation matrix derived from [Orientation.toRotationMatrix].
     */
    fun glDraw(
        viewProjectionMatrix: FloatArray?,
        orientation: FloatArray?
    ) { // Configure shader.
        GLES20.glUseProgram(program)
        checkGlError()
        Matrix.multiplyMM(
            modelViewProjectionMatrix,
            0,
            viewProjectionMatrix,
            0,
            orientation,
            0
        )
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, modelViewProjectionMatrix, 0)
        checkGlError()
        // Render quad.
        GLES20.glEnableVertexAttribArray(positionHandle)
        checkGlError()
        vertexBuffer.position(0)
        GLES20.glVertexAttribPointer(
            positionHandle,
            COORDS_PER_VERTEX,
            GLES20.GL_FLOAT,
            false,
            0,
            vertexBuffer
        )
        checkGlError()
        GLES20.glDrawArrays(
            GLES20.GL_TRIANGLE_STRIP,
            0,
            vertexData.size / COORDS_PER_VERTEX
        )
        checkGlError()
        GLES20.glDisableVertexAttribArray(positionHandle)
    }

    /** Frees GL resources.  */
    fun glShutdown() {
        if (program != 0) {
            GLES20.glDeleteProgram(program)
        }
    }

    companion object {
        // The reticle quad is 2 * SIZE units.
        private const val SIZE = .01f
        private const val DISTANCE = 1f
        // Standard vertex shader.
        private val vertexShaderCode = arrayOf(
            "uniform mat4 uMvpMatrix;",
            "attribute vec3 aPosition;",
            "varying vec2 vCoords;",  // Passthrough normalized vertex coordinates.
            "void main() {",
            "  gl_Position = uMvpMatrix * vec4(aPosition, 1);",
            "  vCoords = aPosition.xy / vec2($SIZE, $SIZE);",
            "}"
        )
        // Procedurally render a ring on the quad between the specified radii.
        private val fragmentShaderCode = arrayOf(
            "precision mediump float;",
            "varying vec2 vCoords;",  // Simple ring shader that is white between the radii and transparent elsewhere.
            "void main() {",
            "  float r = length(vCoords);",  // Blend the edges of the ring at .55 +/- .05 and .85 +/- .05.
            "  float alpha = smoothstep(0.5, 0.6, r) * (1.0 - smoothstep(0.8, 0.9, r));",
            "  if (alpha == 0.0) {",
            "    discard;",
            "  } else {",
            "    gl_FragColor = vec4(alpha);",
            "  }",
            "}"
        )
        // Simple quad mesh.
        private const val COORDS_PER_VERTEX = 3
        private val vertexData = floatArrayOf(
            -SIZE, -SIZE, -DISTANCE,
            SIZE, -SIZE, -DISTANCE,
            -SIZE, SIZE, -DISTANCE,
            SIZE, SIZE, -DISTANCE
        )
        private val vertexBuffer =
            Utils.createBuffer(vertexData)
    }
}