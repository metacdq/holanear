package com.cindaku.holanear.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.PowerManager
import android.view.TextureView
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.cindaku.holanear.BaseApp
import com.cindaku.holanear.R
import com.cindaku.holanear.db.entity.Contact
import com.cindaku.holanear.di.module.SIPConnector
import com.cindaku.holanear.ui.drawable.AcronymDrawable
import com.cindaku.holanear.ui.inf.OnCall
import javax.inject.Inject

class CallActivity : AppCompatActivity(),OnCall {
    private var isIncoming=false
    private lateinit var acceptLayout: LinearLayout
    private lateinit var layoutButtonCall: LinearLayout
    private lateinit var layoutSpeaker: LinearLayout
    private lateinit var layoutSwitchVideo: LinearLayout
    private lateinit var imageViewUser: ImageView
    private lateinit var imageViewCallEnded: ImageView
    private lateinit var imageViewVideoVoice: ImageView
    private lateinit var SwitchCamera: ImageView
    private lateinit var SwitchMic: ImageView
    private lateinit var imageViewCall: ImageView
    private lateinit var imageViewSpeaker: ImageView
    private lateinit var callTextView: TextView
    private lateinit var textureViewRemote: TextureView
    private lateinit var textureViewLocal: TextureView
    private var caller: Contact?=null
    @Inject
    lateinit var sipConnector: SIPConnector
    private var isLoad=false
    private var isMuted=false
    private var isVideo=false
    private var isResume=false
    private var isOnProgress=false
    private var wakeLock: PowerManager.WakeLock? = null
    private var powerManager: PowerManager?=null
    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.addFlags(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON)
        powerManager=getSystemService(POWER_SERVICE) as PowerManager
        (application as BaseApp).appComponent.inject(this)
        isVideo=intent.getBooleanExtra("isVideo",false)
        isResume=intent.getBooleanExtra("resume",false)
        isIncoming=intent.getBooleanExtra("incoming",false)
        isOnProgress=intent.getBooleanExtra("onprogress",false)
        caller = Gson().fromJson(intent.getStringExtra("caller"), Contact::class.java)
        acceptLayout=findViewById(R.id.acceptLayout)
        layoutSwitchVideo=findViewById(R.id.layoutSwitchVideo)
        layoutSpeaker=findViewById(R.id.layoutSpeaker)
        imageViewUser=findViewById(R.id.imageViewUser)
        imageViewSpeaker=findViewById(R.id.imageViewSpeaker)
        imageViewVideoVoice=findViewById(R.id.imageViewVideoVoice)
        imageViewCallEnded=findViewById(R.id.imageViewCallEnded)
        callTextView=findViewById(R.id.callTextView)
        imageViewCall=findViewById(R.id.imageViewCall)
        layoutButtonCall=findViewById(R.id.layoutButtonCall)
        SwitchCamera=findViewById(R.id.SwitchCamera)
        SwitchMic=findViewById(R.id.SwitchMic)
        textureViewLocal=findViewById(R.id.textureViewLocal)
        textureViewRemote=findViewById(R.id.textureViewRemote)
        caller?.let {
            val name=it.fullName
            callTextView.text=name
            it.photo?.also {
                if(it!="") {
                    val options = RequestOptions()
                        .override(400,400)
                    Glide.with(baseContext)
                        .load(it)
                        .apply(options)
                        .fitCenter()
                        .into(imageViewUser)
                }else{
                    val drawable= AcronymDrawable(name!![0].toString(),1f,false)
                    imageViewUser.setImageDrawable(drawable)
                }
            }
        }
        imageViewCallEnded.setOnClickListener {
            if(isIncoming){
                sipConnector.hangUp()
            }else{
                sipConnector.terminate()
            }
            finish()
        }
        SwitchCamera.setOnClickListener {
            sipConnector.toggleCamera()
        }
        imageViewCall.setOnClickListener {
            sipConnector.accept()
        }
        imageViewVideoVoice.setOnClickListener {
            sipConnector.toggleVideoVoiceCall(!isVideo)
        }
        SwitchMic.setOnClickListener {
            isMuted=!isMuted
            sipConnector.toggleMic(isLoad)
            if(isMuted){
                SwitchMic.setImageDrawable(resources.getDrawable(R.drawable.ic_mic_on,null))
            }else{
                SwitchMic.setImageDrawable(resources.getDrawable(R.drawable.ic_mic_off,null))
            }
        }
        imageViewSpeaker.setOnClickListener {
            isLoad=!isLoad
            sipConnector.toggleSpeaker(isLoad)
            if(isLoad){
                imageViewSpeaker.setImageDrawable(resources.getDrawable(R.drawable.ic_unload_speaker,null))
            }else{
                imageViewSpeaker.setImageDrawable(resources.getDrawable(R.drawable.ic_loadspeaker,null))
            }
        }
        imageViewVideoVoice.setOnClickListener {
            isVideo=!isVideo
            sipConnector.toggleVideoVoiceCall(isVideo)
            reLayout()
        }
        sipConnector.setOnCall(this)
        powerManager?.run {
            wakeLock=newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"call_frontend:Kenulin")
            wakeLock?.acquire(10*60*1000)
        }
        reLayout()
    }

    override fun onDestroy() {
        super.onDestroy()
        wakeLock?.release()
    }

    override fun onTerminated() {
        isOnProgress=false
        finish()
    }

    override fun onEnded() {
        isOnProgress=false
        finish()
    }

    override fun onCallProgress(isVideo: Boolean) {
        isOnProgress=true
        isIncoming=false
        this.isVideo=isVideo
        reLayout()
    }

    override fun onRelayout(isVideo: Boolean) {
        this.isVideo=isVideo
        reLayout()
    }

    override fun onSwitchVideoVoice(isVideo: Boolean) {
        this.isVideo=isVideo
        reLayout()
    }
    @SuppressLint("UseCompatLoadingForDrawables")
    private fun reLayout(){
        acceptLayout.isVisible=isIncoming
        layoutButtonCall.isVisible=isOnProgress
        callTextView.isVisible=isIncoming || (isOnProgress && !isVideo) || (!isIncoming && !isOnProgress)
        imageViewUser.isVisible=isIncoming || (isOnProgress && !isVideo) || (!isIncoming && !isOnProgress)
        layoutSpeaker.isVisible=!isVideo
        layoutSwitchVideo.isVisible=isVideo
        textureViewLocal.isVisible=isVideo
        textureViewRemote.isVisible=isVideo
        if(isVideo){
            imageViewVideoVoice.setImageDrawable(resources.getDrawable(R.drawable.ic_voice_only,null))
            sipConnector.setRemoteView(textureViewRemote)
            sipConnector.setLocalView(textureViewLocal)
        }else{
            imageViewVideoVoice.setImageDrawable(resources.getDrawable(R.drawable.ic_video_call,null))
        }
    }
}