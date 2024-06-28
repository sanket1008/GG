package com.unc.gearupvr.service


import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri.Builder
import android.os.Build
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.unc.gearupvr.BuildConfig
import com.unc.gearupvr.components.AppErrorActivity
import com.unc.gearupvr.components.NetworkErrorActivity
import com.unc.gearupvr.model.GearupApp
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLHandshakeException


class ApiRequest(
    private var api: API,
    params: Any? = null,
    pathComp: ArrayList<String?>? = null,
    private var additionalHeaders: HashMap<String, String>? = null
) {

    companion object {
        const val HTTP_STATUS: String = "Status"
    }

    private var url: URL
    private var requestBody: String = ""
    private val isNetworkConnected: Boolean
        get() {
            var result = false
            val connectivityManager =
                GearupApp.ctx?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val networkCapabilities = connectivityManager.activeNetwork ?: return false
                val actNw =
                    connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
                result = actNw.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)


            } else {
                connectivityManager.run {
                    connectivityManager.activeNetworkInfo?.run {
                        result = when (type) {
                            ConnectivityManager.TYPE_WIFI -> true
                            ConnectivityManager.TYPE_MOBILE -> true
                            ConnectivityManager.TYPE_ETHERNET -> true
                            else -> false
                        }

                    }
                }
            }

            return result
        }

    init {
        val urlBuilder = Builder()
        urlBuilder.scheme("https")
            .authority(BuildConfig.API_BASE)
            .path(api.path)
        if (pathComp != null) {
            for (path in pathComp) {
                urlBuilder.appendPath(path)
            }
        }
        if (params != null) {
            when (api.httpMethod) {
                HttpMethod.GET, HttpMethod.DELETE -> {
                    (params as? HashMap<*, *>)?.let { paramsHashMap ->
                        for ((key, value) in paramsHashMap) {
                            (key as? String).let { keyString ->
                                if (value is String) {
                                    urlBuilder.appendQueryParameter(keyString, value)
                                } else (value as? ArrayList<*>)?.let { values ->
                                    for (v in values) {
                                        (v as? String).let { valueString ->
                                            urlBuilder.appendQueryParameter(keyString, valueString)
                                        }
                                    }
                                }
                            }

                        }
                    }
                }
                HttpMethod.PUT, HttpMethod.POST -> {
                    this.requestBody = Gson().toJson(params)
                    Log.e("Params ", "" + this.requestBody)
                }
            }
        }
        url = URL(urlBuilder.build().toString())
    }

    fun invoke(completionHandler: ((Any?, Error?, Map<String, List<String>>?) -> Unit)? = null) {

        doAsync {
            if (isNetworkConnected) {

                val urlConnection = (url.openConnection() as HttpsURLConnection).apply {
                    hostnameVerifier = HostnameVerifier { _, _ -> true }
                    connectTimeout = 300000
                    addRequestProperty("Accept", "application/json")
                    addRequestProperty("Content-Type", "application/json")
                }

                urlConnection.requestMethod = api.httpMethod.toString()
                if (additionalHeaders != null) {
                    for ((key, value) in additionalHeaders ?: return@doAsync) {
                        urlConnection.addRequestProperty(key, value)
                    }
                }

                if (api.isSecured) {
                    val sharedPref =
                        GearupApp.ctx?.getSharedPreferences(
                            GearupApp.SHARED_PREFERENCES,
                            Context.MODE_PRIVATE
                        )
                    val accessToken = sharedPref?.getString("accessToken", null)

                    if (accessToken != null && accessToken.isNotEmpty()) {
                        // api is secured and access token is available
                        urlConnection.addRequestProperty("Authorization", "Bearer $accessToken")
                        openConnection(urlConnection, completionHandler)

                    } else {

                        // api is secured and access token is not available
                        authenticate { result, error, map ->
                            if (result is String) {
                                //access token fetched successfully
                                val editor = sharedPref?.edit()
                                editor?.putString("accessToken", result)
                                editor?.apply()

                                urlConnection.addRequestProperty("Authorization", "Bearer $result")
                                openConnection(urlConnection, completionHandler)

                            } else {
                                // failed to fetched access token
                                uiThread {
                                    if (completionHandler != null) {
                                        completionHandler(result, error, map)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // api is not secured
                    openConnection(urlConnection, completionHandler)
                }
            } else {
                val error = Error("NO INTERNET")
                uiThread {
                    if (completionHandler != null) {
                        completionHandler(null, error, null)
                    }
                    (GearupApp.ctx)?.let { ctx ->
                        ctx.startActivity(
                            NetworkErrorActivity.createIntent(ctx)
                        )
                    }
                }
            }

        }

    }

    private fun openConnection(
        urlConnection: HttpsURLConnection,
        completionHandler: ((Any?, Error?, Map<String, List<String>>?) -> Unit)? = null
    ) {
        println(urlConnection.toString())
        doAsync {
            try {
                if (api.httpMethod == HttpMethod.POST || api.httpMethod == HttpMethod.PUT) {
                    urlConnection.doOutput = true
                    urlConnection.setChunkedStreamingMode(0)

                    (urlConnection.outputStream)?.let { outputStream ->
                        val out: OutputStream = BufferedOutputStream(outputStream)
                        out.write(requestBody.toByteArray(Charsets.UTF_8))
                        out.flush()
                    }
                }

                val status = urlConnection.responseCode
                val header = urlConnection.headerFields.toMutableMap()
                header[HTTP_STATUS] = listOf(status.toString())


                if (status == HttpsURLConnection.HTTP_OK || status == HttpsURLConnection.HTTP_CREATED) {
                    val inputStream = BufferedInputStream(urlConnection.inputStream)
                    val stringBuffer = StringBuffer()
                    inputStream.bufferedReader().forEachLine { stringBuffer.append(it) }
                    val result = stringBuffer.toString()
                    urlConnection.disconnect()
                    uiThread {
                        if (completionHandler != null) {
                            completionHandler(result, null, header)
                        }
                    }
                } else if ((status == HttpsURLConnection.HTTP_UNAUTHORIZED ||
                            status == HttpsURLConnection.HTTP_FORBIDDEN)
                    && api.isSecured
                ) {

                    urlConnection.disconnect()
                    val sharedPref =
                        GearupApp.ctx?.getSharedPreferences(
                            GearupApp.SHARED_PREFERENCES,
                            Context.MODE_PRIVATE
                        )
                    val editor = sharedPref?.edit()?.apply { putString("accessToken", "") }

                    authenticate { result, error, map ->
                        if (result is String) {
                            //access token fetched successfully
                            editor?.putString("accessToken", result)
                            editor?.apply()

                            //retry reconnect
                            invoke(completionHandler)
                        } else {
                            // failed to fetched access token
                            uiThread {
                                if (completionHandler != null) {
                                    completionHandler(result, error, map)
                                }
                            }
                        }
                    }

                } else {
                    val errorStream = urlConnection.errorStream
                    val error = if (errorStream == null) {
                        Error("Unknown API Error")
                    } else {
                        val stringBuffer = StringBuffer()
                        errorStream.bufferedReader().forEachLine { stringBuffer.append(it) }
                        val errorString = stringBuffer.toString()
                        Error(errorString)
                    }
                    urlConnection.disconnect()
                    uiThread {
                        if (completionHandler != null) {
                            completionHandler(null, error, header)
                        }
                    }
                }

            } catch (e: Exception) {
                if (e is SSLHandshakeException) {
                    (GearupApp.ctx)?.let { ctx ->
                        ctx.startActivity(
                            AppErrorActivity.createIntent(
                                context = ctx,
                                pageType = GearupApp.SSL_ERROR_CODE
                            )
                        )
                    }
                }
                e.printStackTrace() //TODO: create Error object from  Exception e adn pass to completion handler
                urlConnection.disconnect()
                uiThread {
                    if (completionHandler != null) {
                        completionHandler(null, Error("Unknown Error"), null)
                    }
                }
            }

        }

    }

    private fun authenticate(completionHandler: ((Any?, Error?, Map<String, List<String>>?) -> Unit)? = null) {
        val params: HashMap<String, String?> =
            hashMapOf(
                "username" to GearupApp.USERNAME[BuildConfig.FLAVOR],
                "password" to GearupApp.PASSWORD[BuildConfig.FLAVOR]
            )
        val request = ApiRequest(API.Login, params)
        request.invoke { result, error, header ->
            var access: String? = null
            var err: Error? = error
            val status = header?.get(HTTP_STATUS)?.first()?.toInt() ?: -1

            if (error == null) {
                (result as? String).let { jsonString ->
                    try {
                        val resultMap: HashMap<String, Any> = Gson().fromJson(
                            jsonString,
                            object : TypeToken<HashMap<String, Any>>() {}.type
                        )
                        access = resultMap["access"] as? String
                    } catch (e: Exception) {
                        e.printStackTrace()
                        err = Error("Unable to retrieve token")
                    }
                }
            } else if (status == HttpURLConnection.HTTP_NOT_FOUND ||
                status == HttpURLConnection.HTTP_FORBIDDEN ||
                status == HttpURLConnection.HTTP_UNAUTHORIZED
            ) {
                (GearupApp.ctx)?.let { ctx ->
                    ctx.startActivity(
                        AppErrorActivity.createIntent(
                            context = ctx,
                            pageType = status
                        )
                    )
                }
            }
            if (completionHandler != null)
                completionHandler(access, err, null)
        }
    }


}



