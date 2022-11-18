package com.cindaku.holanear.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.google.android.exoplayer2.MediaItem
import com.google.gson.Gson
import com.cindaku.holanear.R
import com.cindaku.holanear.db.entity.ChatMessage
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ui.StyledPlayerView
import java.io.File


class VideoPlayerFragment : DialogFragment() {
    private lateinit var videoPlayer: StyledPlayerView
    private var mPlayer: ExoPlayer? = null
    private lateinit var titleTextView: TextView
    private lateinit var backImageView: ImageView
    private var chatMessage: ChatMessage?=null
    private var sender: String?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view=inflater.inflate(R.layout.fragment_video_player, container, false)
        videoPlayer=view.findViewById(R.id.videoPlayer)
        titleTextView=view.findViewById(R.id.titleTextView)
        backImageView=view.findViewById(R.id.backImageView)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        context?.let {
            mPlayer=ExoPlayer.Builder(it).build()
            mPlayer!!.playWhenReady=true
            videoPlayer.player=mPlayer
            chatMessage?.let {
                try {
                    if(it.attachment=="") {
                        val detailMessage= Gson().fromJson(it.body!!, HashMap<String,String>()::class.java)
                        val mediaItem = detailMessage["url"]?.let { it1 -> MediaItem.fromUri(it1) }
                        mediaItem?.let {
                            mPlayer!!.setMediaItem(mediaItem)
                            mPlayer!!.prepare()
                        }
                    }else{
                        val mediaItem=MediaItem.fromUri(Uri.fromFile(File(it.attachment!!)))
                        mPlayer!!.setMediaItem(mediaItem)
                        mPlayer!!.prepare()
                    }
                }catch (e: Exception){
                    e.printStackTrace()
                }
            }
        }
        sender?.let {
            titleTextView.text=it
        }
        backImageView.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        releasePlayer()
    }
    override fun getTheme(): Int {
        return R.style.FullScreenDialog
    }
    companion object {
        @JvmStatic
        fun newInstance() = VideoPlayerFragment()
    }
    fun setChatMessage(message: ChatMessage){
        chatMessage=message
    }
    fun setSender(s: String){
        sender=s
    }
    private fun releasePlayer() {
        if (mPlayer != null) {
            mPlayer!!.release()
            mPlayer = null
        }
    }
}