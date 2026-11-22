package net.biodiscus.androidcontroltv.testing

import net.biodiscus.androidcontroltv.core.model.TvApp
import net.biodiscus.androidcontroltv.core.model.TvSystemApp

fun createTvApp(packageName: String = "com.example.tv", label: String = "Example") =
    TvApp(packageName = packageName, activityName = "$packageName.MainActivity", label = label)

fun createTvSystemApp(packageName: String = "com.android.example", label: String = "Example system app") =
    TvSystemApp(packageName = packageName, label = label)
