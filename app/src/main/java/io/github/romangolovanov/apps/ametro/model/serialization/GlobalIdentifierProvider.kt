package io.github.romangolovanov.apps.ametro.model.serialization

class GlobalIdentifierProvider {
    private var segmentCounter = 0x10000000
    private var transferCounter = 0x20000000

    fun getSegmentUid(): Int = segmentCounter++
    fun getTransferUid(): Int = transferCounter++
}
