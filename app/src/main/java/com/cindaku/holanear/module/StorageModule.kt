package com.cindaku.holanear.module

import dagger.Binds
import dagger.Module

@Module
abstract class StorageModule {

    // Makes Dagger provide SharedPreferencesStorage when a Storage type is requested
    @Binds
    abstract fun provideStorage(storage: SharedPreferencesStorage): Storage
}