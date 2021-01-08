package com.example.portrait3

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.InputStream
import java.net.URL


class SubAcitivity :AppCompatActivity(){

    val itemList = ArrayList<Superhero>()

    override fun onCreate(savedInstanceState: Bundle?) {

        val TAG = SubAcitivity::class.java.name
        val folderUrl = intent.getStringExtra("folderUrl")
        Log.i("挙動の確認" + TAG, folderUrl.toString())

        //curl -d '{"folderID":"1o0Rh1OMbcuWGhpWqQzAZ3OD-Gr0aJjIq"}' -L https://script.google.com/macros/s/AKfycbzoAamQ3SjcfleexVsM-6yXZaG5FacymUIL3IhGEMsoNKsir5PV/exec

        val sendJson = JsonObject()
        sendJson.addProperty("folderID", folderUrl.toString())

        // pbLoading.visibility = View.VISIBLE
        ApiManager.getInstance().service.post(sendJson)
                .enqueue(object : Callback<ResponseData<List<Superhero>>> {
                    override fun onResponse(
                            call: Call<ResponseData<List<Superhero>>>,
                            response: Response<ResponseData<List<Superhero>>>
                    ) {
                        val listData: List<Superhero> = response.body()!!.data
                        itemList.clear()
                        itemList.addAll(listData)
                        // pbLoading.visibility = View.GONE
                    }

                    override fun onFailure(call: Call<ResponseData<List<Superhero>>>, t: Throwable) {
                        Log.e(TAG, "Error on loading data")
                        // pbLoading.visibility = View.GONE
                    }
                })


        var mBitmapList: MutableList<Bitmap> = ArrayList()

        for (i in 0..itemList.size) {
            Log.i(TAG,itemList.get(i).photo)
            val tButtonUrl:URL =  URL(itemList.get(i).photo)
            // inputStreamで画像を読み込む
            val tIstream: InputStream = tButtonUrl.openStream()
            // inputSteramをbitmapに変換
            val mBitmap = BitmapFactory.decodeStream(tIstream)
            mBitmapList.add(BitmapFactory.decodeStream(tIstream))
        }

        super.onCreate(savedInstanceState)
        var mSosotataImageView = SosotataImageView(this)

        // mBitmapList.add(BitmapFactory.decodeResource(this.resources, R.drawable.android_robot))
        // mBitmapList.add(BitmapFactory.decodeResource(this.resources, R.drawable.android_robot2))
        mSosotataImageView.setBitmapList(mBitmapList)

        setContentView(mSosotataImageView)
    }

}