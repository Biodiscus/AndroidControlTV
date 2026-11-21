package net.biodiscus.androidcontroltv.core.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SavedTvDataStore

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SettingsDataStore
