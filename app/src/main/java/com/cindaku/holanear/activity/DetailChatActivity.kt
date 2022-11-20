package com.cindaku.holanear.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.OpenableColumns
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.cindaku.holanear.APP_NAME
import com.cindaku.holanear.BaseApp
import com.cindaku.holanear.R
import com.cindaku.holanear.db.entity.ChatMessage
import com.cindaku.holanear.db.entity.Contact
import com.cindaku.holanear.extension.genericType
import com.cindaku.holanear.fragment.AttachmentDialogFragment
import com.cindaku.holanear.fragment.ImageViewerFragment
import com.cindaku.holanear.fragment.VideoPlayerFragment
import com.cindaku.holanear.model.ContactMessage
import com.cindaku.holanear.model.Location
import com.cindaku.holanear.model.LocationMessage
import com.cindaku.holanear.model.SelectType
import com.cindaku.holanear.ui.adapter.MessageListAdapter
import com.cindaku.holanear.ui.drawable.AcronymDrawable
import com.cindaku.holanear.ui.inf.OnMessageEvent
import com.cindaku.holanear.ui.inf.OnSelectMode
import com.cindaku.holanear.ui.inf.OnSearchMessageResult
import com.cindaku.holanear.viewmodel.ChatDetailViewModel
import com.vanniktech.emoji.EmojiEditText
import com.vanniktech.emoji.EmojiPopup
import com.vanniktech.emoji.listeners.*
import io.ak1.pix.helpers.PixEventCallback
import io.ak1.pix.helpers.addPixToActivity
import io.ak1.pix.helpers.pixFragment
import io.ak1.pix.models.Mode
import io.ak1.pix.models.Options
import io.ak1.pix.models.Ratio
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File


