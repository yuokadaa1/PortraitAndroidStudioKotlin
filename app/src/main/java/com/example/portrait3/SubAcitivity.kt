package com.example.portrait3

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.gson.JsonObject
import kotlinx.coroutines.*
import retrofit2.Response


class SubAcitivity :AppCompatActivity(){

    val context = this
    val mBitmapList: MutableList<Bitmap> = ArrayList()
    val TAG = SubAcitivity::class.java.name

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        Log.i(TAG,"故障個所の確認１")
        val mSosotataImageView = SosotataImageView(context)
        setContentView(mSosotataImageView)

        Log.i(TAG,"故障個所の確認２")
        val folderUrl = intent.getStringArrayListExtra("folderUrl")
        Log.i(TAG, folderUrl?.get(0).toString())
        val sendJson = JsonObject()
        sendJson.addProperty("folderID", folderUrl.toString())

        GlobalScope.launch(Dispatchers.Main) {
            Log.i(TAG,"故障個所の確認２．１")
            withContext(Dispatchers.IO){
                for (i in 0..folderUrl!!.size - 1 ) {
                    val url:String = folderUrl.get(i)
                    val bitmap:Bitmap? = getImage(url)
                    mBitmapList.add(bitmap!!)
                }
            }
            Log.i(TAG,"故障個所の確認３")
            mSosotataImageView.setBitmapList(mBitmapList)
        }
    }

    suspend fun getImageURL(sendJson:JsonObject): Response<ResponseData<List<Superhero>>> {
        return ApiManager.getInstance().service.post(sendJson).execute()
    }

    suspend fun getImage(url:String): Bitmap? {
        return Glide.with(context).asBitmap().load(url).submit().get()
    }

}