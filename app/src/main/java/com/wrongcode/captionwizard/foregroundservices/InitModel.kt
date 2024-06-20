package com.wrongcode.captionwizard.foregroundservices

import android.content.Context
import android.util.Log
import android.view.Display.Mode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.vosk.Model
import org.vosk.android.StorageService
import java.io.IOException
import kotlin.coroutines.resume

class InitModel(private val context: Context) {

    private lateinit var model : Model
    private suspend fun initModelAsync() : Model{
        return suspendCancellableCoroutine { continuation ->
            StorageService.unpack(
                context,"model-en-us","model",
                { unpackedModel: Model ->
                    model = unpackedModel
                    continuation.resume(model) // Resume the coroutine with the initialized model
                }
            ) { exception: IOException ->
                Log.d("sanjay@@@", "Failed to unpack the model" + exception.message)
                continuation.cancel(exception) // Cancel the coroutine with an exception
            }
        }
    }

    suspend fun getModel() : Model{
        if(!::model.isInitialized){
            model = initModelAsync()
        }
        return model
    }

//    private lateinit var model: Model
//    private fun initModelAsync() : Deferred<Model>{
//        return CoroutineScope(Dispatchers.IO).async {
//            val m = StorageService.sync(context, "model-en-us", "model")
//            model = Model(m)
//            model
//        }
//    }
//    suspend fun getModel() : Model{
//        return initModelAsync().await()
//    }

}