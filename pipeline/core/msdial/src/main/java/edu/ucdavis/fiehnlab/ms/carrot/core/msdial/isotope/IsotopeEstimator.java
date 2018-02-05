package edu.ucdavis.fiehnlab.ms.carrot.core.msdial.isotope;

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Ion;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.MSDialProcessingProperties;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.Peak;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.PeakAreaBean;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.isotope.CompoundProperties;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.utils.MassDiffDictionary;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.utils.MolecularFormulaUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by diego on 8/26/2016.
 */
public class IsotopeEstimator {
    private static Logger logger = LoggerFactory.getLogger(IsotopeEstimator.class);
    private static int MAX_ISOTOPE_NUMBER = 8;


    public void setIsotopeInformation(List<PeakAreaBean> detectedPeakAreas, MSDialProcessingProperties properties) {
logger.trace("Incoming peaks: " + detectedPeakAreas.size());
        detectedPeakAreas.sort(Comparator.comparing(PeakAreaBean::accurateMass));

        int spectrumMargin = 2;
        double rtMargin = 0.0275F;

        for (PeakAreaBean peak : detectedPeakAreas) {
            if (peak.isotopeWeightNumber >= 0)
                continue;

            int focusedScanNumber = peak.scanNumberAtPeakTop;
            double focusedMass = peak.accurateMass;
            double focusedRt = peak.rtAtPeakTop;

            int startScanIndex = getStartIndexForTargetMass(focusedMass - 0.0001, detectedPeakAreas);
            List<PeakAreaBean> isotopeCandidates = Collections.singletonList(peak);

            for (int i = startScanIndex; i < detectedPeakAreas.size(); i++) {
                if (detectedPeakAreas.get(i).peakID == peak.peakID)
                    continue;

                if (Math.abs(detectedPeakAreas.get(i).scanNumberAtPeakTop - focusedScanNumber) < spectrumMargin ||
                    Math.abs(detectedPeakAreas.get(i).rtAtPeakTop - focusedRt) < rtMargin)
                    continue;

                if (detectedPeakAreas.get(i).isotopeWeightNumber >= 0)
                    continue;

                if (detectedPeakAreas.get(i).accurateMass <= focusedMass)
                    continue;
                if (detectedPeakAreas.get(i).accurateMass > focusedMass + 8.1)
                    break;

                isotopeCandidates.add(detectedPeakAreas.get(i));
            }

            isotopeCalculationImproved(isotopeCandidates, properties);
        }
    }


