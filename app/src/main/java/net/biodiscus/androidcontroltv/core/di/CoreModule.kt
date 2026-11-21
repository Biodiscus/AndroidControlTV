package net.biodiscus.androidcontroltv.core.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.biodiscus.androidcontroltv.core.adb.AdbTvAppLoader
import net.biodiscus.androidcontroltv.core.adb.DadbTvConnector
import net.biodiscus.androidcontroltv.core.adb.InstalledTvHelperApk
import net.biodiscus.androidcontroltv.core.adb.TvAppLoader
import net.biodiscus.androidcontroltv.core.adb.TvConnector
import net.biodiscus.androidcontroltv.core.adb.TvHelperApk
import net.biodiscus.androidcontroltv.core.repository.AdbTvRepository
import net.biodiscus.androidcontroltv.core.debug.DebugReportLogger
import net.biodiscus.androidcontroltv.core.debug.LogcatDebugReportLogger
import net.biodiscus.androidcontroltv.core.repository.DataStoreFavoriteAppsRepository
import net.biodiscus.androidcontroltv.core.repository.DataStorePowerSettingsRepository
import net.biodiscus.androidcontroltv.core.repository.DataStoreSavedTvRepository
import net.biodiscus.androidcontroltv.core.repository.FavoriteAppsRepository
import net.biodiscus.androidcontroltv.core.repository.PowerSettingsRepository
import net.biodiscus.androidcontroltv.core.repository.SavedTvRepository
import net.biodiscus.androidcontroltv.core.repository.TvRepository

@Module
@InstallIn(SingletonComponent::class)
internal interface CoreModule {

    @Binds
    fun bindTvRepository(repository: AdbTvRepository): TvRepository

    @Binds
    fun bindSavedTvRepository(repository: DataStoreSavedTvRepository): SavedTvRepository

    @Binds
    fun bindPowerSettingsRepository(repository: DataStorePowerSettingsRepository): PowerSettingsRepository

    @Binds
    fun bindFavoriteAppsRepository(repository: DataStoreFavoriteAppsRepository): FavoriteAppsRepository

    @Binds
    fun bindDebugReportLogger(logger: LogcatDebugReportLogger): DebugReportLogger

    @Binds
    fun bindTvAppLoader(loader: AdbTvAppLoader): TvAppLoader

    @Binds
    fun bindTvHelperApk(helperApk: InstalledTvHelperApk): TvHelperApk

    @Binds
    fun bindTvConnector(connector: DadbTvConnector): TvConnector
}
