package edu.ucdavis.fiehnlab.utilities.minix.types;

import org.springframework.data.annotation.Id;

/**
 * Created by wohlgemuth on 11/16/16.
 */
public class BinBaseBin {


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

    private String bin;
    private String inchikey;

    public boolean isFame() {
        return fame;
    }

    public boolean getFame() {
        return fame;
    }

    public void setFame(boolean fame) {
        this.fame = fame;
    }

    private boolean fame;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Id
    private String id;

    public String getSplash() {
        return splash;
    }

    public void setSplash(String splash) {
        this.splash = splash;
    }

    private String splash;

    private Integer binid = 0;
    private Double retentionindex;

    public String getSampleName() {
        return sampleName;
    }

    public void setSampleName(String sampleName) {
        this.sampleName = sampleName;
    }

    private String sampleName = "";

    public Double getKovatsRetentionIndex() {
        return kovatsRetentionIndex;
    }

    public void setKovatsRetentionIndex(Double kovatsRetentionIndex) {
        this.kovatsRetentionIndex = kovatsRetentionIndex;
    }

    private Double kovatsRetentionIndex;

    private Double quantMass = 0.0;
    private String spectra;

    public Double getPurity() {
        return purity;
    }

    public void setPurity(Double purity) {
        this.purity = purity;
    }

    private Double purity;

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    private String group;

    public Double getBinUniqueMass() {
        return binUniqueMass;
    }

    public void setBinUniqueMass(Double binUniqueMass) {
        this.binUniqueMass = binUniqueMass;
    }

    @Override
    public String toString() {
        return "BinBaseBin{" +
                "  bin='" + bin + '\'' +
                ", inchikey='" + inchikey + '\'' +
                ", binid=" + binid +
                ", quantMass=" + quantMass +
                ", spectra='" + spectra + '\'' +
                ", binRetentionIndex=" + retentionindex +
                ", kovatsRetentionIndex=" + kovatsRetentionIndex +
                ", purity=" + purity +
                ", binUniqueMass=" + binUniqueMass +
                ", sample=" + sampleName +
                ", group=" + group +
                ", fame="+ fame +

                '}';
    }

    private Double binUniqueMass;

    public Double getQuantMass() {
        return quantMass;
    }

    public void setQuantMass(Double quantMass) {
        this.quantMass = quantMass;
    }

    public String getSpectra() {
        return spectra;
    }

    public void setSpectra(String spectra) {
        this.spectra = spectra;
    }


}
