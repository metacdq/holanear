package com.cindaku.holanear.di.module

import dagger.Binds
import dagger.Module

@Module
abstract class XMPPModule {
    @Binds
    abstract fun provideXMPPConnection(connector: KenulinXMPPConnector): XMPPConnector
}