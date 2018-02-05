package edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.isotope;

public class IsotopeElementProperties {

    public double relativeAbundance;
    public double massDifferenceFromMonoisotopicIon;
    public String comment;

    public IsotopeElementProperties(double relativeAbundance, double massDifferenceFromMonoisotopicIon) {
        this.relativeAbundance = relativeAbundance;
        this.massDifferenceFromMonoisotopicIon = massDifferenceFromMonoisotopicIon;
    }

    public double massDifferenceFromMonoisotopicIon() {
        return massDifferenceFromMonoisotopicIon;
    }
}
