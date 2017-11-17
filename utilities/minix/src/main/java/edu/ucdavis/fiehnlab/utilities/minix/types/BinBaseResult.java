package edu.ucdavis.fiehnlab.utilities.minix.types;

import java.util.Date;

/**
 * Created by wohlgemuth on 8/17/16.
 */
public class BinBaseResult {

	private String bin;
	private String inchikey;
	private Integer binid = 0;
	private Double retentionindex;
	private Double retentionTime = 0.0;
	private Boolean correctionFailed = false;
	private Double quantMass = 0.0;
	private String leco;
	private Date dateOfImport;
	private String spectra;
	private Double intensity = 0.0;
	private Double normalizedIntensity = 0.0;
	private Double purity;
	private Double similarity;
	private String group;
	Double binRetentionIndex;
	private Double uniqueMass;
	private Double binUniqueMass;
	private Double kovatsRetentionIndex;


	public String getBin() {
        return bin;
    }

    public void setBin(String bin) {
        this.bin = bin;
    }

    public String getInchikey() {
        return inchikey;
    }

    public void setInchikey(String inchikey) {
        this.inchikey = inchikey;
    }

    public Integer getBinid() {
        return binid;
    }

    public void setBinid(Integer binid) {
        this.binid = binid;
    }

    public Double getRetentionindex() {
        return retentionindex;
    }


    public void setRetentionindex(Double retentionindex) {
        this.retentionindex = retentionindex;
    }

    public Double getKovatsRetentionIndex() {
        return kovatsRetentionIndex;
    }

    public void setKovatsRetentionIndex(Double kovatsRetentionIndex) {
        this.kovatsRetentionIndex = kovatsRetentionIndex;
    }

    public Double getRetentionTime() {
        return retentionTime;
    }

    public void setRetentionTime(Double retentionTime) {
        this.retentionTime = retentionTime;
    }

    public Boolean getCorrectionFailed() {
        return correctionFailed;
    }

    public void setCorrectionFailed(Boolean correctionFailed) {
        this.correctionFailed = correctionFailed;
    }

	public Double getIntensity() {
        return intensity;
    }

    public void setIntensity(Double intensity) {
        this.intensity = intensity;
    }

    public Double getPurity() {
        return purity;
    }

    public void setPurity(Double purity) {
        this.purity = purity;
    }

    public Double getSimilarity() {
        return similarity;
    }

    public void setSimilarity(Double similarity) {
        this.similarity = similarity;
    }

	public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public Double getBinRetentionIndex() {
        return binRetentionIndex;
    }

    public void setBinRetentionIndex(Double binRetentionIndex) {
        this.binRetentionIndex = binRetentionIndex;
    }

    public Double getBinUniqueMass() {
        return binUniqueMass;
    }

    public void setBinUniqueMass(Double binUniqueMass) {
        this.binUniqueMass = binUniqueMass;
    }

    @Override
    public String toString() {
        return "BinBaseResult{" +
                "  bin='" + bin + '\'' +
                ", inchikey='" + inchikey + '\'' +
                ", binid=" + binid +
                ", retentionindex=" + retentionindex +
                ", retentionTime=" + retentionTime +
                ", correctionFailed=" + correctionFailed +
                ", quantMass=" + quantMass +
                ", leco='" + leco + '\'' +
                ", dateOfImport=" + dateOfImport +
                ", spectra='" + spectra + '\'' +
                ", purity=" + purity +
                ", similarity=" + similarity +
                ", binRetentionIndex=" + binRetentionIndex +
                ", binUniqueMass=" + binUniqueMass +
                ", group=" + group +

                '}';
    }

    public Double getUniqueMass() {
        return uniqueMass;
    }

    public void setUniqueMass(Double uniqueMass) {
        this.uniqueMass = uniqueMass;
    }

    public Double getQuantMass() {
        return quantMass;
    }

    public void setQuantMass(Double quantMass) {
        this.quantMass = quantMass;
    }

    public String getLeco() {
        return leco;
    }

    public void setLeco(String leco) {
        this.leco = leco;
    }

    public Date getDateOfImport() {
        return dateOfImport;
    }

    public void setDateOfImport(Date dateOfImport) {
        this.dateOfImport = dateOfImport;
    }

    public String getSpectra() {
        return spectra;
    }

    public void setSpectra(String spectra) {
        this.spectra = spectra;
    }

	public Double getNormalizedIntensity() {
		return normalizedIntensity;
	}

	public void setNormalizedIntensity(Double normalizedIntensity) {
		this.normalizedIntensity = normalizedIntensity;
	}

}
