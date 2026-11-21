package net.biodiscus.androidcontroltv.testing

import net.biodiscus.androidcontroltv.core.model.TvApp

fun createTvApp(packageName: String = "com.example.tv", label: String = "Example") =
    TvApp(packageName = packageName, activityName = "$packageName.MainActivity", label = label)
