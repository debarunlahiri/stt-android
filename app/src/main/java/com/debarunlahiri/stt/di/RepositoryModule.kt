package com.debarunlahiri.stt.di

import com.debarunlahiri.stt.data.repository.SttRepository
import com.debarunlahiri.stt.data.repository.SttRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindSttRepository(
        sttRepositoryImpl: SttRepositoryImpl
    ): SttRepository
}
