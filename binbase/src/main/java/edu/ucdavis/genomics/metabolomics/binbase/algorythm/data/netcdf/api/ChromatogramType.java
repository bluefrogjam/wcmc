package edu.ucdavis.genomics.metabolomics.binbase.algorythm.data.netcdf.api;


public enum ChromatogramType {
	TIC("Total Ion Current"), BASE_PEAK("Base Peak Intensity");

	private final String name;

	ChromatogramType(String name) {
		this.name = name;
	}

	public String toString() {
		return this.name;
	}
}
