package com.cindaku.holanear.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.cindaku.holanear.R
import com.cindaku.holanear.db.entity.ChatMessage
import java.io.File

class ImageViewerFragment : DialogFragment() {
    private lateinit var fullImageView: ImageView
    private lateinit var titleTextView: TextView
    private lateinit var backImageView: ImageView
    private lateinit var captionImageView: TextView
    private var chatMessage: ChatMessage?=null
    private var sender: String?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view=inflater.inflate(R.layout.fragment_image_viewer, container, false)
        view.findViewById<ImageView>(R.id.fullImageView)
        fullImageView=view.findViewById(R.id.fullImageView)
        titleTextView=view.findViewById(R.id.titleTextView)
        captionImageView=view.findViewById(R.id.captionImageView)
        backImageView=view.findViewById(R.id.backImageView)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sender?.let {
            titleTextView.text=it
        }
        backImageView.setOnClickListener {
            dismiss()
        }
        chatMessage?.let {
            val detailMessage= Gson().fromJson(it.body!!, HashMap<String,String>()::class.java)
            if(it.attachment==""){
                Glide.with(requireContext())
                    .load(detailMessage["url"]!!)
                    .into(fullImageView)
            }else{
                Glide.with(requireContext())
                    .load(File(it.attachment!!))
                    .into(fullImageView)
            }
            captionImageView.text=detailMessage["caption"]
        }
    }
    fun setChatMessage(message: ChatMessage){
        chatMessage=message
    }
    fun setSender(s: String){
        sender=s
    }
    override fun getTheme(): Int {
        return R.style.FullScreenDialog
    }
    companion object {
        fun newInstance() = ImageViewerFragment()
    }
}