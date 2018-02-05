package edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.isotope;

import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.iupac.IUPACElement;

import java.util.ArrayList;
import java.util.List;

public class ElementProperties {

    public int iupacID;
    public String elementName;
    public int elementNumber;
    public List<IUPACElement> iupacElements = new ArrayList<>();
    public List<IsotopeElementProperties> isotopeProfile = new ArrayList<>();
}
