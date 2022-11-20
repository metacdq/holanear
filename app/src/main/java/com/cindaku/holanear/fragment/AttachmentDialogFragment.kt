package com.cindaku.holanear.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.cindaku.holanear.BaseApp
import com.cindaku.holanear.R
import com.cindaku.holanear.activity.DetailChatActivity
import com.cindaku.holanear.model.SelectType
import com.cindaku.holanear.viewmodel.AttachmentViewModel

class AttachmentDialogFragment: DialogFragment() {
    private val viewModel: AttachmentViewModel by viewModels()
    private lateinit var image: LinearLayout
    private lateinit var video: LinearLayout
    private lateinit var contact: LinearLayout
    private lateinit var location: LinearLayout
    private lateinit var document: LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.let {
            (it.application as BaseApp).appComponent.inject(viewModel)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        image=view.findViewById(R.id.pictureLayout)
        image.setOnClickListener {
            (activity as DetailChatActivity).run {
                this.pick(SelectType.IMAGE)
            }
        }
        video=view.findViewById(R.id.videoLayout)
        contact=view.findViewById(R.id.contactLayout)
        contact.setOnClickListener {
            (activity as DetailChatActivity).run {
                this.pick(SelectType.CONTACT)
            }
        }
        location=view.findViewById(R.id.locationLayout)
        location.setOnClickListener {
            (activity as DetailChatActivity).run {
                this.pick(SelectType.LOCATION)
            }
        }
        document=view.findViewById(R.id.documentLayout)
        document.setOnClickListener {
            (activity as DetailChatActivity).run {
                this.pick(SelectType.DOCUMENT)
            }
        }
        video.setOnClickListener {
            (activity as DetailChatActivity).run {
                this.pick(SelectType.VIDEO)
            }
        }
    }

    override fun getTheme(): Int {
        return R.style.DialogFragment
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return LayoutInflater.from(context).inflate(R.layout.dialog_attachment,container,false)
    }
    companion object{
        fun getInstance(): AttachmentDialogFragment = AttachmentDialogFragment()
    }
}