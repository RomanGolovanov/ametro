package org.ametro.model.serialization;

public class GlobalIdentifierProvider {

    private int segmentCounter = 0x10000000;
    private int transferCounter = 0x20000000;

    public int getSegmentUid() {
        return segmentCounter++;
    }
    public int getTransferUid() {
        return transferCounter++;
    }
}
