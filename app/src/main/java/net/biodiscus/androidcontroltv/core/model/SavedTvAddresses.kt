package net.biodiscus.androidcontroltv.core.model

const val MAX_SAVED_TV_ADDRESSES = 10

fun addAsMostRecent(savedAddresses: List<TvAddress>, address: TvAddress): List<TvAddress> {
    val otherAddresses = savedAddresses.filter { it != address }
    return (listOf(address) + otherAddresses).take(MAX_SAVED_TV_ADDRESSES)
}

fun encodeSavedAddresses(addresses: List<TvAddress>): String =
    addresses.joinToString(separator = "\n") { it.displayText }

fun decodeSavedAddresses(text: String): List<TvAddress> =
    text.lines().mapNotNull(TvAddress::parse)
