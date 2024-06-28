package com.unc.gearupvr.service

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.gson.Gson
import com.unc.gearupvr.BuildConfig
import com.unc.gearupvr.model.GearupApp
import com.unc.gearupvr.model.MenuItem
import com.unc.gearupvr.utils.ImageDownloader
import org.jetbrains.anko.runOnUiThread


object MenuConfigurationService {
    fun fetchMenuConfiguration(completionHandler: ((List<MenuItem>?, Error?) -> Unit)) {
        val request = ApiRequest(API.MenuConfiguration)
        request.invoke { result, error, _ ->
            var menuItems: List<MenuItem>? = null
            val err: Error?
            when {
                result != null -> {
                    try {
                        println(result)
                        menuItems =
                            Gson().fromJson(result.toString(), Array<MenuItem>::class.java).toList()
                        fetchTabIcons(menuItems, 0, null, completionHandler)

                    } catch (e: Exception) {
                        err = java.lang.Error(e.localizedMessage)
                        completionHandler(menuItems, err)
                    }

                }
                error != null -> {//Placeholder for generating custom error message for UI
                    err = error
                    completionHandler(menuItems, err)
                }
                else -> {
                    err =
                        Error("The server encountered an unexpected condition that prevented it from fulfilling the request.")
                    completionHandler(menuItems, err)
                }
            }


        }
    }


    private fun fetchTabIcons(
        menuItems: List<MenuItem>?,
        downloadIndex: Int,
        error: Error?,
        completionHandler: ((List<MenuItem>?, Error?) -> Unit)
    ) {

        Log.e("Glide downloadIndex:", "" + downloadIndex)

        GearupApp.ctx?.runOnUiThread {
            if (downloadIndex >= menuItems?.size ?: 0) {
                completionHandler(menuItems, error)
            }
        }

        if (menuItems != null && menuItems.size > downloadIndex) {
            try {
                Glide.with(GearupApp?.ctx ?: return)
                    .asBitmap()
                    .load(
                        "https://" + BuildConfig.API_BASE + "/" + (menuItems[downloadIndex].icon)
                    )
                    .into(object : CustomTarget<Bitmap?>() {
                        override fun onResourceReady(
                            bitmap: Bitmap,
                            transition: Transition<in Bitmap?>?
                        ) {
                            if (bitmap == null) {
                                println("Bitmap data source returned success, but bitmap null.")
                                fetchTabIcons(
                                    menuItems,
                                    downloadIndex + 1,
                                    Error("Bitmap data source returned success, but bitmap null."),
                                    completionHandler
                                )
                                return
                            }

                            menuItems[downloadIndex].icon?.let {
                                val scaledBitmap =
                                    Bitmap.createScaledBitmap(bitmap, 24, 24, false)
                                ImageDownloader.cacheImage(scaledBitmap, it)
                            }


                            fetchTabIcons(
                                menuItems,
                                downloadIndex + 1,
                                error,
                                completionHandler
                            )
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {}
                        override fun onLoadFailed(errorDrawable: Drawable?) {
                            println("Bitmap data source returned failed")
                            fetchTabIcons(
                                menuItems,
                                downloadIndex + 1,
                                Error("Bitmap data source returned success, but bitmap null."),
                                completionHandler
                            )
                            return
                        }
                    })


            } catch (err: Exception) {
                println(err.localizedMessage)
                fetchTabIcons(
                    menuItems,
                    downloadIndex + 1,
                    Error(err.localizedMessage),
                    completionHandler
                )
            }


        }
    }


}