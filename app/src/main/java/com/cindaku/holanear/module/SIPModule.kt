package com.cindaku.holanear.module

import dagger.Binds
import dagger.Module

@Module
abstract class SIPModule {
    @Binds
    abstract fun provideSIPConnector(connector: ChatSIPConnector): SIPConnector
}