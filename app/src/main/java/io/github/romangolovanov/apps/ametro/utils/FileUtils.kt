package io.github.romangolovanov.apps.ametro.utils

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader

object FileUtils {

    @JvmStatic
    @Throws(IOException::class)
    fun readAllText(inputStream: InputStream): String {
        val br = BufferedReader(InputStreamReader(inputStream))
        inputStream.use {
            val sb = StringBuilder()
            var line = br.readLine()
            while (line != null) {
                sb.append(line)
                sb.append("\n")
                line = br.readLine()
            }
            return sb.toString()
        }
    }
}
