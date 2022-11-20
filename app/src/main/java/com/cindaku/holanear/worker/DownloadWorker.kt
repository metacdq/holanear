package com.cindaku.holanear.worker

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.cindaku.holanear.APP_NAME
import com.cindaku.holanear.BaseApp
import com.cindaku.holanear.model.LocationMessage
import com.cindaku.holanear.model.MessageType
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class DownloadWorker(val context: Context,val workerParams: WorkerParameters) : Worker(context,
    workerParams
) {
    @SuppressLint("SimpleDateFormat")
    override fun doWork(): Result {
        val messageId=workerParams.inputData.getInt("ID",0)
        if(messageId==0){
            return Result.success()
        }
        try {
            val message=(applicationContext as BaseApp).appComponent.chatRepository().getMessageByMessageId(messageId)
            message.isLoading=true
            (applicationContext as BaseApp).appComponent.chatRepository().updateMessage(chatMessage = message)
            if(message.messageType==MessageType.LOCATION){
                val detailMessage=Gson().fromJson(message.body!!, LocationMessage::class.java)
                context.getExternalFilesDir(null)?.let {
                    try {
                        val request=Request.Builder()
                            .url(detailMessage.url)
                            .build()
                        val response=(applicationContext as BaseApp).appComponent.httpClient()
                            .newCall(request).execute()
                        if(response.isSuccessful){
                            val dir=File(it.absolutePath+"/"+
                                    APP_NAME +"/Data")
                            if(!dir.exists()){
                                dir.mkdirs()
                            }
                            val format= SimpleDateFormat("yyyyMMddHHmmss")
                            val filename=format.format(Calendar.getInstance().time)+message.attachmentName
                            val file = File(dir.absolutePath+"/"+filename)
                            val fos = FileOutputStream(file);
                            fos.write(response.body!!.bytes());
                            fos.flush();
                            fos.close()
                            message.attachment=file.absolutePath
                            message.isLoading=false
                            (applicationContext as BaseApp).appComponent.chatRepository()
                                .updateMessage(chatMessage = message)
                            return Result.success()
                        }
                    }catch (e: Exception){
                        e.printStackTrace()
                    }
                }
            }else {
                val detailMessage =
                    Gson().fromJson(message.body!!, HashMap<String, String>()::class.java)
                context.getExternalFilesDir(null)?.let {
                    try {
                        val request = Request.Builder()
                            .url(detailMessage["url"]!!)
                            .build()
                        val response = (applicationContext as BaseApp).appComponent.httpClient()
                            .newCall(request).execute()
                        if (response.isSuccessful) {
                            val dir = File(
                                it.absolutePath + "/" +
                                        APP_NAME + "/Data"
                            )
                            if (!dir.exists()) {
                                dir.mkdirs()
                            }
                            val format = SimpleDateFormat("yyyyMMddHHmmss")
                            val filename =
                                format.format(Calendar.getInstance().time) + message.attachmentName
                            val file = File(dir.absolutePath + "/" + filename)
                            val fos = FileOutputStream(file);
                            fos.write(response.body!!.bytes());
                            fos.flush();
                            fos.close()
                            message.attachment = file.absolutePath
                            message.isLoading=false
                            (applicationContext as BaseApp).appComponent.chatRepository()
                                .updateMessage(chatMessage = message)
                            return Result.success()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }catch (e: Exception){
            e.printStackTrace()
        }
        return Result.failure()
    }
}