package com.example.cinderssoul.network

import android.os.Build
import com.example.cinderssoul.BuildConfig
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

object ApiClient {
    @Volatile
    private var accessToken: String? = null

    fun setAccessToken(token: String?) {
        accessToken = token
    }

    private val baseUrlCandidates: List<HttpUrl> by lazy { buildBaseUrlCandidates() }

    @Volatile
    private var activeBaseUrl: HttpUrl = baseUrlCandidates.first()

    val baseUrl: String
        get() = activeBaseUrl.toString()

    val shareBaseUrl: String
        get() = BuildConfig.SHARE_BASE_URL.takeIf { it.isNotBlank() } ?: baseUrl

    fun normalizeBackendUrl(url: String?): String? {
        if (url.isNullOrBlank()) return null
        val trimmed = url.trim()
        val baseRoot = baseUrl.trim().trimEnd('/')
        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            return if (trimmed.startsWith("/")) baseRoot + trimmed else trimmed
        }
        val candidates = listOf(
            "http://localhost:3000",
            "http://127.0.0.1:3000",
            "http://10.0.2.2:3000",
            "http://10.0.3.2:3000",
            "http://10.0.0.2:3000"
        )
        val matched = candidates.firstOrNull { trimmed.startsWith(it) } ?: return trimmed
        return baseRoot + trimmed.removePrefix(matched)
    }

    private fun buildBaseUrlCandidates(): List<HttpUrl> {
        val customBaseUrl = normalizeBaseUrl(BuildConfig.BASE_URL)
        val preferred = if (isProbablyEmulator()) {
            listOf("http://10.0.2.2:3000/", "http://10.0.3.2:3000/")
        } else {
            listOf("http://localhost:3000/")
        }
        val candidates = (preferred + listOfNotNull(customBaseUrl)).distinct()
        return candidates.map { it.toHttpUrl() }
    }

    private fun normalizeBaseUrl(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val trimmed = raw.trim()
        val withScheme = if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            trimmed
        } else {
            "http://$trimmed"
        }
        return if (withScheme.endsWith("/")) withScheme else "$withScheme/"
    }

    private fun orderedBaseUrls(): List<HttpUrl> {
        val current = activeBaseUrl
        val candidates = baseUrlCandidates
        return if (candidates.first() == current) {
            candidates
        } else {
            listOf(current) + candidates.filterNot { it == current }
        }
    }

    private fun isProbablyEmulator(): Boolean {
        return (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
            || Build.FINGERPRINT.startsWith("generic")
            || Build.FINGERPRINT.startsWith("unknown")
            || Build.HARDWARE.contains("goldfish")
            || Build.HARDWARE.contains("ranchu")
            || Build.MODEL.contains("google_sdk")
            || Build.MODEL.contains("Emulator")
            || Build.MODEL.contains("Android SDK built for x86")
            || Build.MANUFACTURER.contains("Genymotion")
            || Build.PRODUCT.contains("sdk_google")
            || Build.PRODUCT.contains("google_sdk")
            || Build.PRODUCT.contains("sdk")
            || Build.PRODUCT.contains("sdk_x86")
            || Build.PRODUCT.contains("vbox86p")
            || Build.PRODUCT.contains("emulator")
            || Build.PRODUCT.contains("simulator")
    }

    private class AuthInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val requestBuilder = chain.request().newBuilder()
            accessToken?.takeIf { it.isNotBlank() }?.let { token ->
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }
            return chain.proceed(requestBuilder.build())
        }
    }

    private val baseUrlInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        var lastException: IOException? = null
        for (candidate in orderedBaseUrls()) {
            val updatedUrl = originalRequest.url.newBuilder()
                .scheme(candidate.scheme)
                .host(candidate.host)
                .port(candidate.port)
                .build()
            val updatedRequest = originalRequest.newBuilder().url(updatedUrl).build()
            try {
                val response = chain.proceed(updatedRequest)
                activeBaseUrl = candidate
                return@Interceptor response
            } catch (exception: IOException) {
                lastException = exception
            }
        }
        throw lastException ?: IOException("Unable to connect to the backend.")
    }

    private val httpClient: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(baseUrlInterceptor)
            .addInterceptor(AuthInterceptor())

        if (BuildConfig.DEBUG) {
            builder.addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                }
            )
        }

        builder.build()
    }

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl) // <-- SỬ DỤNG URL ĐỘNG TẠI ĐÂY
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
