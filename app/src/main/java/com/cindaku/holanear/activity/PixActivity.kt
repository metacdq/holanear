package com.cindaku.holanear.activity

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.cindaku.holanear.R
import io.ak1.pix.helpers.PixBus
import io.ak1.pix.helpers.PixEventCallback
import io.ak1.pix.helpers.addPixToActivity
import io.ak1.pix.models.Options

class PixActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pix)
        val options = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra("options", Options::class.java)
        } else {
            intent.getParcelableExtra("options")
        }
        addPixToActivity(R.id.container, options) {
            when (it.status) {
                PixEventCallback.Status.SUCCESS -> {
                    val returnValue: ArrayList<Uri> = arrayListOf()
                    returnValue.addAll(it.data)
                    val i=Intent()
                    i.putParcelableArrayListExtra("images", returnValue)
                    setResult(RESULT_OK,i)
                    finish()
                }
                PixEventCallback.Status.BACK_PRESSED -> {

                }
            }
        }
    }

    override fun onBackPressed() {
        PixBus.onBackPressedEvent()
        super.onBackPressed()
    }
}