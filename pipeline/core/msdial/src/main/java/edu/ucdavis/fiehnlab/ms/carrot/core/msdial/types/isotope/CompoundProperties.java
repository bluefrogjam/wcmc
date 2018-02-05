package edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.isotope;

import java.util.ArrayList;
import java.util.List;

public class CompoundProperties {

    public String elementName;
    public double accurateMass;
    public List<ElementProperties> elementProfile = new ArrayList<>();
    public List<IsotopeElementProperties> isotopeProfile = new ArrayList<>();
}
