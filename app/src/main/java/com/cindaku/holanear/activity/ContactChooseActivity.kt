package com.cindaku.holanear.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.cindaku.holanear.BaseApp
import com.cindaku.holanear.R
import com.cindaku.holanear.db.entity.Contact
import com.cindaku.holanear.ui.adapter.ContactListAdapter
import com.cindaku.holanear.ui.adapter.SelectedContactListAdapter
import com.cindaku.holanear.ui.inf.ContactChooseInterface
import com.cindaku.holanear.viewmodel.ContactViewModel
import kotlinx.coroutines.launch

class ContactChooseActivity : AppCompatActivity() , ContactChooseInterface{
    private val viewModel: ContactViewModel by viewModels()
    lateinit var listContact: RecyclerView
    lateinit var listContactSelected: RecyclerView
    lateinit var adapter: ContactListAdapter
    lateinit var adapterSelected: SelectedContactListAdapter
    lateinit var toolbar: Toolbar
    lateinit var back: ImageView
    lateinit var titleTextView: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        (application as BaseApp).appComponent.inject(viewModel)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_choose)
        setSupportActionBar(findViewById(R.id.toolbar))
        viewModel.toSend=intent.getBooleanExtra("toSend",true)
        toolbar=findViewById(R.id.toolbar)
        back=findViewById(R.id.backImageView)
        titleTextView=findViewById(R.id.titleTextView)
        listContact=findViewById(R.id.contactRecylerView)
        listContactSelected=findViewById(R.id.contactSelectedRecylerView)
        val layoutManager=LinearLayoutManager(this)
        listContact.layoutManager=layoutManager
        val layoutManagerSelected=LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false)
        listContactSelected.layoutManager=layoutManagerSelected
        lifecycleScope.launch {
            adapter= ContactListAdapter(baseContext,viewModel.getContact(),this@ContactChooseActivity)
            adapterSelected= SelectedContactListAdapter(baseContext,this@ContactChooseActivity)
            listContact.adapter=adapter
            listContactSelected.adapter=adapterSelected
        }
        back.setOnClickListener {
            if(viewModel.isSelectionMode){
                clear()
            }else{
                setResult(Activity.RESULT_CANCELED, Intent())
                finish()
            }
        }
    }
    override fun onAdd(contact: Contact) {
        viewModel.selected.add(contact)
        if(viewModel.selected.size>0){
            viewModel.isSelectionMode=true
        }
        adapterSelected.setSelected(data = viewModel.selected.toList())
        adapter.notifyDataSetChanged()
        invalidateOptionsMenu()
    }

    override fun onRemove(contact: Contact) {
        viewModel.selected.remove(contact)
        if(viewModel.selected.size<1){
            viewModel.isSelectionMode=false
        }
        adapterSelected.setSelected(data = viewModel.selected.toList())
        adapter.notifyDataSetChanged()
        invalidateOptionsMenu()
    }

    override fun onSingle(contact: Contact) {
        val intent=Intent()
        intent.putExtra("contact", Gson().toJson(contact))
        intent.putExtra("toSend", viewModel.toSend)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun isSelectionMode(): Boolean {
        return viewModel.isSelectionMode
    }

    override fun isSelectedContact(contact: Contact): Boolean {
        return viewModel.selected.indexOf(contact)>=0
    }
    fun clear(){
        viewModel.isSelectionMode=false
        viewModel.selected.clear()
        adapterSelected.setSelected(data = viewModel.selected.toList())
        adapter.notifyDataSetChanged()
        invalidateOptionsMenu()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if(viewModel.isSelectionMode) {
            titleTextView.text=viewModel.selected.size.toString()
            menuInflater.inflate(R.menu.contact, menu)
            if(!viewModel.toSend){
                menu?.findItem(R.id.done)?.title = "CREATE GROUP"
            }else{
                menu?.findItem(R.id.done)?.title = "SEND"
            }
            if(viewModel.selected.size==adapter.getData().size){
                menu?.findItem(R.id.select_all)?.title = "UNSELECT ALL"
            }else{
                menu?.findItem(R.id.select_all)?.title = "SELECT ALL"
            }
        }else{
            titleTextView.text=resources.getString(R.string.title_activity_contact_choose)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId==R.id.done){
            val intent=Intent()
            intent.putExtra("contact_list", Gson().toJson(viewModel.selected))
            intent.putExtra("toSend", viewModel.toSend)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }else if(item.itemId==R.id.select_all){
            if(viewModel.selected.size==adapter.getData().size){
                clear()
            }else{
                viewModel.selected.clear()
                viewModel.selected.addAll(adapter.getData())
                adapterSelected.setSelected(data = viewModel.selected.toList())
                adapter.notifyDataSetChanged()
                invalidateOptionsMenu()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}