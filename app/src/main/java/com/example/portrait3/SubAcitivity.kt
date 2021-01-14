package com.example.portrait3

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.FutureTarget
import com.google.gson.JsonObject
import kotlinx.coroutines.*
import okhttp3.internal.concurrent.Task
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SubAcitivity :AppCompatActivity(){

    val itemList = ArrayList<Superhero>()
    val context = this
    val mBitmapList: MutableList<Bitmap> = ArrayList()
    val TAG = SubAcitivity::class.java.name

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        val mSosotataImageView = SosotataImageView(context)
        setContentView(mSosotataImageView)

        val folderUrl = intent.getStringArrayListExtra("folderUrl")
        Log.i(TAG, folderUrl?.get(0).toString())
        val sendJson = JsonObject()
        sendJson.addProperty("folderID", folderUrl.toString())

        GlobalScope.launch {
            for (i in 0..folderUrl!!.size - 1 ) {
                val url:String = folderUrl.get(i)
                Log.i(TAG,"url:" + url)
                val bitmap:Bitmap? = getImage(url)
                mBitmapList.add(bitmap!!)
            }
            Log.i(TAG,"mBitmapList:" + mBitmapList.size)
            mSosotataImageView.setBitmapList(mBitmapList)

        }


        //ここの塊は動きはする（挙動は謎だが）
        // val futureTarget: FutureTarget<Bitmap> = Glide.with(context)
        //     .asBitmap()
        //     .load("https://drive.google.com/uc?export=view&id=1jLHvYHWBLs6lfcngkaPGCIHpSc9jFyMD&=sharing")
        //     // .load(itemList.get(0).photo)
        //     .submit()
        // lateinit var bitmap2: Bitmap
        // runBlocking {
        //     bitmap2 = futureTarget.get()
        // }
        // mBitmapList.add(bitmap2)
        // mSosotataImageView.setBitmapList(mBitmapList)

        //curl -d '{"folderID":"1o0Rh1OMbcuWGhpWqQzAZ3OD-Gr0aJjIq"}' -L https://script.google.com/macros/s/AKfycbzoAamQ3SjcfleexVsM-6yXZaG5FacymUIL3IhGEMsoNKsir5PV/exec
        // pbLoading.visibility = View.VISIBLE





        // var response: Response<ResponseData<List<Superhero>>>
        // GlobalScope.launch {
        //     response = getImageURL(sendJson)
        //     val listData: List<Superhero> = response.body()!!.data
        //     itemList.clear()
        //     itemList.addAll(listData)
        //
        //     for (i in 0..itemList.size - 1 ) {
        //         val url:String = itemList.get(i).photo
        //         Log.i(TAG,"url:" + url)
        //         val bitmap:Bitmap? = getImage(url)
        //         mBitmapList.add(bitmap!!)
        //     }
        //     Log.i(TAG,"mBitmapList:" + mBitmapList.size)
        //     mSosotataImageView.setBitmapList(mBitmapList)
        //
        // }
        // Log.i(TAG,"set,mBitmapList:" + mBitmapList.size)





        // lateinit var bitmap: Bitmap
        // runBlocking {
        //     for (i in 0..itemList.size - 1 ) {
        //         val url:String = itemList.get(i).photo
        //         Log.i(TAG,"url:" + url)
        //         mBitmapList.add(Glide.with(context).asBitmap().load(url).submit().get())
        //     }
        // }
        // val mSosotataImageView = SosotataImageView(context)
        // setContentView(mSosotataImageView)
        // Log.i(TAG,"mBitmapList:" + mBitmapList.size)
        // mSosotataImageView.setBitmapList(mBitmapList)


        // ApiManager.getInstance().service.post(sendJson)
        //     .enqueue(object : Callback<ResponseData<List<Superhero>>> {
        //         override fun onResponse(
        //             call: Call<ResponseData<List<Superhero>>>,
        //             response: Response<ResponseData<List<Superhero>>>
        //         ) {
        //             Log.i(TAG, "coroutine")
        //             val listData: List<Superhero> = response.body()!!.data
        //             itemList.clear()
        //             itemList.addAll(listData)
        //
        //             lateinit var bitmap: Bitmap
        //             GlobalScope.launch(Dispatchers.Default) {
        //                 for (i in 0..itemList.size - 1 ) {
        //                     val url:String = itemList.get(i).photo
        //                     Log.i(TAG,"url:" + url)
        //                     mBitmapList.add(Glide.with(context).asBitmap().load(url).submit().get())
        //                 }
        //             }
        //
        //             // runBlocking {
        //             //     GlobalScope.launch(Dispatchers.Default) {
        //             //         for (i in 0..itemList.size - 1 ) {
        //             //             val url:String = itemList.get(i).photo
        //             //             Log.i(TAG,"url:" + url)
        //             //             bitmap = Glide.with(context).asBitmap().load(url).submit().get()
        //             //             mBitmapList.add(bitmap)
        //             //         }
        //             //     }
        //             //     Log.i(TAG,"runBlockingの終了")
        //             // }
        //
        //             val mSosotataImageView = SosotataImageView(context)
        //             setContentView(mSosotataImageView)
        //             Log.i(TAG,"mBitmapList:" + mBitmapList.size)
        //             mSosotataImageView.setBitmapList(mBitmapList)
        //
        //
        //             // pbLoading.visibility = View.GONE
        //         }
        //
        //         override fun onFailure(
        //             call: Call<ResponseData<List<Superhero>>>,
        //             t: Throwable
        //         ) {
        //             Log.e(TAG, "Error on loading data")
        //             // pbLoading.visibility = View.GONE
        //         }
        //     })


//        super.onCreate(savedInstanceState)
//        var mSosotataImageView = SosotataImageView(this)
//        setContentView(mSosotataImageView)

//        mBitmapList.add(BitmapFactory.decodeResource(this.resources, R.drawable.android_robot))
//        mBitmapList.add(BitmapFactory.decodeResource(this.resources, R.drawable.android_robot2))
//        mSosotataImageView.setBitmapList(mBitmapList)


    }

    suspend fun getImageURL(sendJson:JsonObject): Response<ResponseData<List<Superhero>>> {
        return ApiManager.getInstance().service.post(sendJson).execute()
    }

    suspend fun getImage(url:String): Bitmap? {
        return Glide.with(context).asBitmap().load(url).submit().get()
    }

}