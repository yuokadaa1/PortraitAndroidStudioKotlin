package com.example.portrait3

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

//こいつの呼ばれ順　getInstance->init->createService

class ApiManager {

    private var apiService: ApiService? = null
    init {
        Log.i("挙動の確認:ApiManager","init")
        createService()
    }

    val service: ApiService get() = apiService!!

    private fun createService() {

        Log.i("挙動の確認:ApiManager","createService")

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

        //これ追加していいのかは不明
        val gson = GsonBuilder().setLenient().create()

        val retrofit: Retrofit = Retrofit.Builder()
            .client(client)
            .baseUrl("https://script.google.com/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            //gson追加なしがコピペ状態
            //.addConverterFactory(GsonConverterFactory.create())
            .build()
        apiService = retrofit.create<ApiService>(ApiService::class.java)
    }

    companion object {
        private var instance: ApiManager? = null
        fun getInstance(): ApiManager {
            return instance ?: synchronized(this) {
                Log.i("挙動の確認:ApiManager","getInstance")
                ApiManager().also { instance = it }
            }
        }
    }

}