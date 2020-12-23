package com.example.portrait3

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ApiManager {

    private var apiService: ApiService? = null
    init {
        createService()
    }

    val service: ApiService get() = apiService!!

    private fun createService() {

        val loggingInterceptor =
            HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
                override fun log(message: String) {
                    Log.i("Retrofit", message)
                }
            })

        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        val client = OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .build()

        val retrofit: Retrofit = Retrofit.Builder()
            .client(client)
//            .baseUrl("https://thesimplycoder.herokuapp.com/")
//            .baseUrl("https://script.google.com/macros/s/AKfycbzoAamQ3SjcfleexVsM-6yXZaG5FacymUIL3IhGEMsoNKsir5PV/exec")
            .baseUrl("https://script.google.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        apiService = retrofit.create<ApiService>(ApiService::class.java)
    }

    companion object {
        private var instance: ApiManager? = null
        fun getInstance(): ApiManager {
            return instance ?: synchronized(this) {
                ApiManager().also { instance = it }
            }
        }
    }

}