package com.cindaku.holanear.di.component

import android.accounts.Account
import android.content.Context
import com.cindaku.holanear.activity.*
import com.cindaku.holanear.api.KenulinAPIService
import com.cindaku.holanear.di.module.*
import com.cindaku.holanear.service.SIPService
import com.cindaku.holanear.service.XMPPService
import com.cindaku.holanear.ui.viewmodel.*
import dagger.BindsInstance
import dagger.Component
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Singleton
@Component(modules = [
    StorageModule::class,
    XMPPModule::class,
    SIPModule::class,
    APIServiceModule::class,
    RoomModule::class,
    AccountModule::class,
    ChatRepositoryModule::class])
interface AppComponent {
    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): AppComponent
    }
    fun inject(activity: ChooseLocationActivity)
    fun inject(activity: MainActivity)
    fun inject(activity: VerifyActivity)
    fun inject(activity: CallActivity)
    fun inject(activity: LoginActivity)
    fun inject(model: AttachmentViewModel)
    fun inject(model: ContactViewModel)
    fun inject(model: CallViewModel)
    fun inject(model: ChatDetailViewModel)
    fun inject(model: ImagePreviewViewModel)
    fun inject(model: ChatViewModel)
    fun inject(service:XMPPService)
    fun inject(service:SIPService)
    fun account(): Account
    fun xmppConnector(): XMPPConnector
    fun httpClient(): OkHttpClient
    fun sipConnector(): SIPConnector
    fun chatRepository(): ChatRepository
    fun kenulinService():  KenulinAPIService
}