    /**
     * @param isotopeCandidates
     * @param properties
     */
    private void isotopeCalculationImproved(List<PeakAreaBean> isotopeCandidates, MSDialProcessingProperties properties) {

        PeakAreaBean monoIsoPeak = isotopeCandidates.get(0);
        double ppm = MolecularFormulaUtility.ppmCalculator(200, 200 + properties.centroidMS1Tolerance);
        double accuracy = MolecularFormulaUtility.ppmToMassAccuracy(monoIsoPeak.accurateMass, ppm);
        double tolerance = accuracy;

        boolean isFinished = false;

        monoIsoPeak.isotopeWeightNumber = 0;
        monoIsoPeak.isotopeParentPeakID = monoIsoPeak.peakID;

        // Charge number check at M + 1
        int predChargeNumber = 1;

        for (int i = 1; i < isotopeCandidates.size(); i++) {
            PeakAreaBean isotopePeak = isotopeCandidates.get(i);

            if (isotopePeak.accurateMass > monoIsoPeak.accurateMass + MassDiffDictionary.C13_C12 + tolerance)
                break;

            for (int j = properties.maxChargeNumber; j >= 1; j--) {
                double predIsotopeMass = monoIsoPeak.accurateMass + MassDiffDictionary.C13_C12 / j;
                double diff = Math.abs(predIsotopeMass - isotopePeak.accurateMass);

                if (diff < tolerance) {
                    predChargeNumber = j;

                    if (j <= 3) {
                        break;
                    } else if (j == 4 || j == 5) {
                        double predNextIsotopeMass = monoIsoPeak.accurateMass + MassDiffDictionary.C13_C12 / (j - 1);
                        double nextDiff = Math.abs(predNextIsotopeMass - isotopePeak.accurateMass);

                        if (diff > nextDiff)
                            predChargeNumber = j - 1;
                        break;
                    } else if (j >= 6) {
                        double predNextIsotopeMass = monoIsoPeak.accurateMass + MassDiffDictionary.C13_C12 / (j - 1);
                        double nextDiff = Math.abs(predNextIsotopeMass - isotopePeak.accurateMass);

                        if (diff > nextDiff) {
                            predChargeNumber = j - 1;
                            diff = nextDiff;

                            predNextIsotopeMass = (double) monoIsoPeak.accurateMass + MassDiffDictionary.C13_C12 / (j - 2);
                            nextDiff = Math.abs(predNextIsotopeMass - isotopePeak.accurateMass);

                            if (diff > nextDiff) {
                                predChargeNumber = j - 2;
                                diff = nextDiff;
                            }
                        }

                        break;
                    }
                }

                if (predChargeNumber != 1)
                    break;
            }
        }

        monoIsoPeak.chargeNumber = predChargeNumber;

        IsotopePeak[] isotopeTemp = new IsotopePeak[properties.maxTraceNumber + 1];
        isotopeTemp[0] = new IsotopePeak(0, monoIsoPeak.accurateMass, monoIsoPeak.intensityAtPeakTop, monoIsoPeak.peakID);

        int reminderIndex = 1;

        for (int i = 1; i < properties.maxTraceNumber; i++) {
            double predIsotopicMass = monoIsoPeak.accurateMass + i * MassDiffDictionary.C13_C12 / predChargeNumber;

            for (int j = reminderIndex; j < isotopeCandidates.size(); j++) {
                PeakAreaBean isotopePeak = isotopeCandidates.get(j);

                if (predIsotopicMass - tolerance < isotopePeak.accurateMass &&
                    isotopePeak.accurateMass < predIsotopicMass + tolerance) {

                    if (isotopeTemp[i] == null) {
                        isotopeTemp[i] = new IsotopePeak(i, isotopePeak.accurateMass, isotopePeak.intensityAtPeakTop, j);
                    } else if (Math.abs(isotopeTemp[i].mz - predIsotopicMass) > Math.abs(isotopePeak.accurateMass - predIsotopicMass)) {
                        isotopeTemp[i].mz = isotopePeak.accurateMass;
                        isotopeTemp[i].intensity = isotopePeak.intensityAtPeakTop;
                        isotopeTemp[i].peakID = j;
                    }
                } else if (isotopePeak.accurateMass >= predIsotopicMass + tolerance) {
                    reminderIndex = j;

                    if (isotopeTemp[i] == null)
                        isFinished = true;

                    break;
                }
            }
            if (isFinished)
                break;
        }

        // TODO: MS-DIAL uses a simulated alkane profile to predict abundances of large peaks, which we will ignore for now
    }

    private class IsotopePeak {

        public int weightNumber;
        public double mz;
        public double intensity;
        public int peakID;

        public IsotopePeak(int weightNumber, double mz, double intensity, int peakID) {
            this.weightNumber = weightNumber;
            this.mz = mz;
            this.intensity = intensity;
            this.peakID = peakID;
        }
    }


    /**
     * @param isotopeCandidates
     * @param properties
     */
    private void isotopeCalculation(List<PeakAreaBean> isotopeCandidates, MSDialProcessingProperties properties) {

        PeakAreaBean monoIsoPeak = isotopeCandidates.get(0);
        double ppm = MolecularFormulaUtility.ppmCalculator(200, 200 + properties.centroidMS1Tolerance);
        double accuracy = MolecularFormulaUtility.ppmToMassAccuracy(monoIsoPeak.accurateMass, ppm);
        double tolerance = accuracy;

        boolean isFinished = false;

        monoIsoPeak.isotopeWeightNumber = 0;
        monoIsoPeak.isotopeParentPeakID = monoIsoPeak.peakID;

        double reminderIntensity = monoIsoPeak.intensityAtPeakTop;
        int reminderIndex = 1;

        // Charge number check at M + 1
        boolean isDoubleCharged = false;
        double isotopicMassDoubleCharged = monoIsoPeak.accurateMass + MassDiffDictionary.C13_C12 * 0.5;

        for (int i = 1; i < isotopeCandidates.size(); i++) {
            PeakAreaBean isotopePeak = isotopeCandidates.get(i);

            if (Math.abs(isotopicMassDoubleCharged - isotopePeak.accurateMass) <= tolerance) {
                if (monoIsoPeak.accurateMass > 900 || monoIsoPeak.intensityAtPeakTop > isotopePeak.intensityAtPeakTop) {
                    isDoubleCharged = true;
                }

                if (isotopePeak.accurateMass >= isotopicMassDoubleCharged + tolerance) {
                    break;
                }
            }
        }

        double chargeCoff = isDoubleCharged ? 0.50 : 1.0;
        monoIsoPeak.chargeNumber = isDoubleCharged ? 2 : 1;

        for (int i = 1; i <= MAX_ISOTOPE_NUMBER; i++) {
            double isotopicMass = monoIsoPeak.accurateMass + i * MassDiffDictionary.C13_C12 * chargeCoff;

            for (int j = reminderIndex; j < isotopeCandidates.size(); j++) {
                PeakAreaBean isotopePeak = isotopeCandidates.get(i);

                if (Math.abs(isotopicMass - isotopePeak.accurateMass) <= tolerance) {
                    if (monoIsoPeak.accurateMass < 900) {
                        if (reminderIntensity > isotopePeak.intensityAtPeakTop) {
                            isotopePeak.isotopeParentPeakID = monoIsoPeak.peakID;
                            isotopePeak.isotopeWeightNumber = i;
                            isotopePeak.chargeNumber = monoIsoPeak.chargeNumber;

                            reminderIntensity = isotopePeak.intensityAtPeakTop;
                            reminderIndex = j + 1;
                        } else {
                            isFinished = true;
                            break;
                        }
                    } else {
                        if (i <= 3 || reminderIntensity > isotopePeak.intensityAtPeakTop) {
                            isotopePeak.isotopeParentPeakID = monoIsoPeak.peakID;
                            isotopePeak.isotopeWeightNumber = i;
                            isotopePeak.chargeNumber = monoIsoPeak.chargeNumber;

                            reminderIntensity = isotopePeak.intensityAtPeakTop;
                            reminderIndex = j + 1;

                            if (i <= 3)
                                break;
                        } else {
                            isFinished = true;
                            break;
                        }
                    }
                }
            }

            if (isFinished)
                break;
        }
    }


