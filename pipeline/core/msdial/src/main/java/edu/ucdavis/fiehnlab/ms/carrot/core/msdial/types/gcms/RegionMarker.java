package edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.gcms;

/**
 * Created by sajjan on 04/16/2018.
 */
public class RegionMarker {

    public int id;
    public int scanStart;
    public int scanEnd;

    public RegionMarker(int id, int scanStart, int scanEnd) {
        this.id = id;
        this.scanStart = scanStart;
        this.scanEnd = scanEnd;
    }
}
