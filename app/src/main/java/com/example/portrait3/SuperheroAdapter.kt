package com.example.portrait3

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlinx.android.synthetic.main.item_superhero.view.*


class SuperheroAdapter : RecyclerView.Adapter<SuperheroAdapter.ViewHolder>() {

    private lateinit var itemList: List<Superhero>
    lateinit var context: Context

    // リスナー格納変数
    lateinit var listener: OnItemClickListener

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.item_superhero, parent, false)


        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return if (::itemList.isInitialized) itemList.size else 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()

        holder.itemView.setOnClickListener {
            listener.onItemClickListener(it, position, itemList.get(position).folderID)
        }

    }

    fun updateData(list: List<Superhero>) {
        itemList = list
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind() {
            val item = itemList.get(adapterPosition)
            val fileNameArray = item.targetName.split("_")
            itemView.tvtargetName.text = fileNameArray[1] + " Part" + item.countSubject
            itemView.tvMakeY4MD.text = "更新日：" + fileNameArray[0].substring(0,4) + "/" + fileNameArray[0].substring(4,6) + "/" + fileNameArray[0].substring(6,8)
            itemView.tvFileCount.text = "写真数：" + item.countFile.toString()

            Glide.with(context)
                .load(item.fileURL)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .circleCrop()
                .into(itemView.ivfileURL)
        }

    }

    //インターフェースの作成
    interface OnItemClickListener{
        fun onItemClickListener(view: View, position: Int, clickedText: String)
    }

    // リスナー
    fun setOnItemClickListener(listener: OnItemClickListener){
        this.listener = listener
    }

}