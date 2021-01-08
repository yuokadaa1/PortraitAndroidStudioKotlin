package com.example.portrait3

import com.google.gson.JsonObject
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiService {
//    @GET("marvel-heroes")
    @GET("macros/s/AKfycbzoAamQ3SjcfleexVsM-6yXZaG5FacymUIL3IhGEMsoNKsir5PV/exec")
    fun listHeroes(): Call<ResponseData<List<Superhero>>>

    @Headers("Content-Type: application/json")
    @POST("macros/s/AKfycbzoAamQ3SjcfleexVsM-6yXZaG5FacymUIL3IhGEMsoNKsir5PV/exec")
    fun post(@Body sJson:JsonObject): Call<ResponseData<List<Superhero>>>

}