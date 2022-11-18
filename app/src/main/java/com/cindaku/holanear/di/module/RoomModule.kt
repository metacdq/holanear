package com.cindaku.holanear.di.module

import android.content.Context
import androidx.room.Room
import com.cindaku.holanear.APP_NAME
import com.cindaku.holanear.db.HolaNearDatabase
import com.cindaku.holanear.db.dao.CallDao
import com.cindaku.holanear.db.dao.ChatDao
import com.cindaku.holanear.db.dao.ChatMessageDao
import com.cindaku.holanear.db.dao.ContactDao
import dagger.Module
import dagger.Provides
import javax.inject.Singleton


@Module
class RoomModule {
    @Singleton
    @Provides
    fun provideKenulinDatabase(context: Context): HolaNearDatabase{
        return Room.databaseBuilder(context.applicationContext, HolaNearDatabase::class.java, APP_NAME)
                .allowMainThreadQueries()
                .build()
    }
    @Singleton
    @Provides
    fun provideCallDao(database: HolaNearDatabase): CallDao{
        return database.callDao()
    }
    @Singleton
    @Provides
    fun provideChatDao(database: HolaNearDatabase): ChatDao{
        return database.chatDao()
    }
    @Singleton
    @Provides
    fun provideContactDao(database: HolaNearDatabase): ContactDao{
        return database.contactDao()
    }
    @Singleton
    @Provides
    fun provideChhatMessageDao(database: HolaNearDatabase): ChatMessageDao{
        return database.chatMessageDao()
    }
}