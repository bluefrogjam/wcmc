package edu.ucdavis.fiehnlab.ms.carrot.core.msdial.deconvolution.gcms;

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Ion;
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.IonMode;
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.Feature;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.MSDialGCMSProcessingProperties;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.AccuracyType;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.Peak;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.PeakAreaBean;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.gcms.*;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.utils.DataAccessUtility;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.utils.SpectralCentroiding;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.utils.TypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Created by sajjan on 04/16/2018.
 */
public class GCMSDeconvolution {

    private static Logger logger = LoggerFactory.getLogger(GCMSDeconvolution.class);

    public List<MS1DeconvolutionResult> gcmsMS1Deconvolution(List<Feature> spectrumList, List<PeakAreaBean> peakAreaList, MSDialGCMSProcessingProperties properties) {
        peakAreaList = peakAreaList.stream()
                .sorted(Comparator.comparing(PeakAreaBean::scanNumberAtPeakTop)
                        .thenComparing(PeakAreaBean::accurateMass))
                .collect(Collectors.toList());

        // Get scan ID dictionary between raw scan number and MS1 chromatogram scan ID.
        Map<Integer, Integer> scanNumberMap = getRawScanNumberMap(spectrumList, properties.ionMode);

        // Maps the values of peak shape, symmetry, and quality of detected peaks into the array
        // where the length is equal to the scan number
        DeconvolutionBin[] deconvolutionBins = getGcmsBinArray(spectrumList, peakAreaList, scanNumberMap, properties.ionMode);

        // Apply matched filter to extract 'metabolite components'
        // where EIC peaks having slightly (1-2 scan point diff) different retention times are merged.
        double[] matchedFilterArray = getMatchedFilterArray(deconvolutionBins, properties.sigma);

        // Making model chromatograms by considering their peak qualities
        List<ModelChromatogram> modelChromatograms = getModelChromatograms(spectrumList, peakAreaList, deconvolutionBins, matchedFilterArray, scanNumberMap, properties);

        // Exclude duplicate model chromatograms which have the complete same retention time's peak tops
        modelChromatograms = getRefinedModelChromatograms(modelChromatograms);


        // Perform deconvolution at each model chromatogram area
        List<MS1DeconvolutionResult> ms1DecResults = new ArrayList<>();

        double minIntensity = Double.MAX_VALUE, maxIntensity = Double.MIN_VALUE;
        int counter = 0;

        for (int i = 0; i < modelChromatograms.size(); i++) {
            // Consider adjacent model chromatograms to be considered as 'co-eluting' metabolites
            ModelChromatogramVector modelChromVector = getModelChromatogramVector(i, modelChromatograms, deconvolutionBins);

            // To get trimming EIC chromatograms where the retention time range is equal to the range of model chromatogram vector
            List<List<Peak>> ms1Chromatograms = getMS1Chromatograms(spectrumList, modelChromVector, deconvolutionBins, modelChromatograms.get(i).chromScanOfPeakTop, properties);
            MS1DeconvolutionResult ms1DecResult = MS1DeconvolutionUtilities.getMS1DeconvolutionResult(modelChromVector, ms1Chromatograms);

            if (ms1DecResult != null && ms1DecResult.spectrum.size() > 0) {
                ms1DecResult.ms1DecID = counter;
                ms1DecResult = getRefinedMS1DeconvolutionResult(ms1DecResult, properties.accuracyType);
                ms1DecResults.add(ms1DecResult);

                if (ms1DecResult.basepeakHeight < minIntensity)
                    minIntensity = ms1DecResult.basepeakHeight;

                if (ms1DecResult.basepeakHeight > maxIntensity)
                    maxIntensity = ms1DecResult.basepeakHeight;

                counter++;
            }
        }

        for (MS1DeconvolutionResult ms1DecResult : ms1DecResults) {
            ms1DecResult.amplitudeScore = (ms1DecResult.basepeakHeight - minIntensity) / (maxIntensity - minIntensity);

            // Calculating purity
            double tic = TypeConverter.getJavaIonList(spectrumList.stream().filter(s -> s.scanNumber() == ms1DecResult.scanNumber).findFirst().get())
                    .stream().mapToDouble(Ion::intensity).sum();
            double eic = ms1DecResult.spectrum.stream().mapToDouble(Peak::intensity).sum();
            ms1DecResult.modelPeakPurity = eic / tic;
        }

        return ms1DecResults;
    }

