package com.cindaku.holanear.di.module

import dagger.Binds
import dagger.Module

@Module
abstract class ChatRepositoryModule {
    @Binds
    abstract fun provideStorage(storage: ChatRepositoryImpl): ChatRepository
}