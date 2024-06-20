package com.wrongcode.captionwizard.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wrongcode.captionwizard.constants.Constants.Companion.TABLE_NAME
import com.wrongcode.captionwizard.models.Subtitle
import com.wrongcode.captionwizard.models.SubtitleList

@Entity(tableName = TABLE_NAME)
class SubtitleEntity(
    @PrimaryKey
    val videoId : String,
    val subtitleList: ArrayList<Subtitle>
)