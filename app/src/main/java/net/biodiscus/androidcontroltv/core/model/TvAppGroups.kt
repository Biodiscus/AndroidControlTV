package net.biodiscus.androidcontroltv.core.model

data class TvAppGroups(val favorites: List<TvApp>, val others: List<TvApp>) {
    val isEmpty: Boolean = favorites.isEmpty() && others.isEmpty()
}

/** Splits the apps into the favorites and the rest. Both keep the order of [apps]. */
fun groupTvApps(apps: List<TvApp>, favoritePackageNames: Set<String>): TvAppGroups {
    val favorites = apps.filter { app -> app.packageName in favoritePackageNames }
    val others = apps.filterNot { app -> app.packageName in favoritePackageNames }
    return TvAppGroups(favorites, others)
}
