package com.cindaku.holanear.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.hbb20.CountryCodePicker
import com.cindaku.holanear.BaseApp
import com.cindaku.holanear.R
import com.cindaku.holanear.api.KenulinAPIService
import com.cindaku.holanear.model.OTPRequest
import javax.inject.Inject

class LoginActivity : AppCompatActivity() {
    lateinit var ccp: CountryCodePicker;
    lateinit var phone: EditText;
    lateinit var otpButton: Button
    @Inject
    lateinit var kenulinAPIService: KenulinAPIService
    override fun onCreate(savedInstanceState: Bundle?) {
        (application as BaseApp).appComponent.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        ccp=findViewById(R.id.countryPicker)
        phone=findViewById(R.id.phoneEditText)
        otpButton=findViewById(R.id.otpButton)
        otpButton.setOnClickListener {
            val otpRequest=OTPRequest()
            otpRequest.phone=phone.text.toString()
            otpRequest.country_code=ccp.selectedCountryNameCode
            otpRequest.country_number=ccp.selectedCountryCodeAsInt.toString()
            otpButton.isEnabled=false
            val response=kenulinAPIService.otp(otpRequest).execute()
            if(response.code()==200){
                Log.d("OTP",response.body().toString())
                otpButton.isEnabled=true
                val intent=Intent(this,VerifyActivity::class.java)
                intent.putExtra("otpRequest",otpRequest);
                intent.putExtra("otpResponse",response.body());
                startActivity(intent)
            }else{
                otpButton.isEnabled=true
                Log.e("AUTH", response.errorBody()!!.string())
                val dialog: AlertDialog = AlertDialog.Builder(this).let {
                    it.setMessage(R.string.phone_check)
                        .setPositiveButton("OK") { dialog, which ->  dialog.dismiss() }
                    it.create()
                }
                dialog.show()
            }
        }
    }
}