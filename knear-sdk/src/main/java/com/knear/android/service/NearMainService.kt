package com.knear.android.service

import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import com.knear.android.NearService
import com.knear.android.NearWalletLoginDialog
import com.knear.android.OnReceiveUri
import com.knear.android.scheme.KeyPair
import com.knear.android.scheme.KeyPairEd25519

class NearMainService(private val context: Context,
                      private var networkId: String,
                      appName: String,
                      private var fullAccess: Boolean,
                      walletUrl: String,
                      rcpEndpoint: String,) {

    private var accountId: String = ""
    private val redirectUri = "hola-near://success-auth"

    private var allKeys: String = ""
    private var publicKey: String = ""

    private var nearService : NearService = NearService(walletUrl, rcpEndpoint, context.getSharedPreferences(appName, Context.MODE_PRIVATE))
    private lateinit var privateKey: KeyPairEd25519
    private var androidKeyStore: AndroidKeyStore = AndroidKeyStore(context.getSharedPreferences(appName, Context.MODE_PRIVATE))

    fun checkLogin(): Boolean{
        var isLogin=false
        this.androidKeyStore.setNetworkId(networkId)
        val networkId = androidKeyStore.getNetworkId()
        val accountId = androidKeyStore.getAccountId();

        if(!networkId.isNullOrEmpty() && !accountId.isNullOrEmpty()) {
            this.accountId = accountId
            this.networkId = networkId
        }else{
            return false
        }
        this.androidKeyStore.getKey(this.networkId, this.accountId)?.let {
            this.privateKey = it
            isLogin = true
        }
        return isLogin
    }

    fun getAccountId(): String?{
        return androidKeyStore.getAccountId()
    }

    fun logout(){
        this.androidKeyStore.setKey(this.networkId, this.accountId, KeyPair.fromRandom("ed25519"))
        this.androidKeyStore.setAccountId("")
    }

    fun login(activity: AppCompatActivity, onReceiveUri: OnReceiveUri){
        this.androidKeyStore.setNetworkId(networkId)

        val networkId = androidKeyStore.getNetworkId()
        val accountId = androidKeyStore.getAccountId();

        if(!networkId.isNullOrEmpty() && !accountId.isNullOrEmpty()) {
            this.accountId = accountId
            this.networkId = networkId
        }
        this.privateKey = KeyPair.fromRandom("ed25519")
        loginOAuth(activity, onReceiveUri)
    }

    private fun loginOAuth(activity: AppCompatActivity, onReceiveUri: OnReceiveUri) {
        val loggingUri = this.nearService.startLogin(this.privateKey.publicKey, redirectUri, fullAccess)

        this.androidKeyStore.setAccountId(accountId)
        this.androidKeyStore.setNetworkId(networkId)
        this.androidKeyStore.setKey(networkId, accountId, this.privateKey)
        val dialog=NearWalletLoginDialog()
        dialog.login(loggingUri.toString(), onReceiveUri)
        dialog.show(activity.supportFragmentManager,"WALLET_LOGIN")
    }

    fun attemptLogin(uri: Uri?):Boolean {
        var success = false
        uri?.let {
            if (it.toString().startsWith(redirectUri)) {
                val currentAccountId = uri.getQueryParameter("account_id")
                val currentPublicKey = uri.getQueryParameter("public_key")
                val currentKeys = uri.getQueryParameter("all_keys")

                if (!currentAccountId.isNullOrEmpty() && !currentKeys.isNullOrEmpty()) {
                    accountId = currentAccountId
                    allKeys = currentKeys

                    if(fullAccess){
                        if(!currentPublicKey.isNullOrEmpty()){
                            publicKey = currentPublicKey
                        }else{
                            return success
                        }
                    }
                    this.nearService.finishLogging(networkId, this.privateKey, accountId)
                    success = true
                }
            }
        }
        return success
    }

//    fun sendTransaction(receiver: String, amount: String) = this.nearService.sendMoney(accountId, receiver, amount)

    fun callViewFunction(
        contractName: String,
        methodName: String,
        methodArgs: String
    ) = nearService.callViewFunction(accountId,contractName, methodName,methodArgs)

    fun callViewFunctionTransaction(contractName: String, methodName: String, methodArgs: String) =
        nearService.callViewFunctionTransaction(accountId, contractName, methodName, methodArgs)

    fun viewAccessKey() = nearService.viewAccessKey(accountId)
    fun viewAccessKeyLists() = nearService.viewAccessKeyList(accountId)
    fun viewAccessKeyChangesAll(accountIdList:List<String>) = nearService.viewAccessKeyChangeAll(accountIdList,publicKey)
    fun viewAccessKeyChange(currentAccessKey:String) = nearService.viewAccessKeyChange(accountId, currentAccessKey)
    fun transactionStatus(txResultHash:String) = nearService.transactionStatus(txResultHash, accountId)
    fun viewAccount() = nearService.viewAccount(accountId)
    fun viewAccountChanges(accountIdList: List<String>, blockId:Int) = nearService.viewAccountChanges(accountIdList, blockId)
    fun viewContractCode() = nearService.viewContractCode(accountId)
    fun viewContractState() = nearService.viewContractState(accountId)
    fun viewContractStateChanges(accountIdList: List<String>, keyPrefix:String, blockId:Int) = nearService.viewContractStateChanges(accountIdList, keyPrefix, blockId)
    fun viewContractCodeChanges(accountIdList: List<String>, blockId:Int) = nearService.viewContractCodeChanges(accountIdList, blockId)
    fun getBlockDetails(blockId: Int) = nearService.blockDetails(blockId)
    fun getBlockChanges(blockId: Int) = nearService.blockChanges(blockId)
    fun getChunkHash(chunksHash:String) = nearService.chunkDetails(chunksHash)
    fun gasPrice(blockId: Int) = nearService.gasPrice(blockId)
    fun getGenesisConfiguration() = nearService.getGenesisConfig()
    fun getProtocolConfig() = nearService.getProtocolConfig()
    fun getNetworkStatus() = nearService.getNetworkStatus()
}
