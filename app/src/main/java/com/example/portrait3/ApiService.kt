package com.example.portrait3

import retrofit2.Call
import retrofit2.http.GET

interface ApiService {
//    @GET("marvel-heroes")
    @GET("macros/s/AKfycbzoAamQ3SjcfleexVsM-6yXZaG5FacymUIL3IhGEMsoNKsir5PV/exec")
    fun listHeroes(): Call<ResponseData<List<Superhero>>>
}