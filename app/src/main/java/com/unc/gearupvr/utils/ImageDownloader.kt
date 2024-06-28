package com.unc.gearupvr.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.unc.gearupvr.model.GearupApp
import java.io.FileNotFoundException
import java.io.IOException

class ImageDownloader {

    companion object {
        fun cacheImage(bitmap: Bitmap, name: String) {
            try {
                val regex = Regex("[^a-zA-Z0-9]")
                val fileName = name.replace(regex, "_")
                (GearupApp.ctx?.openFileOutput(fileName, Context.MODE_PRIVATE))?.let { fos ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                    fos.flush()
                    fos.close()
                }

            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        fun getImage(name: String): Bitmap? {
            val regex = Regex("[^a-zA-Z0-9]")
            val fileName = name.replace(regex, "_")
            try {
                (GearupApp.ctx?.openFileInput(fileName))?.let { input ->
                    val bitmap = BitmapFactory.decodeStream(input)
                    input.close()
                    return bitmap
                }
                return null
            } catch (fe: FileNotFoundException) {
                return null
            }
        }
    }
}