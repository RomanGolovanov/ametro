package io.github.romangolovanov.apps.ametro.model.entities

class MapLocale(
    private val texts: Map<Int, String>,
    private val allTexts: Map<Int, List<String>>
) {
    fun getText(textId: Int): String? = texts[textId]
    fun getAllTexts(textId: Int): List<String>? = allTexts[textId]
}
