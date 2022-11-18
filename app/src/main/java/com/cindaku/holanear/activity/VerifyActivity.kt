package com.cindaku.holanear.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.cindaku.holanear.BaseApp
import com.cindaku.holanear.R
import com.cindaku.holanear.api.KenulinAPIService
import com.cindaku.holanear.di.module.Storage
import com.cindaku.holanear.model.OTPRequest
import com.cindaku.holanear.model.OTPResponse
import com.cindaku.holanear.model.VerifyRequest
import javax.inject.Inject

class VerifyActivity : AppCompatActivity() {
    @Inject
    lateinit var kenulinAPIService: KenulinAPIService
    @Inject
    lateinit var storage: Storage
    lateinit var back: ImageView
    lateinit var time: TextView
    lateinit var desc: TextView
    lateinit var token: EditText
    lateinit var resend: TextView
    lateinit var verify: Button
    var otpRequest: OTPRequest?=null
    var otpResponse: OTPResponse?=null
    var count=0
    override fun onCreate(savedInstanceState: Bundle?) {
        (application as BaseApp).appComponent.inject(this)
        super.onCreate(savedInstanceState)
        otpRequest=intent.getParcelableExtra("otpRequest")
        otpResponse=intent.getParcelableExtra("otpResponse")
        setContentView(R.layout.activity_verify)
        back=findViewById(R.id.backImageView)
        time=findViewById(R.id.timerTextView)
        desc=findViewById(R.id.descTextView)
        token=findViewById(R.id.tokenEditText)
        resend=findViewById(R.id.resendTextView)
        verify=findViewById(R.id.otpButton)
        desc.text=resources.getText(R.string.phone_check,otpResponse!!.phone)
        back.setOnClickListener {
            finish()
        }
        timer()
        verify.setOnClickListener {
            val verifyRequest=VerifyRequest()
            verifyRequest.email=otpResponse!!.email
            verifyRequest.token=token.text.toString()
            verify.isEnabled=false
            val response=kenulinAPIService.verify(verifyRequest).execute()
            verify.isEnabled=true
            if(response.code()==200){
                storage.setBoolean("login",true)
                storage.setString("jid", response.body()!!.jid)
                storage.setString("country_number", otpRequest!!.country_number.toString())
                storage.setString("password", response.body()!!.password)
                val intent= Intent(baseContext,MainActivity::class.java)
                startActivity(intent)
                finish()
            }else{
                Log.e("AUTH", response.errorBody()!!.string())
                val dialog: AlertDialog=AlertDialog.Builder(this)?.let {
                    it.setMessage(R.string.invalid_otp)
                        .setPositiveButton("OK") { dialog, which ->  dialog.dismiss() }
                    it.create()
                }
                dialog.show()
            }
        }
        resend.setOnClickListener {
            count=0
            resend.isEnabled=false
            val response=kenulinAPIService.otp(otpRequest!!).execute()
            if(response.code()==200){
                Log.d("OTP",response.body().toString())
                val intent=Intent(this,VerifyActivity::class.java)
                intent.putExtra("otpRequest",otpRequest);
                intent.putExtra("otpResponse",response.body());
                startActivity(intent)
            }else{
                Log.e("AUTH", response.errorBody()!!.string())
                val dialog: AlertDialog = AlertDialog.Builder(this)?.let {
                    it.setMessage(R.string.phone_check)
                        .setPositiveButton("OK") { dialog, which ->  dialog.dismiss() }
                    it.create()
                }
                dialog.show()
            }
        }
        resend.isEnabled=false
    }
    private fun timer(){
        Handler().postDelayed({
            if(count>=180){
                resend.isEnabled=true
                count=0
            }
            count += 1
            setTimer()
            timer()
        },1000)
    }
    private fun setTimer() {
        val minute= (count/60)
        val second= (count-(minute*60))
        val minuteString= "0$minute"
        var secondString=if(second<10) "0"+second else "$second"
        time.text = "$minuteString:$secondString"
    }
}