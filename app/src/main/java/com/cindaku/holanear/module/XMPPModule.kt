package com.cindaku.holanear.module

import dagger.Binds
import dagger.Module

@Module
abstract class XMPPModule {
    @Binds
    abstract fun provideXMPPConnection(connector: ChatXMPPConnector): XMPPConnector
}