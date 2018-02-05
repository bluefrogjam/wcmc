package edu.ucdavis.fiehnlab.ms.carrot.core.msdial.utils;

public class MolecularFormulaUtility {

    public static double ppmCalculator(double exactMass, double actualMass) {
        return (actualMass - exactMass) / exactMass * 1000000;
    }

    public static double ppmToMassAccuracy(double exactMass, double ppm) {
        return ppm * exactMass / 1000000.0;
    }
}
