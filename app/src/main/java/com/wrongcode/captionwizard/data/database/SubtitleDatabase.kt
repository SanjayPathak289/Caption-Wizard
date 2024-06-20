package com.wrongcode.captionwizard.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.wrongcode.captionwizard.data.database.entities.SubtitleEntity

@Database(entities = [SubtitleEntity::class], version = 1)
@TypeConverters(SubtitleTypeConverter::class)
abstract class SubtitleDatabase : RoomDatabase() {
    abstract fun subtitleDao() : SubtitleDao
}