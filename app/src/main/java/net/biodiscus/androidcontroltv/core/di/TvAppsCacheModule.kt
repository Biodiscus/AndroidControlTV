package net.biodiscus.androidcontroltv.core.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton
import net.biodiscus.androidcontroltv.core.cache.FileTvAppsCache
import net.biodiscus.androidcontroltv.core.cache.TvAppsCache

@Module
@InstallIn(SingletonComponent::class)
internal object TvAppsCacheModule {

    @Provides
    @Singleton
    fun provideTvAppsCache(@ApplicationContext context: Context): TvAppsCache =
        FileTvAppsCache(File(context.cacheDir, "tv_apps"))
}