    /**
     *
     * @param spectrumList
     * @param ionMode
     * @return
     */
    private Map<Integer, Integer> getRawScanNumberMap(List<Feature> spectrumList, IonMode ionMode) {
        Map<Integer, Integer> scanNumberMap = new HashMap<>();
        int counter = 0;

        for (Feature spectrum : spectrumList) {
            if (spectrum.associatedScan().get().msLevel() > 1 || !spectrum.ionMode().get().mode().equals(ionMode.mode()))
                continue;

            scanNumberMap.put(spectrum.scanNumber(), counter);
            counter++;
        }

        return scanNumberMap;
    }

    /**
     *
     * @param spectrumList
     * @param peakAreaList
     * @param scanNumberMap
     * @param ionMode
     * @return
     */
    private DeconvolutionBin[] getGcmsBinArray(List<Feature> spectrumList, List<PeakAreaBean> peakAreaList, Map<Integer, Integer> scanNumberMap, IonMode ionMode) {
        List<Feature> ms1SpectrumList = getMs1SpectrumList(spectrumList, ionMode);
        DeconvolutionBin[] gcmsDecBins = new DeconvolutionBin[ms1SpectrumList.size()];

        for (int i = 0; i < gcmsDecBins.length; i++) {
            gcmsDecBins[i] = new DeconvolutionBin();
            gcmsDecBins[i].rawScanNumber = ms1SpectrumList.get(i).scanNumber();
            gcmsDecBins[i].retentionTime = ms1SpectrumList.get(i).retentionTimeInMinutes();
        }

        for (int i = 0; i < peakAreaList.size(); i++) {
            PeakAreaBean peak = peakAreaList.get(i);
            PeakSpot model = new PeakSpot();
            model.peakSpotID = i;

            int scanBin = scanNumberMap.get(peak.ms1LevelDataPointNumber);

            if (peak.idealSlopeValue > 0.999) {
                model.quality = ModelQuality.HIGH;
            } else if (peak.idealSlopeValue > 0.9) {
                model.quality = ModelQuality.MEDIUM;
            } else {
                model.quality = ModelQuality.LOW;
            }

            if (model.quality == ModelQuality.HIGH || model.quality == ModelQuality.MEDIUM)
                gcmsDecBins[scanBin].totalSharpnessValue += peak.shapenessValue;

            gcmsDecBins[scanBin].peakSpots.add(model);
        }

        return gcmsDecBins;
    }

    /**
     * Filters the raw data looking for ms1 level scans that match the desired polarity
     *
     * @param spectrumList list of raw scans
     * @param ionMode desired scan polarity
     * @return a list of filtered raw ms1 scans
     */
    private List<Feature> getMs1SpectrumList(List<Feature> spectrumList, IonMode ionMode) {
        return spectrumList.stream()
                .filter(s -> s.associatedScan().get().msLevel() == 1)
                .filter(s -> s.ionMode().get().mode().equals(ionMode.mode()))
                .collect(Collectors.toList());
    }

    /**
     * Get an array of 'normal distribution' coefficients
     *
     * @param gcmsDecBinArray
     * @param sigma
     * @return
     */
    private double[] getMatchedFilterArray(DeconvolutionBin[] gcmsDecBinArray, double sigma) {
        double halfPoint = 10.0; // currently this value should be enough for GC
        double[] matchedFilterArray = new double[gcmsDecBinArray.length];
        double[] matchedFilterCoefficient = new double[2 * (int) halfPoint + 1];

        for (int i = 0; i < matchedFilterCoefficient.length; i++) {
            matchedFilterCoefficient[i] = (1 - Math.pow((-halfPoint + i) / sigma, 2)) * Math.exp(-0.5 * Math.pow((-halfPoint + i) / sigma, 2));
        }

        for (int i = 0; i < gcmsDecBinArray.length; i++) {
            double sum = 0.0;

            for (int j = -1 * (int) halfPoint; j <= (int) halfPoint; j++) {
                if (i + j < 0) {
                    sum += 0;
                } else if (i + j > gcmsDecBinArray.length - 1) {
                    sum += 0;
                } else {
                    sum += gcmsDecBinArray[i + j].totalSharpnessValue * matchedFilterCoefficient[(int) (j + halfPoint)];
                }
            }

            matchedFilterArray[i] = sum;
        }

        return matchedFilterArray;
    }

