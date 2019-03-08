package edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.isotope;

import java.util.ArrayList;
import java.util.List;

public class CompoundProperties {

    public String elementName;
    public double accurateMass;
    public List<ElementProperties> elementProfile = new ArrayList<>();
    public List<IsotopeElementProperties> isotopeProfile = new ArrayList<>();

    @Override
    public String toString() {
        StringBuilder string = new StringBuilder();
        string.append("{eleName:").append(elementName).append("}");
        string.append(",{accMass:").append(accurateMass).append("}");
        string.append(",{elementProfile:[");
        for(ElementProperties ele : elementProfile) {
            string.append("{eleName:").append(ele.elementName).append("}");
        }
        string.append("]}");
        string.append(",{isotopeProfile:[");
        for(IsotopeElementProperties iso : isotopeProfile) {
            string.append("{relAbundance:").append(iso.relativeAbundance).append("}");
            string.append(",{massDifferenceFromMonoisotopicIon:").append(iso.massDifferenceFromMonoisotopicIon()).append("}");
            string.append(",{comment:").append(iso.comment).append("}");
        }
        string.append("]}");

        return string.toString();
    }
}
