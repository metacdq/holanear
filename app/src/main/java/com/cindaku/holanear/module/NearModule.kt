package com.cindaku.holanear.module

import android.content.Context
import com.cindaku.holanear.APP_NAME
import com.cindaku.holanear.NETWORK_ID
import com.cindaku.holanear.RPC_ENDPOINT
import com.cindaku.holanear.WALLET_URL
import com.knear.android.service.NearMainService
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@Module
class NearModule {
    @Singleton
    @Provides
    fun provideNearService(context: Context): NearMainService {
        return NearMainService(context,
            networkId = NETWORK_ID,
            appName = APP_NAME,
            walletUrl = WALLET_URL,
            rcpEndpoint = RPC_ENDPOINT,
            fullAccess = false)
    }
}