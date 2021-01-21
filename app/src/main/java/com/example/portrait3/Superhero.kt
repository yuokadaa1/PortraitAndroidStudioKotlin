package com.example.portrait3

import com.google.gson.annotations.SerializedName

class Superhero {
    @SerializedName("target_Name")
    var targetName: String = ""
    var folderID: String = ""
    var fileURL: String = ""
    var countFile: Int = 0
    var countSubject: Int = 0
}