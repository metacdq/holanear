package com.cindaku.holanear.di

import android.content.Context
import com.cindaku.holanear.activity.CallActivity
import com.cindaku.holanear.activity.ChooseLocationActivity
import com.cindaku.holanear.api.CindakuAPIService
import com.cindaku.holanear.module.*
import com.cindaku.holanear.service.SIPService
import com.cindaku.holanear.service.XMPPService
import com.cindaku.holanear.viewmodel.*
import com.knear.android.service.NearMainService
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
    NearModule::class,
    ChatRepositoryModule::class])
interface AppComponent {
    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): AppComponent
    }
    fun inject(activity: ChooseLocationActivity)
    fun inject(activity: CallActivity)
    fun inject(model: MainViewModel)
    fun inject(model: AttachmentViewModel)
    fun inject(model: ChatMasterViewModel)
    fun inject(model: SettingViewModel)
    fun inject(model: ContactViewModel)
    fun inject(model: CallViewModel)
    fun inject(model: ChatDetailViewModel)
    fun inject(model: ImagePreviewViewModel)
    fun inject(model: ChatViewModel)
    fun inject(service:XMPPService)
    fun inject(service:SIPService)
    fun near(): NearMainService
    fun xmppConnector(): XMPPConnector
    fun httpClient(): OkHttpClient
    fun sipConnector(): SIPConnector
    fun chatRepository(): ChatRepository
    fun cindakuService():  CindakuAPIService
}