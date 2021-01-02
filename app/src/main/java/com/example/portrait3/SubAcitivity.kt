package com.example.portrait3

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


class SubAcitivity :AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        //↓こっちじゃなくていいのか？
        //val row:MutableList<Bitmap> = mutableListOf()
        var mBitmapList: MutableList<Bitmap> = ArrayList()

        super.onCreate(savedInstanceState)
        var mSosotataImageView = SosotataImageView(this)

        mBitmapList.add(BitmapFactory.decodeResource(this.resources, R.drawable.android_robot))
        mBitmapList.add(BitmapFactory.decodeResource(this.resources, R.drawable.android_robot2))
        mSosotataImageView.setBitmapList(mBitmapList)

        setContentView(mSosotataImageView)
    }

}