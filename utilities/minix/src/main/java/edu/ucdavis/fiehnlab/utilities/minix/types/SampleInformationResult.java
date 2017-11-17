package edu.ucdavis.fiehnlab.utilities.minix.types;

import org.springframework.data.annotation.Id;

import java.util.Collection;

/**
 * Created by wohlg on 8/17/2016.
 */
public class SampleInformationResult {

    public String getSample() {
        return sample;
    }

    public void setSample(String sample) {
        this.sample = sample;
    }

    public int getExperimentId() {
        return experimentId;
    }

    public void setExperimentId(int experimentId) {
        this.experimentId = experimentId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getOrgan() {
        return organ;
    }

    public void setOrgan(String organ) {
        this.organ = organ;
    }

    @Id
    String sample;


    int experimentId;

    protected boolean verbose = false;

    @Override
    public String toString() {
        StringBuffer buffer = new StringBuffer();

        buffer.append("SampleInformationResult{" +
                "sample='" + sample + '\'' +
                ", experimentId=" + experimentId +
                ", description='" + description + '\'' +
                ", species='" + species + '\'' +
                ", organ='" + organ + '\'' +
                ", label='" + label + '\'' +
                ", comment='" + comment + '\'' +
                ", class='" + className + '\'' +
                ", treatment='" + treatmentName + '\'' +
                ", treatment value='" + treatmentValue + '\'' +
                ", annotations= {");

        if (annotations != null) {

            if (verbose) {
                for (BinBaseResult x : annotations) {
                    buffer.append("\t" + x.toString() + "\n");
                }
                buffer.append("\t" + "treatment=" + "treatmentName" + "\n");
            } else {
                buffer.append(annotations.size());
            }
        }
        buffer.append('}');

        return buffer.toString();
    }

    String description;

    String species;

    String organ;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    String className;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    String label;

    String comment;

    public String getTreatmentName() {
        return treatmentName;
    }

    public void setTreatmentName(String treatmentName) {
        this.treatmentName = treatmentName;
    }

    public String getTreatmentValue() {
        return treatmentValue;
    }

    public void setTreatmentValue(String treatmentValue) {
        this.treatmentValue = treatmentValue;
    }

    String treatmentName;

    String treatmentValue;

    public Collection<BinBaseResult> getAnnotations() {
        return annotations;
    }

    public void setAnnotations(Collection<BinBaseResult> annotations) {
        this.annotations = annotations;
    }

    Collection<BinBaseResult> annotations;
}
