package com.wrongcode.captionwizard.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wrongcode.captionwizard.data.database.entities.SubtitleEntity
import com.wrongcode.captionwizard.models.Subtitle
import kotlinx.coroutines.flow.Flow

@Dao
interface SubtitleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtitle(subtitleEntity: SubtitleEntity)

    @Query("SELECT * FROM video_table")
    fun readAllSubtitleVideos() : Flow<List<SubtitleEntity>>

    @Query("SELECT * FROM video_table WHERE videoId = :videoId")
    fun readSubtitleByVideoId(videoId : String) : Flow<List<SubtitleEntity>>

    @Query("UPDATE video_table SET subtitleList = :updatedSubtitleList WHERE videoId = :videoId")
    suspend fun updateSubtitleList(videoId: String, updatedSubtitleList: ArrayList<Subtitle>)

    @Delete
    suspend fun deleteSubtitleVideo(subtitleEntity: SubtitleEntity)
}