package com.cindaku.holanear.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import com.cindaku.holanear.BaseApp
import com.cindaku.holanear.R
import com.cindaku.holanear.db.entity.Contact
import com.cindaku.holanear.ui.inf.OnFinishAddContact
import com.cindaku.holanear.viewmodel.ContactViewModel

class ContactActivity : AppCompatActivity(), OnFinishAddContact {
    lateinit var toolbar: Toolbar
    lateinit var back: ImageView
    lateinit var buttonSave: Button
    lateinit var nameEditText: EditText
    lateinit var phoneEditText: EditText
    lateinit var viewModel: ContactViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        viewModel=ViewModelProvider(this)[ContactViewModel::class.java]
        (application as BaseApp).appComponent.inject(viewModel)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact)
        toolbar=findViewById(R.id.toolbar)
        back=findViewById(R.id.backImageView)
        nameEditText=findViewById(R.id.editTextContactName)
        phoneEditText=findViewById(R.id.editTextWalletAddress)
        back.setOnClickListener {
            finish()
        }
        setSupportActionBar(toolbar)
        buttonSave = findViewById(R.id.buttonAddContact)
        buttonSave.setOnClickListener {
            val name = nameEditText.text.toString()
            val phone = phoneEditText.text.toString()
            if(name.isEmpty()){
                Toast.makeText(this, "Fill Contact Name", Toast.LENGTH_SHORT).show()
            }
            if(phone.isEmpty()){
                Toast.makeText(this, "Fill Wallet Address", Toast.LENGTH_SHORT).show()
            }
            viewModel.save(this, name, phone)
        }
    }

    override fun contactAdded(contact: Contact) {
        finish()
    }
}