    /**
     * get model chromatograms by applying coefficients
     *
     * @param spectra         list of spectra from unprocess data
     * @param detectedPeaks   list of detected peaks
     * @param gcmsDecBins
     * @param scanNumberMap
     * @return
     */
    private List<ModelChromatogram> getModelChromatograms(List<Feature> spectra, List<PeakAreaBean> detectedPeaks,
                                                                 DeconvolutionBin[] gcmsDecBins, double[] matchedFilterArray,
                                                                 Map<Integer, Integer> scanNumberMap,
                                                                 MSDialGCMSProcessingProperties properties) {

        List<RegionMarker> regionMarkers = getRegionMarkers(matchedFilterArray);
        List<ModelChromatogram> modelChromatograms = new ArrayList<>();

        for (RegionMarker region : regionMarkers) {
            List<PeakAreaBean> peakAreas = new ArrayList<>();
            for (int i = region.scanStart; i <= region.scanEnd; i++) {
                for (PeakSpot peakSpot : gcmsDecBins[i].peakSpots.stream().filter(n -> n.quality == ModelQuality.HIGH).collect(Collectors.toList())) {
                    peakAreas.add(detectedPeaks.get(peakSpot.peakSpotID));
                }
            }

            if (peakAreas.size() == 0) {
                for (int i = region.scanStart; i <= region.scanEnd; i++) {
                    for (PeakSpot peakSpot : gcmsDecBins[i].peakSpots.stream().filter(n -> n.quality == ModelQuality.MEDIUM).collect(Collectors.toList())) {
                        peakAreas.add(detectedPeaks.get(peakSpot.peakSpotID));
                    }
                }
            }

            ModelChromatogram modelChrom = getModelChromatogram(spectra, peakAreas, gcmsDecBins, scanNumberMap, properties);
            if (modelChrom != null) {
                modelChromatograms.add(modelChrom);
            }
        }

        return modelChromatograms;
    }

    /**
     *
     * @param matchedFilterArray
     * @return
     */
    private List<RegionMarker> getRegionMarkers(double[] matchedFilterArray) {
        List<RegionMarker> regionMarkers = new ArrayList<>();
        int scanBegin = 0;
        int margin = 5;
        boolean scanBeginFlag = false;

        for (int i = margin; i < matchedFilterArray.length - margin; i++) {
            if (matchedFilterArray[i] > 0 && matchedFilterArray[i - 1] < matchedFilterArray[i] && !scanBeginFlag) {
                scanBegin = i;
                scanBeginFlag = true;
            } else if (scanBeginFlag) {
                if (matchedFilterArray[i] <= 0) {
                    regionMarkers.add(new RegionMarker(regionMarkers.size(), scanBegin, i - 1));
                    scanBeginFlag = false;
                } else if (matchedFilterArray[i - 1] > matchedFilterArray[i] && matchedFilterArray[i] < matchedFilterArray[i + 1] && matchedFilterArray[i] >= 0) {
                    regionMarkers.add(new RegionMarker(regionMarkers.size(), scanBegin, i));
                    scanBegin = i + 1;
                    scanBeginFlag = true;
                    i++;
                }
            }
        }

        return regionMarkers;
    }

