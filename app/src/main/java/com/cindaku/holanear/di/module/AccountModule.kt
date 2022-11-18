package com.cindaku.holanear.di.module

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import com.cindaku.holanear.provider.ACCOUNT_TYPE
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AccountModule {
    @Singleton
    @Provides
    fun provideAccount(context: Context,storage: Storage): Account{
        val accountManager= AccountManager.get(context)
        return Account(storage.getString("jid"), ACCOUNT_TYPE).also {
            accountManager.addAccountExplicitly(it, storage.getString("password"), null)
        }
    }
}