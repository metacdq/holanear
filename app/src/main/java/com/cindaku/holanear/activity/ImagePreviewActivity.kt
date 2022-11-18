package com.cindaku.holanear.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.cindaku.holanear.APP_NAME
import com.cindaku.holanear.BaseApp
import com.cindaku.holanear.R
import com.cindaku.holanear.ui.adapter.ImagePreviewAdapter
import com.cindaku.holanear.ui.viewmodel.ImagePreviewViewModel
import io.ak1.pix.helpers.PixEventCallback
import io.ak1.pix.helpers.addPixToActivity
import io.ak1.pix.models.Mode
import io.ak1.pix.models.Options
import io.ak1.pix.models.Ratio
import kotlinx.coroutines.launch
import java.io.File

class ImagePreviewActivity : AppCompatActivity() {
    private val viewModel: ImagePreviewViewModel by viewModels()
    lateinit var back: ImageView
    lateinit var toolbar: Toolbar
    lateinit var list: RecyclerView
    lateinit var send: ImageView
    lateinit var add: ImageView
    lateinit var preview: ImageView
    lateinit var text: EditText
    var jId: String?=null

    override fun onBackPressed() {
        setResult(RESULT_CANCELED)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as BaseApp).appComponent.inject(viewModel)
        setContentView(R.layout.activity_image_preview)
        setSupportActionBar(findViewById(R.id.toolbar))
        toolbar=findViewById(R.id.toolbar)
        list=findViewById(R.id.imagesRecyclerView)
        send=findViewById(R.id.sendImageView)
        add=findViewById(R.id.addImageView)
        preview=findViewById(R.id.previewImageView)
        text=findViewById(R.id.captionEditText)
        back=toolbar.findViewById(R.id.backImageView)
        list.layoutManager=LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
        back.setOnClickListener {
           setResult(RESULT_CANCELED)
           finish()
        }
        jId=intent.getStringExtra("to")
        val stringImages=intent.getStringArrayListExtra("images")
        stringImages?.let { listData ->
            lifecycleScope.launch {
                try{
                    val  toSend= arrayListOf<HashMap<String,String>>()
                    listData.forEach {
                        val map= hashMapOf<String,String>()
                        map["image"] = it
                        map["caption"] = ""
                        toSend.add(map)
                    }
                    viewModel.load(toSend)
                    list.adapter=ImagePreviewAdapter(this@ImagePreviewActivity,viewModel.images)
                    if(listData.size>0){
                        viewModel.activeIndex=0
                        loadCurrentImage(toSend[0])
                    }
                }catch (e: Exception){
                    e.printStackTrace()
                }
            }
        }
        add.setOnClickListener {
            viewModel.images[viewModel.activeIndex]["caption"] = text.text.toString()
            val images= arrayListOf<Uri>()
            for(text in viewModel.imagesToString()){
                images.add(Uri.parse(text))
            }
            val options = Options().apply{
                ratio =
                    Ratio.RATIO_AUTO                                    //Image/video capture ratio
                count = 10                                                   //Number of images to restrict selection count
                spanCount = 5                                               //Number for columns in grid
                path = "/"+ APP_NAME+"/images"                                       //Custom Path For media Storage
                preSelectedUrls = images
                isFrontFacing = false                                       //Front Facing camera on start
                mode = Mode.Picture                                             //Option to select only pictures or videos or both
                preSelectedUrls = ArrayList()                          //Pre selected Image Urls
            }
            addPixToActivity(R.id.container, options) {
                when (it.status) {
                    PixEventCallback.Status.SUCCESS -> {
                        val returnValue: List<Uri> = it.data
                        returnValue.let { listData->
                            lifecycleScope.launch{
                                val  toSend= arrayListOf<HashMap<String,String>>()
                                listData.forEach { it ->
                                    val map= hashMapOf<String,String>()
                                    map["image"] = it.toString()
                                    map["caption"] = ""
                                    toSend.add(map)
                                }
                                viewModel.reload(toSend)
                                list.adapter=ImagePreviewAdapter(this@ImagePreviewActivity,viewModel.images)
                                if(listData.size>0){
                                    viewModel.activeIndex=0
                                    loadCurrentImage(toSend[0])
                                }
                            }
                        }
                    }
                    PixEventCallback.Status.BACK_PRESSED -> {

                    }
                }
            }
        }
        send.setOnClickListener {
            viewModel.images[viewModel.activeIndex]["caption"] = text.text.toString()
            val i=Intent()
            i.putExtra("images",Gson().toJson(viewModel.images))
            setResult(RESULT_OK,i)
            finish()
        }
    }
    fun loadCurrentImage(data: HashMap<String,String>){
       try{
           data["image"]?.let {
               Glide.with(baseContext)
                   .load(File(it))
                   .into(preview)
           }
           data["caption"]?.let {
               text.setText(it)
           }
       }catch (e: Exception){
           e.printStackTrace()
       }
    }
    fun setActiveImage(position: Int){
        viewModel.images[viewModel.activeIndex]["caption"] = text.text.toString()
        viewModel.activeIndex=position
        loadCurrentImage(viewModel.images[position])
    }
}