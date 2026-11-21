package net.biodiscus.androidcontroltv.core.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.biodiscus.androidcontroltv.core.adb.DadbTvConnector
import net.biodiscus.androidcontroltv.core.adb.TvConnector
import net.biodiscus.androidcontroltv.core.repository.AdbTvRepository
import net.biodiscus.androidcontroltv.core.repository.TvRepository

@Module
@InstallIn(SingletonComponent::class)
internal interface CoreModule {

    @Binds
    fun bindTvRepository(repository: AdbTvRepository): TvRepository

    @Binds
    fun bindTvConnector(connector: DadbTvConnector): TvConnector
}
