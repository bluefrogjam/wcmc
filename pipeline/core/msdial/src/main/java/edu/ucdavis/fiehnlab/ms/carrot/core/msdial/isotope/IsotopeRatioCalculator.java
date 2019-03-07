package edu.ucdavis.fiehnlab.ms.carrot.core.msdial.isotope;

import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.iupac.IUPACElement;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.iupac.IUPACReference;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.isotope.CompoundProperties;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.isotope.ElementProperties;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.isotope.IsotopeElementProperties;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.math.BasicMathematics;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.utils.LCMSDataAccessUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IsotopeRatioCalculator {

    private static Logger logger = LoggerFactory.getLogger(LCMSDataAccessUtility.class);


    public CompoundProperties getNominalIsotopeProperty(String elementName, int massFilter) {

        CompoundProperties compoundProperties = new CompoundProperties();
        compoundProperties.elementName = elementName;
        compoundProperties.elementProfile = getBasicCompoundElementProfile(elementName);

        if (compoundProperties.elementProfile.isEmpty())
            return null;

        setIupacReferenceInformation(compoundProperties);

        if (compoundProperties.elementProfile.isEmpty())
            return null;

        setNominalIsotopePropertyInformation(compoundProperties, massFilter);
        setFinalNominalIsotopeProfile(compoundProperties, massFilter);

        return compoundProperties;
    }


    private void matchElement(String formula, String regex, String elementName, List<ElementProperties> elementProfile) {
        Matcher m = Pattern.compile(regex).matcher(formula);

        if (m.find()) {
            ElementProperties elementProperties = new ElementProperties();
            elementProperties.elementName = elementName;

            if (m.group(1).isEmpty()) {
                elementProperties.elementNumber = 1;
            } else {
                elementProperties.elementNumber = Integer.parseInt(m.group(1));
            }

            elementProfile.add(elementProperties);
        }
    }

    private List<ElementProperties> getBasicCompoundElementProfile(String formula) {
        List<ElementProperties> elementProfile = new ArrayList<>();

        if (Character.isDigit(formula.charAt(0))) {
            logger.warn("The element composition name format is incorrect for "+ formula);
            return new ArrayList<>();
        }

        matchElement(formula, "C(?!a|d|e|l|o|r|s|u)([0-9]*)", "C", elementProfile);
        matchElement(formula, "H(?!e|f|g|o)([0-9]*)", "H", elementProfile);
        matchElement(formula, "N(?!a|b|d|e|i)([0-9]*)", "N", elementProfile);
        matchElement(formula, "O(?!s)([0-9]*)", "O", elementProfile);
        matchElement(formula, "S(?!b|c|e|i|m|n|r)([0-9]*)", "S", elementProfile);
        matchElement(formula, "Br([0-9]*)", "Br", elementProfile);
        matchElement(formula, "Cl([0-9]*)", "Cl", elementProfile);
        matchElement(formula, "F(?!e)([0-9]*)", "F", elementProfile);
        matchElement(formula, "I(?!n|r)([0-9]*)", "I", elementProfile);

        return elementProfile.isEmpty() ? new ArrayList<>() : elementProfile;
    }

    private void setIupacReferenceInformation(CompoundProperties compoundProperties) {
        double accurateMass = 0;

        for (ElementProperties elementProperties : compoundProperties.elementProfile) {
            if (IUPACReference.iupacElementsByName.containsKey(elementProperties.elementName)) {
                elementProperties.iupacElements = IUPACReference.iupacElementsByName.get(elementProperties.elementName);
                elementProperties.iupacID = elementProperties.iupacElements.get(0).iupacID;
                accurateMass += elementProperties.iupacElements.get(0).accurateMass * elementProperties.elementNumber;
            } else {
                logger.warn(elementProperties.elementName +" is not included in IUPAC reference");
                compoundProperties.elementProfile = new ArrayList<>();
                return;
            }
        }

        compoundProperties.accurateMass = accurateMass;
    }


    private void setNominalIsotopePropertyInformation(CompoundProperties compoundProperties, int massFilter) {
        for (ElementProperties elementProperties : compoundProperties.elementProfile) {
            elementProperties.isotopeProfile = getIsotopeElementProperty(elementProperties.iupacElements);
            elementProperties.isotopeProfile = getNominalIsotopeElementProperty(elementProperties.isotopeProfile, elementProperties.elementNumber, massFilter);
            elementProperties.isotopeProfile.sort(Comparator.comparing(IsotopeElementProperties::massDifferenceFromMonoisotopicIon));
        }
    }

    private List<IsotopeElementProperties> getIsotopeElementProperty(List<IUPACElement> iupacElements) {
        List<IsotopeElementProperties> isotopeElementPropertiesList = new ArrayList<>();

        double relativeAbundance, massDifference;

        for (IUPACElement iupacElement : iupacElements) {
            relativeAbundance = iupacElement.naturalRelativeAbundance / iupacElements.get(0).naturalRelativeAbundance;
            massDifference = iupacElement.accurateMass - iupacElements.get(0).accurateMass;
            isotopeElementPropertiesList.add(new IsotopeElementProperties(relativeAbundance, massDifference));
        }

        return isotopeElementPropertiesList;
    }

    private static List<IsotopeElementProperties> getNominalMultiplatedIsotopeElement(List<IsotopeElementProperties> isotopeProfileA, List<IsotopeElementProperties> isotopeProfileB, int filterMass) {
        List<IsotopeElementProperties> multipliedIsotopeProfile = new ArrayList<>();

        for (int i = 0; i < filterMass; i++)
            // TODO is setting the mass difference to i correct?
            multipliedIsotopeProfile.add(new IsotopeElementProperties(0,  i));

        for (IsotopeElementProperties isotopeA : isotopeProfileA) {
            for (IsotopeElementProperties isotopeB : isotopeProfileB) {
                int massDiff = (int)(isotopeA.massDifferenceFromMonoisotopicIon + isotopeB.massDifferenceFromMonoisotopicIon);

                if (massDiff <= filterMass - 1) {
                    multipliedIsotopeProfile.get(massDiff).relativeAbundance += isotopeA.relativeAbundance * isotopeB.relativeAbundance;
                }
            }
        }

        return multipliedIsotopeProfile;
    }

    private static List<IsotopeElementProperties> getNominalMultiplatedIsotopeElement(double relativeAbund, double massDiff, List<IsotopeElementProperties> isotopeProfile, int filterMass) {
        List<IsotopeElementProperties> multipliedIsotopeProfile = new ArrayList<>();

        for (int i = 0; i < filterMass; i++)
            multipliedIsotopeProfile.add(new IsotopeElementProperties(0,  0));

        for (IsotopeElementProperties isotope : isotopeProfile) {
            double massDifference = Math.round(massDiff + isotope.massDifferenceFromMonoisotopicIon);

            if (Math.abs(massDifference) <= filterMass - 1) {
                multipliedIsotopeProfile.get((int)massDifference).relativeAbundance += relativeAbund * isotope.relativeAbundance;
            }
        }

        return multipliedIsotopeProfile;
    }

    private void setFinalNominalIsotopeProfile(CompoundProperties compoundProperties, int massFilter) {
        List<IsotopeElementProperties> combinedIsotopeProfile = compoundProperties.elementProfile.get(0).isotopeProfile;

        for (int i = 1; i < compoundProperties.elementProfile.size(); i++)
            combinedIsotopeProfile = getNominalMultiplatedIsotopeElement(combinedIsotopeProfile, compoundProperties.elementProfile.get(i).isotopeProfile, massFilter);

        combinedIsotopeProfile.sort(Comparator.comparing(IsotopeElementProperties::massDifferenceFromMonoisotopicIon));
        compoundProperties.isotopeProfile = combinedIsotopeProfile;

        for (IsotopeElementProperties isotopeProperties : compoundProperties.isotopeProfile) {
            isotopeProperties.massDifferenceFromMonoisotopicIon += compoundProperties.accurateMass;
            isotopeProperties.relativeAbundance *= 100;
        }
    }


    private List<IsotopeElementProperties> getNominalIsotopeElementProperty(List<IsotopeElementProperties> isotopeProfile, int n, int filterMass) {
        return getNominalIsotopeElementProperty(isotopeProfile, n, 0, filterMass);
    }

    private List<IsotopeElementProperties> getNominalIsotopeElementProperty(List<IsotopeElementProperties> isotopeProfile, int n, int k, int filterMass) {
        List<IsotopeElementProperties> combinedIsotopeProfile = new ArrayList<>();

        for (int i = 0; i < filterMass; i++)
            combinedIsotopeProfile.add(new IsotopeElementProperties(0,  0));

        double relativeAbundance, massDifference;

        if (k == 0) {
            relativeAbundance = isotopeProfile.get(1).relativeAbundance;
            massDifference = isotopeProfile.get(1).massDifferenceFromMonoisotopicIon;
        } else {
            relativeAbundance = isotopeProfile.get(k + 1).relativeAbundance / isotopeProfile.get(k).relativeAbundance;
            massDifference = isotopeProfile.get(k + 1).massDifferenceFromMonoisotopicIon - isotopeProfile.get(k).massDifferenceFromMonoisotopicIon;
        }

        for (int i = 0; i <= n; i++) {
            relativeAbundance = BasicMathematics.binomialCoefficient(n, i) * Math.pow(relativeAbundance, i);
            massDifference = Math.round(massDifference * i);

            if (Math.abs(massDifference) > filterMass - 1)
                break;

            if (isotopeProfile.size() > 2 && k < isotopeProfile.size() - 2) {
                List<IsotopeElementProperties> subIsotopeProfile = getNominalIsotopeElementProperty(isotopeProfile, i, k + 1, filterMass);

                subIsotopeProfile = getNominalMultiplatedIsotopeElement(relativeAbundance, massDifference, subIsotopeProfile, filterMass);
                combinedIsotopeProfile = getNominalMergedIsotopeElement(combinedIsotopeProfile, subIsotopeProfile);
            } else {
                combinedIsotopeProfile.get((int)massDifference).relativeAbundance += relativeAbundance;
                combinedIsotopeProfile.get((int)massDifference).massDifferenceFromMonoisotopicIon = (int) massDifference;
            }
        }

        return combinedIsotopeProfile;
    }



    private static List<IsotopeElementProperties> getNominalMergedIsotopeElement(List<IsotopeElementProperties> isotopeProfileA, List<IsotopeElementProperties> isotopeProfileB) {

        for (int i = 0; i < isotopeProfileA.size(); i++) {
            isotopeProfileA.get(i).relativeAbundance += isotopeProfileB.get(i).relativeAbundance;
            isotopeProfileA.get(i).massDifferenceFromMonoisotopicIon = i;
        }

        return isotopeProfileA;
    }
}
