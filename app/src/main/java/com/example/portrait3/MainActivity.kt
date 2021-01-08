package com.example.portrait3

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    val TAG = MainActivity::class.java.name
    val itemList = ArrayList<Superhero>()
    lateinit var adapter: SuperheroAdapter

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // initialize adapter
        adapter = SuperheroAdapter()

        adapter.setOnItemClickListener(object:SuperheroAdapter.OnItemClickListener{
            override fun onItemClickListener(view: View, position: Int, clickedText: String) {
                Log.i("挙動の確認:MainActivity","Toastが出るはず")

                val intent = Intent(this@MainActivity, SubAcitivity::class.java)
                intent.putExtra("folderUrl", clickedText)
                startActivity(intent)
            }
        })

        rvData.adapter = adapter
        // call api to get the data from network

        loadData()

    }

    private fun loadData() {
        // show loading progress bar
        pbLoading.visibility = View.VISIBLE
        Log.i("挙動の確認:MainActivity","loadData")

        ApiManager.getInstance().service.listHeroes()
            .enqueue(object : Callback<ResponseData<List<Superhero>>> {
                override fun onResponse(
                    call: Call<ResponseData<List<Superhero>>>,
                    response: Response<ResponseData<List<Superhero>>>
                ) {
                    Log.i("挙動の確認:MainActivity","listDataの上")

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
}