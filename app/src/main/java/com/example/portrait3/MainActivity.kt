package com.example.portrait3

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    val TAG = MainActivity::class.java.name
    val itemList = ArrayList<Superhero>()
    val itemList2 = ArrayList<Superhero>()
    lateinit var adapter: SuperheroAdapter
    // lateinit var listData: List<Superhero>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    // }
    //
    // override fun onStart() {
    //
    //     super.onStart()

        adapter = SuperheroAdapter()
        adapter.setOnItemClickListener(object:SuperheroAdapter.OnItemClickListener{
            override fun onItemClickListener(view: View, position: Int, clickedText: String) {

                val intent = Intent(this@MainActivity, SubAcitivity::class.java)
                val sendJson = JsonObject()
                sendJson.addProperty("folderID", clickedText)
                Log.i("onCreate","clickedText:" + clickedText)

                GlobalScope.launch(Dispatchers.Main) {
                    var response: Response<ResponseData<List<Superhero>>>
                    response = withContext(Dispatchers.Default) {
                        getImageURL(sendJson)
                    }
                    val listData: List<Superhero> = response.body()!!.data
                    itemList2.clear()
                    itemList2.addAll(listData)
                    val aaa: MutableList<String> = ArrayList()
                    itemList2.forEach{ i ->
                        aaa.add(i.fileURL)
                    }
                    intent.putStringArrayListExtra("folderUrl", aaa as ArrayList<String>)
                    startActivity(intent)
                }

            }
        })

        rvData.adapter = adapter
        loadData()

    }

    private fun loadData() {
        // show loading progress bar
        pbLoading.visibility = View.VISIBLE

        ApiManager.getInstance().service.listHeroes()
            .enqueue(object : Callback<ResponseData<List<Superhero>>> {
                override fun onResponse(
                    call: Call<ResponseData<List<Superhero>>>,
                    response: Response<ResponseData<List<Superhero>>>
                ) {
                    val listData: List<Superhero> = response.body()!!.data
                    // updating data from network to adapter
                    itemList.clear()
                    itemList.addAll(listData)
                    adapter.updateData(itemList)
                    // hide loading progress bar
                    pbLoading.visibility = View.GONE
                }
                override fun onFailure(call: Call<ResponseData<List<Superhero>>>, t: Throwable) {
                    // if there is error while get data from network
                    Log.e(TAG, "Error on loading data")
                    pbLoading.visibility = View.GONE
                }
            })
    }

    suspend fun getImageURL(sendJson: JsonObject): Response<ResponseData<List<Superhero>>> {
        return ApiManager.getInstance().service.post(sendJson).execute()
    }

}