    /**
     * find a model chromatogram
     *
     * @param spectra
     * @param detectedPeaks
     * @param deconvolutionBins
     * @param scanNumberMap
     * @return
     */
    private ModelChromatogram getModelChromatogram(List<Feature> spectra, List<PeakAreaBean> detectedPeaks,
                                                          DeconvolutionBin[] deconvolutionBins, Map<Integer, Integer> scanNumberMap,
                                                          MSDialGCMSProcessingProperties properties) {
        if (detectedPeaks.isEmpty())
            return null;

        double maxSharpnessValue = detectedPeaks.stream().map(n -> n.shapenessValue * n.intensityAtPeakTop).max(Comparator.comparing(Double::valueOf)).get();
        double maxIdealSlopeValue = detectedPeaks.stream().map(n -> n.idealSlopeValue).max(Comparator.comparing(Double::valueOf)).get();

        ModelChromatogram modelChrom = new ModelChromatogram(maxSharpnessValue, maxIdealSlopeValue);
        boolean firstFlag = false;

        List<Peak> peakList = new ArrayList<>();
        List<List<Peak>> peakLists = new ArrayList<>();
        List<Peak> baselineCorrectedPeakList;

        List<PeakAreaBean> sortedPeaks = detectedPeaks.stream()
                .filter(n -> n.shapenessValue * n.intensityAtPeakTop >= maxSharpnessValue * 0.9)
                .sorted(Comparator.comparingDouble(n -> -n.shapenessValue * n.intensityAtPeakTop)) // Sort by descending sharpness
                .collect(Collectors.toList());

        for (PeakAreaBean peak : sortedPeaks) {
            if (!firstFlag) {
                modelChrom.rawScanOfPeakTop = peak.ms1LevelDataPointNumber;
                modelChrom.chromScanOfPeakTop = scanNumberMap.get(modelChrom.rawScanOfPeakTop);
                modelChrom.chromScanOfPeakLeft = modelChrom.chromScanOfPeakTop - (peak.scanNumberAtPeakTop - peak.scanNumberAtLeftPeakEdge);
                modelChrom.chromScanOfPeakRight = modelChrom.chromScanOfPeakTop + (peak.scanNumberAtRightPeakEdge - peak.scanNumberAtPeakTop);
                modelChrom.modelMasses.add(peak.accurateMass);
                modelChrom.sharpnessValue = peak.shapenessValue;

                peakList = getTrimedAndSmoothedPeaklist(spectra, modelChrom.chromScanOfPeakLeft, modelChrom.chromScanOfPeakRight, deconvolutionBins, peak.accurateMass(), properties);
                baselineCorrectedPeakList = getBaselineCorrectedPeaklist(peakList, modelChrom.chromScanOfPeakTop - modelChrom.chromScanOfPeakLeft);
                peakLists.add(baselineCorrectedPeakList);
                firstFlag = true;
            } else {
                modelChrom.modelMasses.add(peak.accurateMass);
                peakList = getTrimedAndSmoothedPeaklist(spectra, modelChrom.chromScanOfPeakLeft, modelChrom.chromScanOfPeakRight, deconvolutionBins, peak.accurateMass(), properties);
                baselineCorrectedPeakList = getBaselineCorrectedPeaklist(peakList, modelChrom.chromScanOfPeakTop - modelChrom.chromScanOfPeakLeft);
                peakLists.add(baselineCorrectedPeakList);
            }
        }

        double mzCount = modelChrom.modelMasses.size();

        for (Peak peak : peakLists.get(0)) {
            modelChrom.peaks.add(new Peak(peak.scanNumber, peak.retentionTime, peak.mz, peak.intensity / mzCount));
        }

        if (peakList.size() > 1) {
            for (int i = 1; i < peakLists.size(); i++) {
                for (int j = 0; j < peakLists.get(i).size(); j++) {
                    modelChrom.peaks.get(j).intensity += peakLists.get(i).get(j).intensity /= mzCount;
                }
            }
        }

        return getRefinedModelChromatogram(modelChrom, deconvolutionBins, properties.averagePeakWidth);
    }

    /**
     *
     * @param modelChromatogram
     * @param deconvolutionBins
     * @param averagePeakWidth
     * @return
     */
    private ModelChromatogram getRefinedModelChromatogram(ModelChromatogram modelChromatogram, DeconvolutionBin[] deconvolutionBins, int averagePeakWidth) {
        double maxIntensity = Double.MIN_VALUE;
        int peakTopID = -1, peakLeftID = -1, peakRightID = -1;

        for (int i = 0; i < modelChromatogram.peaks.size(); i++) {
            if (modelChromatogram.peaks.get(i).intensity > maxIntensity) {
                maxIntensity = modelChromatogram.peaks.get(i).intensity;
                peakTopID = i;
            }
        }

        modelChromatogram.maximumPeakTopValue = maxIntensity;

        // left spike check
        for (int i = peakTopID; i > 0; i--) {
            if (peakTopID - i < averagePeakWidth * 0.5)
                continue;

            if (modelChromatogram.peaks.get(i - 1).intensity >= modelChromatogram.peaks.get(i).intensity) {
                peakLeftID = i;
                break;
            }
        }

        if (peakLeftID < 0)
            peakLeftID = 0;

        // right spike check
        for (int i = peakTopID; i < modelChromatogram.peaks.size() - 1; i++) {
            if (i - peakTopID < averagePeakWidth * 0.5)
                continue;

            if (modelChromatogram.peaks.get(i).intensity <= modelChromatogram.peaks.get(i + 1).intensity) {
                peakRightID = i;
                break;
            }
        }

        if (peakRightID < 0)
            peakRightID = modelChromatogram.peaks.size() - 1;

        modelChromatogram.chromScanOfPeakTop = peakTopID + modelChromatogram.chromScanOfPeakLeft;
        modelChromatogram.chromScanOfPeakRight = peakRightID + modelChromatogram.chromScanOfPeakLeft;
        modelChromatogram.chromScanOfPeakLeft = peakLeftID + modelChromatogram.chromScanOfPeakLeft;
        modelChromatogram.rawScanOfPeakTop = deconvolutionBins[modelChromatogram.chromScanOfPeakTop].rawScanNumber;

        List<Peak> peaks = new ArrayList<>();

        for (int i = peakLeftID; i <= peakRightID; i++)
            peaks.add(modelChromatogram.peaks.get(i));

        modelChromatogram.peaks = peaks;

        // final curation
        if (peakTopID - peakLeftID < 3 || peakRightID - peakTopID < 3)
            return null;

        return modelChromatogram;
    }

