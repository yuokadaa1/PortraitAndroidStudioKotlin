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
            listener.onItemClickListener(it, position, itemList.get(position).toString())
        }

    }

    fun updateData(list: List<Superhero>) {
        itemList = list;
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind() {
            val item = itemList.get(adapterPosition)
            itemView.tvName.text = item.name
            itemView.tvSuperheroName.text = item.superheroName
            Glide.with(context)
                .load(item.photo)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .circleCrop()
                .into(itemView.ivPhoto)
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