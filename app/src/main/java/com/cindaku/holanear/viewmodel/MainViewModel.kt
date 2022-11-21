package com.cindaku.holanear.viewmodel

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cindaku.holanear.activity.MainActivity
import com.cindaku.holanear.api.CindakuAPIService
import com.cindaku.holanear.model.LoginRequest
import com.cindaku.holanear.model.LoginResponse
import com.cindaku.holanear.module.Storage
import com.cindaku.holanear.ui.inf.OnLoading
import com.knear.android.OnReceiveUri
import com.knear.android.service.NearMainService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Base64
import javax.inject.Inject

class MainViewModel : ViewModel() {
    @Inject
    lateinit var storage: Storage
    @Inject
    lateinit var nearMainService: NearMainService
    @Inject
    lateinit var cindakuAPIService: CindakuAPIService
    fun checkLogin(): Boolean{
        return storage.getBoolean("login")
    }

    fun login(activity: MainActivity){
        val loading: OnLoading = activity
        loading.showLoading()
        nearMainService.login(activity, object: OnReceiveUri{
            override fun onReceive(uri: Uri) {
                val allKeys=uri.getQueryParameter("all_keys").toString()
                val accountId=uri.getQueryParameter("account_id").toString()
                val preToken = "$accountId&$allKeys"
                val encoder = Base64.getEncoder()
                val token = encoder.encodeToString(preToken.toByteArray())
                val loginRequest=LoginRequest()
                loginRequest.token=token
                Log.d("LOGIN-TOKEN", token)
                viewModelScope.launch(Dispatchers.IO){
                    try{
                        cindakuAPIService.login(loginRequest).enqueue(object : Callback<LoginResponse>{
                            override fun onResponse(
                                call: Call<LoginResponse>,
                                response: Response<LoginResponse>
                            ) {
                                try{
                                    if(response.isSuccessful){
                                        val data = response.body()
                                        if(data==null || !data.status){
                                            viewModelScope.launch(Dispatchers.Main) {
                                                Toast.makeText(activity, "Login Failed", Toast.LENGTH_SHORT)
                                                    .show()
                                            }
                                            loading.hideLoading()
                                        }else{
                                            storage.setBoolean("login",true)
                                            storage.setString("jid", accountId)
                                            storage.setString("password", data.data!!.xmpp_token)
                                            viewModelScope.launch(Dispatchers.Main) {
                                                loading.hideLoading()
                                                activity.checkLogin()
                                            }

                                        }
                                    }else{
                                        viewModelScope.launch(Dispatchers.Main){
                                            Toast.makeText(activity, "Login Failed", Toast.LENGTH_SHORT).show()
                                            loading.hideLoading()
                                        }
                                    }
                                }catch (e: Exception){
                                    viewModelScope.launch(Dispatchers.Main) {
                                        Toast.makeText(activity, "Login Failed", Toast.LENGTH_SHORT).show()
                                        loading.hideLoading()
                                    }
                                }
                            }

                            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                                viewModelScope.launch(Dispatchers.Main) {
                                    Toast.makeText(activity, t.message, Toast.LENGTH_SHORT).show()
                                    loading.hideLoading()
                                }
                            }

                        })

                    }catch (e: Exception){
                        viewModelScope.launch(Dispatchers.Main) {
                            Toast.makeText(activity, "Login Failed", Toast.LENGTH_SHORT).show()
                            loading.hideLoading()
                        }
                    }
                }
            }

        })
    }
}