    /**
     *
     * @param spectrumList
     * @param chromScanOfPeakLeft
     * @param chromScanOfPeakRight
     * @param deconvolutionBins
     * @param focusedMass
     * @param properties
     * @return
     */
    private List<Peak> getTrimedAndSmoothedPeaklist(List<Feature> spectrumList, int chromScanOfPeakLeft, int chromScanOfPeakRight,
                                                           DeconvolutionBin[] deconvolutionBins, double focusedMass,
                                                           MSDialGCMSProcessingProperties properties) {
        List<Peak> peakList = new ArrayList<>();

        int leftRemainder = 0, rightRemainder = 0;
        double massTolerance = properties.massAccuracy;

        int chromLeft = chromScanOfPeakLeft - properties.smoothingLevel;
        int chromRight = chromScanOfPeakRight + properties.smoothingLevel;

        if (chromLeft < 0) {
            leftRemainder = properties.smoothingLevel - chromScanOfPeakLeft;
            chromLeft = 0;
        }

        if (chromRight > deconvolutionBins.length - 1) {
            rightRemainder = chromScanOfPeakRight + properties.smoothingLevel - (deconvolutionBins.length - 1);
            chromRight = deconvolutionBins.length - 1;
        }

        for (int i = chromLeft; i <= chromRight; i++) {
            int rawScan = deconvolutionBins[i].rawScanNumber;
            Feature spectrum = spectrumList.get(rawScan);
            List<Ion> massSpectra = TypeConverter.getJavaIonList(spectrum);

            float sum = 0;
            double maxIntensityMz = Double.MIN_VALUE;
            double maxMass = focusedMass;

            int startIndex = DataAccessUtility.getMs1StartIndex(focusedMass, massTolerance, massSpectra);

            for (int j = startIndex; j < massSpectra.size(); j++) {
                if (massSpectra.get(j).mass() < focusedMass - massTolerance) {
                    continue;
                } else if (focusedMass - massTolerance <= massSpectra.get(j).mass() && massSpectra.get(j).mass() < focusedMass + massTolerance) {
                    sum += massSpectra.get(j).intensity();
                    if (maxIntensityMz < massSpectra.get(j).intensity()) {
                        maxIntensityMz = massSpectra.get(j).intensity();
                        maxMass = massSpectra.get(j).mass();
                    }
                } else if (massSpectra.get(j).mass() >= focusedMass + massTolerance) {
                    break;
                }
            }

            if (spectrumList.get(0).scanNumber() == 0)
                peakList.add(new Peak(spectrum.scanNumber(), spectrum.retentionTimeInMinutes(), maxMass, sum));
            else
                peakList.add(new Peak(spectrum.scanNumber() - 1, spectrum.retentionTimeInMinutes(), maxMass, sum));
        }

        List<Peak> smoothedPeakList = DataAccessUtility.getSmoothedPeaks(peakList, properties.smoothingMethod, properties.smoothingLevel);

        for (int i = 0; i < properties.smoothingLevel - leftRemainder; i++)
            smoothedPeakList.remove(0);
        for (int i = 0; i < properties.smoothingLevel - rightRemainder; i++)
            smoothedPeakList.remove(smoothedPeakList.size() - 1);

        return smoothedPeakList;
    }

