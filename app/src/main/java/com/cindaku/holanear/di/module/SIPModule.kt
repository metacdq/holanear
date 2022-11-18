package com.cindaku.holanear.di.module

import dagger.Binds
import dagger.Module

@Module
abstract class SIPModule {
    @Binds
    abstract fun provideSIPConnector(connector: KenulinSIPConnector): SIPConnector
}