    /**
     * @param targetMass
     * @param peakAreaBeans
     */
    private int getStartIndexForTargetMass(double targetMass, List<PeakAreaBean> peakAreaBeans) {
        int startIndex = 0, endIndex = peakAreaBeans.size() - 1;
        int counter = 0;

        while (counter < 5) {
            if (peakAreaBeans.get(startIndex).accurateMass <= targetMass && targetMass < peakAreaBeans.get((startIndex + endIndex) / 2).accurateMass) {
                endIndex = (startIndex + endIndex) / 2;
            } else if (peakAreaBeans.get((startIndex + endIndex) / 2).accurateMass <= targetMass && targetMass < peakAreaBeans.get(endIndex).accurateMass) {
                startIndex = (startIndex + endIndex) / 2;
            }

            counter++;
        }

        return startIndex;
    }


    /// <summary>
    /// peak list must be sorted by m/z (ordering)
    /// peak should be initialized by new Peak() { Mz = spec[0], Intensity = spec[1], Charge = 1, IsotopeFrag = false, Comment = "NA" }
    /// </summary>

    /**
     *
     * @param peaks
     * @param maxTraceNumber
     * @param maxChargeNumber
     * @param tolerance
     */
    public void msmsIsotopeRecognition(List<Peak> peaks, int maxTraceNumber, int maxChargeNumber, double tolerance) {

        for (int i = 0; i < peaks.size(); i++) {
            Peak peak = peaks.get(i);

            if (!peak.comment.equals("NA"))
                continue;

            peak.isotopeFragment = false;
            peak.comment = Integer.toString(i);

            // Charge state checking at M + 1
            int predChargeNumber = 1;

            for (int j = i + 1; j < peaks.size(); j++) {
                Peak isotopePeak = peaks.get(j);

                if (isotopePeak.mz > peak.mz + MassDiffDictionary.C13_C12 + tolerance)
                    break;
                if (!isotopePeak.comment.equals("NA"))
                    continue;

                for (int k = maxChargeNumber; k >= 1; k--) {
                    double predIsotopeMass = peak.mz + MassDiffDictionary.C13_C12 / k;
                    double diff = Math.abs(predIsotopeMass - isotopePeak.mz);

                    if (diff < tolerance) {
                        predChargeNumber = k;

                        if (k <= 3) {
                            break;
                        } else if (k == 4 || k == 5) {
                            double predNextIsotopeMass = peak.mz + MassDiffDictionary.C13_C12 / (k - 1);
                            double nextDiff = Math.abs(predNextIsotopeMass - isotopePeak.mz);

                            if (diff > nextDiff)
                                predChargeNumber = k - 1;

                            break;
                        } else if (k >= 6) {
                            double predNextIsotopeMass = peak.mz + MassDiffDictionary.C13_C12 / (k - 1);
                            double nextDiff = Math.abs(predNextIsotopeMass - isotopePeak.mz);

                            if (diff > nextDiff) {
                                predChargeNumber = k - 1;
                                diff = nextDiff;

                                predNextIsotopeMass = peak.mz + MassDiffDictionary.C13_C12 / (k - 2);
                                nextDiff = Math.abs(predNextIsotopeMass - isotopePeak.mz);

                                if (diff > nextDiff) {
                                    predChargeNumber = k - 2;
                                    diff = nextDiff;
                                }
                            }

                            break;
                        }
                    }
                }

                if (predChargeNumber != 1)
                    break;
            }

            peak.charge = predChargeNumber;


            // Isotope grouping till M + 8
            IsotopePeak[] isotopeTemps = new IsotopePeak[maxTraceNumber + 1];
            isotopeTemps[0] = new IsotopePeak(0, peak.mz, peak.intensity, i);

            int reminderIndex = i + 1;
            boolean isFinished = false;

            for (int j = 1; j <= maxTraceNumber; j++) {
                double predIsotopicMass = peak.mz + j * MassDiffDictionary.C13_C12 / (double)predChargeNumber;

                for (int k = reminderIndex; k < peaks.size(); k++) {
                    Peak isotopePeak = peaks.get(k);

                    if (!isotopePeak.comment.equals("NA"))
                        continue;

                    if (predIsotopicMass - tolerance < isotopePeak.mz && isotopePeak.mz < predIsotopicMass + tolerance) {
                        if (isotopeTemps[j] == null) {
                            isotopeTemps[j] = new IsotopePeak(j, isotopePeak.mz, isotopePeak.intensity, k);
                        } else {
                            if (Math.abs(isotopeTemps[j].mz - predIsotopicMass) > Math.abs(isotopePeak.mz - predIsotopicMass)) {
                                isotopeTemps[j].mz = isotopePeak.mz;
                                isotopeTemps[j].intensity = isotopePeak.intensity;
                                isotopeTemps[j].peakID = k;
                            }
                        }
                    } else if (isotopePeak.mz >= predIsotopicMass + tolerance) {
                        reminderIndex = k;

                        if (isotopeTemps[j] == null)
                            isFinished = true;

                        break;
                    }
                }

                if (isFinished)
                    break;
            }


            // Finalize and store
            double reminderIntensity = peak.intensity;
            double monoisotopicMass = peak.mz * predChargeNumber;
            String simulatedFormulaByAlkane = getSimulatedFormulaByAlkane(monoisotopicMass);

            // From here, simple decreasing will be expected for <= 800 Da
            // Simulated profiles by alkane formula will be projected to the real abundances for the peaks of more than 800 Da
            CompoundProperties simulatedIsotopicPeaks = null;

            if (monoisotopicMass > 800)
                simulatedIsotopicPeaks = new IsotopeRatioCalculator().getNominalIsotopeProperty(simulatedFormulaByAlkane, 9);

            for (int j = 1; j <= maxTraceNumber; j++) {
                if (isotopeTemps[j] == null) break;
                if (isotopeTemps[j].intensity <= 0) break;

                if (monoisotopicMass <= 800) {
                    if (isotopeTemps[j - 1].intensity > isotopeTemps[j].intensity) {
                        peaks.get(isotopeTemps[j].peakID).isotopeFragment = true;
                        peaks.get(isotopeTemps[j].peakID).charge = peak.charge;
                        peaks.get(isotopeTemps[j].peakID).comment = Integer.toString(i);
                    } else {
                        break;
                    }
                } else {
                    double expRatio = isotopeTemps[j].intensity / isotopeTemps[j - 1].intensity;
                    double simRatio = simulatedIsotopicPeaks.isotopeProfile.get(j).relativeAbundance / simulatedIsotopicPeaks.isotopeProfile.get(j - 1).relativeAbundance;

                    if (Math.abs(expRatio - simRatio) < 5.0) {
                        peaks.get(isotopeTemps[j].peakID).isotopeFragment = true;
                        peaks.get(isotopeTemps[j].peakID).charge = peak.charge;
                        peaks.get(isotopeTemps[j].peakID).comment = Integer.toString(i);
                    } else {
                        break;
                    }
                }
            }
        }
    }


    private String getSimulatedFormulaByAlkane(double mass) {
        double ch2Mass = 14.0;
        int carbonCount = (int)(mass / ch2Mass);
        int hCount = carbonCount * 2;

        if (carbonCount == 0 || carbonCount == 1)
            return "CH2";
        else {
            return "C"+ carbonCount +"H"+ hCount;
        }
    }
}
