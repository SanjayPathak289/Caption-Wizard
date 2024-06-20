package com.wrongcode.captionwizard.data

import com.wrongcode.captionwizard.data.database.SubtitleDao
import com.wrongcode.captionwizard.data.database.entities.SubtitleEntity
import com.wrongcode.captionwizard.models.Subtitle
import com.wrongcode.captionwizard.models.SubtitleList
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LocalDataSource @Inject constructor(
    private val subtitleDao: SubtitleDao
) {
    suspend fun insertSubtitle(subtitleEntity: SubtitleEntity){
        subtitleDao.insertSubtitle(subtitleEntity)
    }
    fun readAllSubtitleVideos() : Flow<List<SubtitleEntity>>{
        return subtitleDao.readAllSubtitleVideos()
    }

    fun readSubtitleByVideoId(videoId : String) : Flow<List<SubtitleEntity>>{
        return subtitleDao.readSubtitleByVideoId(videoId)
    }
    suspend fun updateSubtitleList(videoId: String, updatedSubtitleList: ArrayList<Subtitle>){
        return subtitleDao.updateSubtitleList(videoId,updatedSubtitleList)
    }

    suspend fun deleteSubtitleVideo(subtitleEntity: SubtitleEntity){
        return subtitleDao.deleteSubtitleVideo(subtitleEntity)
    }
}