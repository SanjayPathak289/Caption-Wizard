package com.wrongcode.captionwizard.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.wrongcode.captionwizard.data.Repository
import com.wrongcode.captionwizard.data.database.entities.SubtitleEntity
import com.wrongcode.captionwizard.models.Subtitle
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubtitleViewModel @Inject constructor(private val repository: Repository, application: Application) : AndroidViewModel(application){
    val readAllSubtitle : LiveData<List<SubtitleEntity>> = repository.local.readAllSubtitleVideos().asLiveData()

    private val _subtitleOfVideoId = MutableLiveData<List<SubtitleEntity>>()
    val subtitleOfVideoId : LiveData<List<SubtitleEntity>> get() = _subtitleOfVideoId

    private val _isUserOnProcess = MutableLiveData<Boolean>(true)
    val isUserOnProcess : LiveData<Boolean> get() = _isUserOnProcess

    fun updateIsUserOnProcess(newValue : Boolean){
        _isUserOnProcess.value = newValue
    }

    fun insertSubtitleList(subtitleEntity: SubtitleEntity){
        viewModelScope.launch(Dispatchers.IO){
            repository.local.insertSubtitle(subtitleEntity)
        }
    }
    fun readSubtitleByVideoId(videoId : String){
        viewModelScope.launch {
            repository.local.readSubtitleByVideoId(videoId).collect{
                _subtitleOfVideoId.value = it
            }
        }

    }
    fun updateSubtitleList(videoId: String, updatedSubtitleList: ArrayList<Subtitle>){
        viewModelScope.launch {
            repository.local.updateSubtitleList(videoId,updatedSubtitleList)
        }
    }
    fun deleteSubtitleVideo(subtitleEntity: SubtitleEntity){
        viewModelScope.launch {
            repository.local.deleteSubtitleVideo(subtitleEntity)
        }
    }

}