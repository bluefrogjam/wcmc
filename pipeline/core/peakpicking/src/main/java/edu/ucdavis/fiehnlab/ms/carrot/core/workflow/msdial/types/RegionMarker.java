package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.types;

/**
 * Created by diego on 8/11/2016.
 */
public class RegionMarker {
	public int id;
	public int scanBegin;
	public int scanEnd;
	public RegionMarker(int id, int begin, int end) {
		this.id = id;
		this.scanBegin = begin;
		this.scanEnd = end;
	}

}
