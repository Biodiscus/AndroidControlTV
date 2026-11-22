package net.biodiscus.androidcontroltv.core.adb

internal fun createTextInputCommand(text: String): String =
    "input text ${quoteForShell(encodeSpacesForInputText(text))}"

private fun encodeSpacesForInputText(text: String): String = text.replace(" ", "%s")

private fun quoteForShell(text: String): String {
    val textWithEscapedQuotes = text.replace("'", "'\\''")
    return "'$textWithEscapedQuotes'"
}