class DetailChatActivity : AppCompatActivity(),OnSelectMode,OnMessageEvent {
    private val viewModel: ChatDetailViewModel by viewModels()
    private lateinit var toolbar: Toolbar
    private lateinit var back: ImageView
    private lateinit var title: TextView
    private lateinit var userImageView: ImageView
    private lateinit var messageList: RecyclerView
    private lateinit var sendImageView: ImageView
    private lateinit var emojiImageView: ImageView
    private lateinit var attachment: ImageView
    private lateinit var textMessage: EmojiEditText
    private lateinit var adapter: MessageListAdapter
    private lateinit var emojiPopup: EmojiPopup
    private lateinit var chatRootView: ConstraintLayout
    private lateinit var layoutReply: LinearLayout
    private lateinit var replyTextView: TextView
    private lateinit var replySubTextView: TextView
    private lateinit var cloreReplyImageView: ImageView
    private var onSearchMessageResult: OnSearchMessageResult?=null
    var attahmentDialog: AttachmentDialogFragment?=null
    var onImageCaptioned=registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            it.data?.let { intent->
                lifecycleScope.launch {
                    val result=intent.getStringExtra("images")
                    val images=Gson().fromJson<ArrayList<HashMap<String,String>>>(result,
                        genericType<ArrayList<HashMap<String,String>>>())
                    viewModel.sendImage(images)
                    initReply()
                }
            }
        }
    }

    var onSelectedDocument=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode==Activity.RESULT_OK){
           it.data?.let {
               it.data?.run {
                   val uri=this
                   val cursor=contentResolver.query(this,null,null,null,null)
                   cursor?.use {
                       val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                       it.moveToFirst()
                       val name=it.getString(nameIndex)
                       lifecycleScope.launch {
                           getExternalFilesDir(null)?.let {
                               val dir=File(it.absolutePath+"/"+
                                       APP_NAME+"/Documents")
                               if(!dir.exists()){
                                   dir.mkdirs()
                               }
                               viewModel.sendDocument(contentResolver,uri,name,dir)
                               initReply()
                           }
                       }
                       it.close()
                   }
               }
           }
        }
        hidePicker()
    }
    var onSelectedContact=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode==Activity.RESULT_OK){
            it.data?.let {
                var result=it.getStringExtra("contact")
                if(result!=null){
                    val sendContact=Gson().fromJson<Contact>(result,Contact::class.java)
                    lifecycleScope.launch {
                        val contactMessage=ContactMessage()
                        contactMessage.name=sendContact.fullName!!
                        contactMessage.phones.add(sendContact.phone!!)
                        viewModel.sendContact(contactMessage)
                        initReply()
                    }
                }else{
                    result=it.getStringExtra("contact_list")
                    val sendContact=Gson().fromJson<List<Contact>>(result, genericType<List<Contact>>())
                    lifecycleScope.launch(Dispatchers.IO) {
                        val toSend= arrayListOf<ContactMessage>()
                        sendContact.forEach {
                            val contactMessage=ContactMessage()
                            contactMessage.name= it.fullName.toString()
                            contactMessage.phones.add(it.phone!!)
                            toSend.add(contactMessage)
                        }
                        viewModel.sendContactMultiple(toSend.toList())
                        initReply()
                    }
                }
            }
        }
        hidePicker()
    }
    var onSelectedContactForwarding=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode==Activity.RESULT_OK){
            it.data?.let {
                var result=it.getStringExtra("contact")
                if(result!=null){
                    val sendContact=Gson().fromJson<Contact>(result,Contact::class.java)
                    val to= arrayListOf<Contact>(sendContact)
                    lifecycleScope.launch {
                        viewModel.forwardMessage(to)
                        initReply()
                        clearSelection()
                    }
                }else{
                    result=it.getStringExtra("contact_list")
                    lifecycleScope.launch(Dispatchers.IO) {
                        val sendContact=Gson().fromJson<List<Contact>>(result, genericType<List<Contact>>())
                        val to= arrayListOf<Contact>()
                        to.addAll(sendContact)
                        viewModel.forwardMessage(to)
                        initReply()
                        clearSelection()
                    }
                }
            }
        }
        hidePicker()
    }

    var onSelectedMap=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode==Activity.RESULT_OK){
            it.data?.let {
                val location=it.getParcelableExtra<Location>("location")
                val image=it.getStringExtra("image")
                location?.let {
                    image?.let {
                        lifecycleScope.launch {
                            viewModel.sendLocation(location, File(image))
                            initReply()
                        }
                    }
                }
            }
        }
        hidePicker()
    }

    var onAddedContact=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        if(it.resultCode==Activity.RESULT_OK) {
            it.data?.let {

            }
        }
    }
    val permissionList= arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_chat)
        (application as BaseApp).appComponent.inject(viewModel)
        viewModel.contact=Gson().fromJson(intent.getStringExtra("contact"),Contact::class.java)
        toolbar=findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        back=toolbar.findViewById(R.id.backImageView)
        title=findViewById(R.id.titleTextView)
        textMessage=findViewById(R.id.messageEditText)
        emojiImageView=findViewById(R.id.emojiImageView)
        chatRootView=findViewById(R.id.chatDetailLayout)
        userImageView=toolbar.findViewById(R.id.userImageView)
        sendImageView=findViewById(R.id.sendImageView)
        attachment=findViewById(R.id.attachmentImageView)
        messageList=findViewById(R.id.messageRecylerView)
        replyTextView=findViewById(R.id.replyTextView)
        layoutReply=findViewById(R.id.layoutReply)
        replySubTextView=findViewById(R.id.replySubTextView)
        cloreReplyImageView=findViewById(R.id.cloreReplyImageView)
        val layoutManager=LinearLayoutManager(this)
        messageList.layoutManager=layoutManager
        back.setOnClickListener {
            if(viewModel.isSelectMode){
                clearSelection()
            }else{
                finish()
            }
        }
        sendImageView.setOnClickListener {
            if(textMessage.text.toString()!=""){
                viewModel.send(textMessage.text.toString())
                val im: InputMethodManager= getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                im.hideSoftInputFromWindow(textMessage.windowToken,0)
                textMessage.clearFocus()
                textMessage.setText("")
                initReply()
            }
        }
        attachment.setOnClickListener {
            if (attahmentDialog==null){
                showPicker()
            }else{
                hidePicker()
            }
        }
        cloreReplyImageView.setOnClickListener {
            viewModel.chatToReply=null
            initReply()
        }
    }

    override fun onResume() {
        super.onResume()
        initReply()
        loadData()
    }
    fun setupToolbar(){
        if(!viewModel.isSelectMode){
            loadUserimage()
            loadUername()
        }else{
            loadSelectionCount()
        }
    }

    override fun onBackPressed() {
        if(viewModel.isSelectMode){
            clearSelection()
        }else{
            super.onBackPressed()
        }
    }
    override fun onStart() {
        super.onStart()
    }
    fun loadUername(){
        viewModel.contact?.let {
            title.text=it.fullName
        }
    }
    @SuppressLint("SetTextI18n")
    fun loadSelectionCount(){
        userImageView.isVisible=false
        title.text=viewModel.selected.size.toString()
    }
    fun loadData(){
        try {
            loadUername()
            loadUserimage()
            setupEmoji()
            loadChat()
        }catch (e: Exception){
            e.printStackTrace()
        }
    }
    fun showPicker(){
        attahmentDialog=AttachmentDialogFragment.getInstance()
        val transaction=supportFragmentManager.beginTransaction()
        transaction.add(R.id.dialogWraper, attahmentDialog!!)
        transaction.commit()
    }
    fun hidePicker(){
        attahmentDialog?.let {
            val transaction=supportFragmentManager.beginTransaction()
            transaction.remove(it)
            transaction.commit()
            attahmentDialog=null
        }
    }
    fun loadChat(){
        adapter=MessageListAdapter(this@DetailChatActivity)
        messageList.adapter=adapter
        adapter.setOnMessage(this)
        onSearchMessageResult=adapter
        viewModel.contact?.let {
            lifecycleScope.launch {
                it.id?.let {
                    viewModel.findMessageByContact(it).collect {
                        adapter.setData(it)
                        if(it.size>0){
                            messageList.scrollToPosition(it.size-1)
                        }
                        viewModel.setReadedAll()
                    }
                }
            }
        }
    }
    fun loadUserimage(){
        userImageView.isVisible=true
        viewModel.contact?.let {
            val name=it.fullName
            it.photo?.also {
                if(it!="") {
                    val options = RequestOptions()
                        .override(200,200)
                        .transform(RoundedCorners(100))
                    Glide.with(baseContext)
                        .load(it)
                        .apply(options)
                        .fitCenter()
                        .into(userImageView)
                }else{
                    val drawable=AcronymDrawable(name!![0].toString(),1f)
                    userImageView.setImageDrawable(drawable)
                }
            }
        }
    }

    override fun pick(select: SelectType) {
        if(select==SelectType.IMAGE){
            val options = Options().apply{
                ratio =
                    Ratio.RATIO_AUTO                                    //Image/video capture ratio
                count = 10                                                   //Number of images to restrict selection count
                spanCount = 5                                               //Number for columns in grid
                path = "/"+ APP_NAME+"/images"                                       //Custom Path For media Storage
                isFrontFacing = false                                       //Front Facing camera on start
                mode = Mode.Picture                                             //Option to select only pictures or videos or both
                preSelectedUrls = ArrayList()                          //Pre selected Image Urls
            }
            addPixToActivity(R.id.container, options) {
                when (it.status) {
                    PixEventCallback.Status.SUCCESS -> {
                        val returnValue: List<Uri> = it.data
                        returnValue.let {
                            if (it.size>0){
                                viewModel.contact?.run {
                                    val images= arrayListOf<String>()
                                    for (uri in returnValue){
                                        images.add(uri.toString())
                                    }
                                    val intent=Intent(baseContext,ImagePreviewActivity::class.java)
                                    intent.putExtra("images",images)
                                    intent.putExtra("to",this.jId)
                                    onImageCaptioned.launch(intent)
                                    hidePicker()
                                }
                            }
                        }
                    }
                    PixEventCallback.Status.BACK_PRESSED -> {
                        hidePicker()
                    }
                }
            }
        }else if(select==SelectType.VIDEO){
            val options = Options().apply{
                ratio =
                    Ratio.RATIO_AUTO                                    //Image/video capture ratio
                count = 1                                                   //Number of images to restrict selection count
                spanCount = 5                                               //Number for columns in grid
                path = "/"+ APP_NAME+"/video"                                       //Custom Path For media Storage
                isFrontFacing = false                                       //Front Facing camera on start
                mode = Mode.Picture                                             //Option to select only pictures or videos or both
                preSelectedUrls = ArrayList()                          //Pre selected Image Urls
            }
            addPixToActivity(R.id.container, options) {
                when (it.status) {
                    PixEventCallback.Status.SUCCESS -> {
                        val returnValue: List<Uri> = it.data
                        returnValue.let {
                            if (returnValue.isNotEmpty()){
                                val video=returnValue[0].toString()
                                lifecycleScope.launch {
                                    viewModel.sendVideo(video)
                                    initReply()
                                }
                            }
                        }
                    }
                    PixEventCallback.Status.BACK_PRESSED -> {
                        hidePicker()
                    }
                }
            }
        }else if(select==SelectType.DOCUMENT){
            val i = Intent(Intent.ACTION_GET_CONTENT, null)
            i.type = "application/*,texxt/*"
            onSelectedDocument.launch(i)
        }else if(select==SelectType.CONTACT){
            val i = Intent(baseContext,ContactChooseActivity::class.java)
            intent.putExtra("toSend",true)
            onSelectedContact.launch(i)
        }else if(select==SelectType.LOCATION){
            val i = Intent(baseContext, ChooseLocationActivity::class.java)
            onSelectedMap.launch(i)
        }else{
            hidePicker()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if(!viewModel.isSelectMode){
            menuInflater.inflate(R.menu.detail_chat, menu)
            menu?.let { it ->
                val search=it.findItem(R.id.menuSearchChat)
                val searchView= search.actionView as SearchView
                search.setOnActionExpandListener(object : MenuItem.OnActionExpandListener{

                    override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                        menu.setGroupVisible(R.id.groupCall,false)
                        menu.setGroupVisible(R.id.groupSearch,true)
                        return true
                    }

                    override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                        menu.setGroupVisible(R.id.groupCall,true)
                        invalidateOptionsMenu()
                        adapter.clearSearch()
                        return true
                    }

                })
                searchView.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener,
                    SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(p0: String?): Boolean {
                        p0?.let { query ->
                            val result=viewModel.searchMeassage(query)
                            onSearchMessageResult?.apply {
                                this.onResult(result)
                                if(result.size>0){
                                    this.getNext()?.also {
                                        if(it>=0) {
                                            messageList.scrollToPosition(it)
                                        }
                                    }
                                }
                            }
                        }
                        return true
                    }

                    override fun onQueryTextChange(p0: String?): Boolean {
                        return true
                    }

                })
            }
        }else{
            menuInflater.inflate(R.menu.selection, menu)
            menu?.setGroupVisible(R.id.groupSelectionOneOnly,viewModel.selected.size==1)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==R.id.menuCall){
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(permissionList,1380)
            }else{
                viewModel.call(false)
            }
        }else if(item.itemId==R.id.menuVideoCall){
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(permissionList,1380)
            }else{
                viewModel.call(true)
            }
        }else if(item.itemId==R.id.menuNext){
            onSearchMessageResult?.apply {
                this.getNext()?.also {
                    if(it>=0) {
                        messageList.scrollToPosition(it)
                    }
                }
            }
        }else if(item.itemId==R.id.menuPrevious){
            onSearchMessageResult?.apply {
                this.getPrevious()?.also {
                    if(it>=0) {
                        messageList.scrollToPosition(it)
                    }
                }
            }
        }else if(item.itemId==R.id.menuDelete){
            viewModel.deleteMessage()
            clearSelection()
        }else if(item.itemId==R.id.menuForward){
            val i = Intent(baseContext,ContactChooseActivity::class.java)
            onSelectedContactForwarding.launch(i)
        }else if(item.itemId==R.id.menuCopy){
            if(viewModel.selected.size>0){
                val clipboard: ClipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Copied Text", viewModel.selected.get(0).body)
                clipboard.setPrimaryClip(clip)
                clearSelection()
            }
        }else if(item.itemId==R.id.menuStar){
            viewModel.starMessage()
            clearSelection()
        }else if(item.itemId==R.id.menuReply){
            viewModel.generateSnippet(viewModel.selected.get(0))
            clearSelection()
            initReply()
        }
        return super.onOptionsItemSelected(item)
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    fun setupEmoji(){
        emojiPopup = EmojiPopup(chatRootView,
            textMessage,
            onEmojiPopupShownListener = {
                emojiImageView.setImageResource(R.drawable.ic_keyboard)
            },
            onEmojiPopupDismissListener = {
                emojiImageView.setImageResource(R.drawable.ic_emoji)
            },
            keyboardAnimationStyle = R.style.emoji_fade_animation_style
        )
        emojiImageView.setOnClickListener {
            if(emojiPopup.isShowing){
                emojiPopup.dismiss()
            }else{
                emojiPopup.show()
            }
        }
    }

    override fun requestAddContact(contactToAdd: ContactMessage) {
        val intent = Intent(
            ContactsContract.Intents.Insert.ACTION
        )
        intent.setType(ContactsContract.RawContacts.CONTENT_TYPE)
        intent.putExtra(ContactsContract.Intents.Insert.PHONE, contactToAdd.phones[0])
        intent.putExtra(ContactsContract.Intents.Insert.NAME, contactToAdd.name)
        onAddedContact.launch(intent)
    }

    override fun onLongPressed(message: ChatMessage) {
        viewModel.isSelectMode=true
        if(viewModel.selected.contains(message)){
            unSelectMessage(message)
        }else{
            selectMessage(message)
        }
    }

    override fun onClick(message: ChatMessage) {

    }

    override fun checkSelected(message: ChatMessage): Boolean {
        return viewModel.selected.indexOf(message)>=0
    }

    override fun unSelectMessage(message: ChatMessage) {
        viewModel.selected.remove(message)
        if(viewModel.selected.size<1){
            viewModel.isSelectMode=false
            setupToolbar()
        }else{
            setupToolbar()
        }
        invalidateOptionsMenu()
        adapter.notifyDataSetChanged()
    }

    override fun selectMessage(message: ChatMessage) {
        invalidateOptionsMenu()
        viewModel.selected.add(message)
        setupToolbar()
        adapter.notifyDataSetChanged()
    }

    override fun isSelectedMode(): Boolean {
        return viewModel.isSelectMode
    }

    override fun clearSelection() {
        viewModel.isSelectMode=false
        viewModel.selected.clear()
        setupToolbar()
        invalidateOptionsMenu()
        adapter.notifyDataSetChanged()
    }

    override fun getYourJid(): String {
        return viewModel.myJid()
    }

    override fun goToContact(contact: Contact) {
        val intent= Intent(baseContext, DetailChatActivity::class.java)
        intent.putExtra("contact",Gson().toJson(contact))
        startActivity(intent)
    }

    override fun getReplyContactName(jid: String): String {
        return viewModel.getContactName(jid)
    }

    override fun onDownloadRequest(message: ChatMessage) {
        viewModel.downloadMessage(baseContext,chatMessage = message)
    }

    override fun onImageViewerRequest(message: ChatMessage) {
        val imageViewer=ImageViewerFragment.newInstance()
        imageViewer.setChatMessage(message)
        viewModel.contact?.fullName?.let { imageViewer.setSender(it) }
        imageViewer.show(supportFragmentManager,"IMAGE_VIEWER")
    }

    override fun onVideoPlayRequest(message: ChatMessage) {
        val videoPlayer=VideoPlayerFragment.newInstance()
        videoPlayer.setChatMessage(message)
        viewModel.contact?.fullName?.let { videoPlayer.setSender(it) }
        videoPlayer.show(supportFragmentManager,"VIDEO_PLAYER")
    }

    override fun onGeoNavigate(message: LocationMessage) {
        val uri = "geo:0,0?q= "+message.location.lat+","+message.location.lng
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri)))
    }

    override fun checkContact(phone: String): List<Contact> {
        return viewModel.search(phone)
    }

    override fun initReply() {
        if(viewModel.chatToReply!=null){
            layoutReply.isVisible=true
            replySubTextView.text=viewModel.getSubTextReply()
            replyTextView.text=viewModel.getTextReply()
        }else{
            layoutReply.isVisible=false
        }
    }

    override fun onRequestScrollToMessageId(messageId: String) {
        val message=viewModel.getMessageByStanzaId(messageId)
        if(message!=null){
            val position=adapter.findMessageOnList(message)
            if(position>=0){
                messageList.scrollToPosition(position)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        hidePicker()
    }

}