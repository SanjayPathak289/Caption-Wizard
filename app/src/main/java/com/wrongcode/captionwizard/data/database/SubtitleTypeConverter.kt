package com.wrongcode.captionwizard.data.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wrongcode.captionwizard.models.Subtitle
import com.wrongcode.captionwizard.models.SubtitleList

class SubtitleTypeConverter {
    private val gson = Gson()

    @TypeConverter
    fun subtitleListToString(subtitleList: ArrayList<Subtitle>) : String{
        return gson.toJson(subtitleList)
    }

    @TypeConverter
    fun stringToSubtitleList(data: String) : ArrayList<Subtitle>{
        val listType = object : TypeToken<ArrayList<Subtitle>>(){}.type
        return gson.fromJson(data,listType)
    }
}