    /**
     *
     * @param peakList
     * @param peakTop
     * @return
     */
    private List<Peak> getBaselineCorrectedPeaklist(List<Peak> peakList, int peakTop) {
        List<Peak> baselineCorrectedPeaklist = new ArrayList<>();

        // Find local minimum of left and right edge
        int minimumLeftID = 0;
        double minimumValue = Double.MAX_VALUE;

        for (int i = peakTop; i >= 0; i--) {
            if (peakList.get(i).intensity < minimumValue) {
                minimumValue = peakList.get(i).intensity;
                minimumLeftID = i;
            }
        }

        int minimumRightID = peakList.size() - 1;
        minimumValue = Double.MAX_VALUE;

        for (int i = peakTop; i < peakList.size(); i++) {
            if (peakList.get(i).intensity < minimumValue) {
                minimumValue = peakList.get(i).intensity;
                minimumRightID = i;
            }
        }

        double coeff = (peakList.get(minimumRightID).intensity - peakList.get(minimumLeftID).intensity) /
                (peakList.get(minimumRightID).retentionTime - peakList.get(minimumLeftID).retentionTime);
        double intercept = (peakList.get(minimumRightID).retentionTime * peakList.get(minimumLeftID).intensity - peakList.get(minimumLeftID).retentionTime * peakList.get(minimumRightID).intensity) /
                (peakList.get(minimumRightID).retentionTime - peakList.get(minimumLeftID).retentionTime);

        for (Peak peak : peakList) {
            float correctedIntensity = peak.intensity - (int) (coeff * peak.retentionTime + intercept);

            if (correctedIntensity >= 0) {
                baselineCorrectedPeaklist.add(new Peak(peak.scanNumber, peak.retentionTime, peak.mz, correctedIntensity));
            } else {
                baselineCorrectedPeaklist.add(new Peak(peak.scanNumber, peak.retentionTime, peak.mz, 0));
            }
        }

        return baselineCorrectedPeaklist;
    }

    /**
     *
     * @param modelChromatograms
     * @return
     */
    private List<ModelChromatogram> getRefinedModelChromatograms(List<ModelChromatogram> modelChromatograms) {
        modelChromatograms = modelChromatograms.stream().sorted(Comparator.comparing(ModelChromatogram::chromScanOfPeakTop)).collect(Collectors.toList());

        List<ModelChromatogram> chromatograms = new ArrayList<>();
        chromatograms.add(modelChromatograms.get(0));

        for (int i = 1; i < modelChromatograms.size(); i++) {
            if (chromatograms.get(chromatograms.size() - 1).chromScanOfPeakTop == modelChromatograms.get(i).chromScanOfPeakTop) {
                if (chromatograms.get(chromatograms.size() - 1).maximumPeakTopValue < modelChromatograms.get(i).maximumPeakTopValue) {
                    chromatograms.remove(chromatograms.size() - 1);
                    chromatograms.add(modelChromatograms.get(i));
                }
            } else {
                chromatograms.add(modelChromatograms.get(i));
            }
        }

        return chromatograms;
    }

