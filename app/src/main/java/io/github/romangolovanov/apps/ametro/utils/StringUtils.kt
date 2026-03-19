package io.github.romangolovanov.apps.ametro.utils

import java.text.Collator

object StringUtils {

    private val COLLATOR: Collator = Collator.getInstance().also {
        it.strength = Collator.PRIMARY
    }

    private val SI_UNITS = arrayOf("k", "M", "G", "T", "P", "E")
    private val COMPUTING_UNITS = arrayOf("Ki", "Mi", "Gi", "Ti", "Pi", "Ei")

    @JvmStatic
    fun humanReadableByteCount(bytes: Long, si: Boolean): String {
        val unit = if (si) 1000 else 1024
        if (bytes < unit) return "$bytes B"
        val exp = (Math.log(bytes.toDouble()) / Math.log(unit.toDouble())).toInt()
        val pre = (if (si) SI_UNITS else COMPUTING_UNITS)[exp - 1]
        return String.format("%.1f %sB", bytes / Math.pow(unit.toDouble(), exp.toDouble()), pre)
    }

    @JvmStatic
    fun startsWithoutDiacritics(text: String, prefix: String): Boolean {
        val textLength = text.length
        val prefixLength = prefix.length
        if (textLength < prefixLength) {
            return false
        }
        val textPrefix = text.substring(0, prefixLength)
        return COLLATOR.compare(textPrefix, prefix) == 0
    }

    @JvmStatic
    fun isNullOrEmpty(value: String?): Boolean {
        return value == null || value.trim().isEmpty()
    }

    @JvmStatic
    fun humanReadableTime(totalSeconds: Int): String {
        val seconds = totalSeconds % 60
        val minutes = (totalSeconds / 60) % 60
        val hours = totalSeconds / 60 / 60

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }

        return String.format("%02d:%02d", minutes, seconds)
    }
}
