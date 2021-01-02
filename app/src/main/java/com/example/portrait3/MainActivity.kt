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
                Log.i("aaa","Toastが出るはず")
                //理由はわからんがtoastはされない。Logは出力されるので検知はされている模様。ヨシ。とする。
                //Toast.makeText(applicationContext, "${clickedText}がタップされました", Toast.LENGTH_LONG).show()
                //3.Intentクラスのオブジェクトを生成。
                val intent = Intent(this@MainActivity, SubAcitivity::class.java)
                //生成したオブジェクトを引数に画面を起動！
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
}