    /**
     *
     * @param modelID
     * @param modelChromatograms
     * @param deconvolutionBins
     * @return
     */
    private ModelChromatogramVector getModelChromatogramVector(int modelID, List<ModelChromatogram> modelChromatograms, DeconvolutionBin[] deconvolutionBins) {
        int modelChromLeft = modelChromatograms.get(modelID).chromScanOfPeakLeft;
        int modelChromRight = modelChromatograms.get(modelID).chromScanOfPeakRight;

        boolean isTwoLeftModel = false, isOneLeftModel = false, isTwoRightModel = false, isOneRightModel = false;

        if (modelID > 1 && modelChromatograms.get(modelID - 2).chromScanOfPeakRight > modelChromLeft)
            isTwoLeftModel = true;
        if (modelID > 0 && modelChromatograms.get(modelID - 1).chromScanOfPeakRight > modelChromLeft)
            isOneLeftModel = true;
        if (modelID < modelChromatograms.size() - 2 && modelChromatograms.get(modelID + 2).chromScanOfPeakLeft < modelChromRight)
            isTwoRightModel = true;
        if (modelID < modelChromatograms.size() - 1 && modelChromatograms.get(modelID + 1).chromScanOfPeakLeft < modelChromRight)
            isOneRightModel = true;

        if (isTwoLeftModel)
            modelChromLeft = Math.min(modelChromatograms.get(modelID).chromScanOfPeakLeft, Math.min(modelChromatograms.get(modelID - 2).chromScanOfPeakLeft, modelChromatograms.get(modelID - 1).chromScanOfPeakLeft));
        else if (isOneLeftModel)
            modelChromLeft = Math.min(modelChromatograms.get(modelID).chromScanOfPeakLeft, modelChromatograms.get(modelID - 1).chromScanOfPeakLeft);

        if (isTwoRightModel)
            modelChromRight = Math.max(modelChromatograms.get(modelID).chromScanOfPeakRight, Math.max(modelChromatograms.get(modelID + 2).chromScanOfPeakRight, modelChromatograms.get(modelID + 1).chromScanOfPeakRight));
        else if (isOneRightModel)
            modelChromRight = Math.max(modelChromatograms.get(modelID).chromScanOfPeakRight, modelChromatograms.get(modelID + 1).chromScanOfPeakRight);

        ModelChromatogramVector modelVector = new ModelChromatogramVector();
        modelVector.modelMasses = modelChromatograms.get(modelID).modelMasses;

        for (int i = modelChromLeft; i <= modelChromRight; i++) {
            modelVector.chromatogramScanList.add(i);
            modelVector.rawScanList.add(deconvolutionBins[i].rawScanNumber);
            modelVector.retentionTimeList.add(deconvolutionBins[i].retentionTime);

            if (modelChromatograms.get(modelID).chromScanOfPeakLeft > i || modelChromatograms.get(modelID).chromScanOfPeakRight < i)
                modelVector.targetIntensityArray.add(0.0f);
            else
                modelVector.targetIntensityArray.add(modelChromatograms.get(modelID).peaks.get(i - modelChromatograms.get(modelID).chromScanOfPeakLeft).intensity);

            if (isTwoLeftModel) {
                if (modelChromatograms.get(modelID - 2).chromScanOfPeakLeft > i || modelChromatograms.get(modelID - 2).chromScanOfPeakRight < i)
                    modelVector.twoLeftIntensityArray.add(0.0f);
                else
                    modelVector.twoLeftIntensityArray.add(modelChromatograms.get(modelID - 2).peaks.get(i - modelChromatograms.get(modelID - 2).chromScanOfPeakLeft).intensity);
            }

            if (isOneLeftModel || isTwoLeftModel) {
                if (modelChromatograms.get(modelID - 1).chromScanOfPeakLeft > i || modelChromatograms.get(modelID - 1).chromScanOfPeakRight < i)
                    modelVector.oneLeftIntensityArray.add(0.0f);
                else
                    modelVector.oneLeftIntensityArray.add(modelChromatograms.get(modelID - 1).peaks.get(i - modelChromatograms.get(modelID - 1).chromScanOfPeakLeft).intensity);
            }

            if (isTwoRightModel) {
                if (modelChromatograms.get(modelID + 2).chromScanOfPeakLeft > i || modelChromatograms.get(modelID + 2).chromScanOfPeakRight < i)
                    modelVector.twoRightInetnsityArray.add(0.0f);
                else
                    modelVector.twoRightInetnsityArray.add(modelChromatograms.get(modelID + 2).peaks.get(i - modelChromatograms.get(modelID + 2).chromScanOfPeakLeft).intensity);
            }

            if (isOneRightModel || isTwoRightModel) {
                if (modelChromatograms.get(modelID + 1).chromScanOfPeakLeft > i || modelChromatograms.get(modelID + 1).chromScanOfPeakRight < i)
                    modelVector.oneRightIntensityArray.add(0.0f);
                else
                    modelVector.oneRightIntensityArray.add(modelChromatograms.get(modelID + 1).peaks.get(i - modelChromatograms.get(modelID + 1).chromScanOfPeakLeft).intensity);
            }
        }

        modelVector.ms1DeconvolutionPattern = getMS1DeconvolutionPattern(isOneLeftModel, isTwoLeftModel, isOneRightModel, isTwoRightModel);
        modelVector.targetScanLeftInModelChromatogramVector = modelChromatograms.get(modelID).chromScanOfPeakLeft - modelChromLeft;
        modelVector.targetScanTopInModelChromatogramVector = modelVector.targetScanLeftInModelChromatogramVector + modelChromatograms.get(modelID).chromScanOfPeakTop - modelChromatograms.get(modelID).chromScanOfPeakLeft;
        modelVector.targetScanRightInModelChromatogramVector = modelVector.targetScanLeftInModelChromatogramVector + modelChromatograms.get(modelID).chromScanOfPeakRight - modelChromatograms.get(modelID).chromScanOfPeakLeft;

        return modelVector;
    }

