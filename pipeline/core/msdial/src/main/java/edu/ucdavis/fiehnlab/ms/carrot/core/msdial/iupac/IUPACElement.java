package edu.ucdavis.fiehnlab.ms.carrot.core.msdial.iupac;

public class IUPACElement {

    public int iupacID;
    public String elementName;
    public int nominalMass;
    public double naturalRelativeAbundance;
    public double accurateMass;

    public IUPACElement(int iupacID, String elementName, int nominalMass, double naturalRelativeAbundance, double accurateMass) {
        this.iupacID = iupacID;
        this.elementName = elementName;
        this.nominalMass = nominalMass;
        this.naturalRelativeAbundance = naturalRelativeAbundance;
        this.accurateMass = accurateMass;
    }

    public IUPACElement(String[] fields) {
        this(Integer.parseInt(fields[0]), fields);
    }

    public IUPACElement(int iupacID, String[] fields) {
        this(iupacID, fields[1], Integer.parseInt(fields[2]), Double.parseDouble(fields[3]), Double.parseDouble(fields[4]));
    }
}