    /**
     *
     * @param isOneLeftModel
     * @param isTwoLeftModel
     * @param isOneRightModel
     * @param isTwoRightModel
     * @return
     */
    private MS1DeconvolutionPattern getMS1DeconvolutionPattern(boolean isOneLeftModel, boolean isTwoLeftModel, boolean isOneRightModel, boolean isTwoRightModel) {
        if (!isOneLeftModel && !isTwoLeftModel && !isOneRightModel && !isTwoRightModel)
            return MS1DeconvolutionPattern.C;
        else if (isOneLeftModel && !isTwoLeftModel && !isOneRightModel && !isTwoRightModel)
            return MS1DeconvolutionPattern.BC;
        else if (!isOneLeftModel && !isTwoLeftModel && isOneRightModel && !isTwoRightModel)
            return MS1DeconvolutionPattern.CD;
        else if (isOneLeftModel && !isTwoLeftModel && isOneRightModel && !isTwoRightModel)
            return MS1DeconvolutionPattern.BCD;
        else if (isOneLeftModel && isTwoLeftModel && !isOneRightModel && !isTwoRightModel)
            return MS1DeconvolutionPattern.ABC;
        else if (!isOneLeftModel && !isTwoLeftModel && isOneRightModel && isTwoRightModel)
            return MS1DeconvolutionPattern.CDE;
        else if (isOneLeftModel && isTwoLeftModel && isOneRightModel && !isTwoRightModel)
            return MS1DeconvolutionPattern.ABCD;
        else if (isOneLeftModel && !isTwoLeftModel && isOneRightModel && isTwoRightModel)
            return MS1DeconvolutionPattern.BCDE;
        else if (isOneLeftModel && isTwoLeftModel && isOneRightModel && isTwoRightModel)
            return MS1DeconvolutionPattern.ABCDE;
        else
            return MS1DeconvolutionPattern.C;
    }

    /**
     *
     * @param spectrumList
     * @param modelChromVector
     * @param deconvolutionBins
     * @param chromScanOfPeakTop
     * @param properties
     * @return
     */
    private List<List<Peak>> getMS1Chromatograms(List<Feature> spectrumList, ModelChromatogramVector modelChromVector,
                                                 DeconvolutionBin[] deconvolutionBins, int chromScanOfPeakTop,
                                                 MSDialGCMSProcessingProperties properties) {

        int rawScan = deconvolutionBins[chromScanOfPeakTop].rawScanNumber;
        double massBin = properties.accuracyType == AccuracyType.NOMINAL ? properties.massAccuracy : properties.massAccuracy;

        List<List<Peak>> peaksList = new ArrayList<>();

        List<Ion> focusedMS1Spectrum = SpectralCentroiding.getGCMSCentroidedSpectrum(spectrumList, properties.dataType,
                rawScan, massBin,properties.amplitudeCutoff, properties.massRangeBegin, properties.massRangeEnd);

        List<Ion> sortedIons = focusedMS1Spectrum.stream()
                .filter(n -> n.intensity() >= properties.amplitudeCutoff)
                .sorted(Comparator.comparing(Ion::intensity).reversed())
                .collect(Collectors.toList());

        for (Ion ion : sortedIons) {
            List<Peak> peaks = getTrimedAndSmoothedPeaklist(spectrumList, modelChromVector.chromatogramScanList.get(0), modelChromVector.chromatogramScanList.get(modelChromVector.chromatogramScanList.size() - 1), deconvolutionBins, ion.mass(), properties);
            List<Peak> baselineCorrectedPeaks = getBaselineCorrectedPeaklist(peaks, modelChromVector.targetScanTopInModelChromatogramVector);
            peaksList.add(baselineCorrectedPeaks);
        }

        return peaksList;
    }

    private static MS1DeconvolutionResult getRefinedMS1DeconvolutionResult(MS1DeconvolutionResult ms1DecResult, AccuracyType massResolution) {
        if (massResolution == AccuracyType.NOMINAL)
            return ms1DecResult;

        List<Peak> spectrum = new ArrayList<>();
        spectrum.add(ms1DecResult.spectrum.get(0));

        for (int i = 1; i < ms1DecResult.spectrum.size(); i++) {
            if (i > ms1DecResult.spectrum.size() - 1)
                break;

            DecimalFormat df = new DecimalFormat("#.0000");

            if (!Objects.equals(df.format(spectrum.get(spectrum.size() - 1).mz), df.format(ms1DecResult.spectrum.get(i).mz))) {
                spectrum.add(ms1DecResult.spectrum.get(i));
            } else if (spectrum.get(spectrum.size() - 1).intensity < ms1DecResult.spectrum.get(i).intensity) {
                spectrum.remove(spectrum.size() - 1);
                spectrum.add(ms1DecResult.spectrum.get(i));
            }
        }

        if (spectrum.size() > 0) {
            double maxIntensity = spectrum.stream().max(Comparator.comparing(Peak::intensity)).get().intensity;
            ms1DecResult.spectrum = spectrum.stream().filter(n -> n.intensity > maxIntensity * 0.001).collect(Collectors.toList());
        }

        return ms1DecResult;
    }
}
