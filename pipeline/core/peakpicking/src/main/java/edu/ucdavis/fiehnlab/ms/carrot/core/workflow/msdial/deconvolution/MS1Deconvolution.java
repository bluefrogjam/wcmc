package edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.deconvolution;

import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.peakdetection.MsdialGCBasedPeakSpotting;
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.peakdetection.MSDialPreProcessingProperties;
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.types.*;
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.utils.GCMSDataAccessUtility;
import edu.ucdavis.fiehnlab.ms.carrot.core.workflow.msdial.utils.TypeConverter;
import edu.ucdavis.fiehnlab.spectra.hash.core.impl.SplashVersion1;
import edu.ucdavis.fiehnlab.spectra.hash.core.types.SpectraType;
import edu.ucdavis.fiehnlab.spectra.hash.core.types.SpectrumImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Created by diego on 8/5/2016.
 */
@Component
public class MS1Deconvolution {
	private static Logger logger = LoggerFactory.getLogger(MsdialGCBasedPeakSpotting.class);

	@Autowired
	TypeConverter converter = null;

	public List<MS1DecResult> gcmsMS1DecResults(List<RawSpectrum> spectra, List<PeakAreaBean> peakAreaList, MSDialPreProcessingProperties properties) {

		peakAreaList = peakAreaList.stream().sorted(Comparator.comparing(PeakAreaBean::scanNumAtPeakTop).thenComparing(Comparator.comparing(PeakAreaBean::accurateMass))).collect(Collectors.toList());

		Map<Integer, Integer> rawToChromDict = getRdamAndMs1chromatogramScanDictionary(spectra, properties.ionMode);
		GCMSDecBin[] gcmsDecBinArray = getGcmsBinArray(spectra, peakAreaList, rawToChromDict, properties.ionMode);
		double[] matchedFilterArray = getMatchedFileterArray(gcmsDecBinArray, properties.sigma);
		List<ModelChromatogram> modelChromatograms = getModelChromatograms(spectra, peakAreaList, gcmsDecBinArray, matchedFilterArray, rawToChromDict, properties);

		modelChromatograms = getRefinedModelChromatograms(modelChromatograms);
		List<MS1DecResult> ms1DecResults = new ArrayList<>();
		int counter = 0;
		double minIntensity = Double.MAX_VALUE;
		double maxIntensity = Double.MIN_VALUE;

		for (int i = 0; i < modelChromatograms.size(); i++) {
			ModelChromVector modelChromVector = getModelChromatogramVector(i, modelChromatograms, gcmsDecBinArray);
			List<List<MsDialPeak>> ms1Chromatograms = getMs1Chromatograms(spectra, modelChromVector, gcmsDecBinArray, modelChromatograms.get(i).chromScanOfPeakTop, properties);
			MS1DecResult ms1DecResult = MS1Dec.getMs1DecResult(modelChromVector, ms1Chromatograms);

			if (ms1DecResult != null && ms1DecResult.spectrum.size() > 0) {
				ms1DecResult.ms1DecID = counter;
				ms1DecResult = getRefinedMs1DecResult(ms1DecResult, MassResolution.valueOf(properties.massResolution.toUpperCase()));
				ms1DecResult.splash = calculateSplash(ms1DecResult);
				ms1DecResults.add(ms1DecResult);

				if (ms1DecResult.basepeakHeight < minIntensity) minIntensity = ms1DecResult.basepeakHeight;
				if (ms1DecResult.basepeakHeight > maxIntensity) maxIntensity = ms1DecResult.basepeakHeight;

				counter++;
			}
		}

		for (MS1DecResult ms1DecResult : ms1DecResults) {
			ms1DecResult.amplitudeScore = (ms1DecResult.basepeakHeight - minIntensity) / (maxIntensity - minIntensity);

			//calculating purity
			double tic = spectra.stream().filter(sp -> sp.scanNum == ms1DecResult.scanNumber).findFirst().get().tic;

			double eic = ms1DecResult.spectrum.stream().mapToDouble(MsDialPeak::intensity).sum();
			ms1DecResult.modelPeakPurity = eic / tic;
		}
		return ms1DecResults;
	}

	/**
	 * creates a map from raw data scan index to chromatogram index filtering wrong ion modes (opposite polarity) scans
	 *
	 * @param spectra
	 * @param ionMode
	 * @return
	 */
	private static Map<Integer, Integer> getRdamAndMs1chromatogramScanDictionary(List<RawSpectrum> spectra, char ionMode) {
		Map<Integer, Integer> rdamToChromDictionary = new HashMap<>();

		int counter = 0;
		for (RawSpectrum spec : spectra) {
			if (spec.msLevel > 1) continue;
			if (spec.polarity != ionMode) continue;

			if (spectra.get(0).scanNum == 0) {
				rdamToChromDictionary.put(spec.scanNum, counter);
			} else {
				rdamToChromDictionary.put(spec.scanNum - 1, counter);
			}

			counter++;
		}

		return rdamToChromDictionary;
	}

	/**
	 * @param spectra         raw data
	 * @param peakAreaList    detected peaks
	 * @param rawDataScanDict
	 * @param ionMode
	 * @return
	 */
	private static GCMSDecBin[] getGcmsBinArray(List<RawSpectrum> spectra, List<PeakAreaBean> peakAreaList, Map<Integer, Integer> rawDataScanDict, char ionMode) {
		List<RawSpectrum> ms1SpectrumList = getMs1SpectrumList(spectra, ionMode);
		GCMSDecBin[] gcmsDecBins = new GCMSDecBin[ms1SpectrumList.size()];
		int margin = ms1SpectrumList.get(0).scanNum == 0 ? 0 : 1;   // if first scan is 0, fix marging to avoid IndexOutOfBounds exceptions

		for (int i = 0; i < gcmsDecBins.length; i++) {
			gcmsDecBins[i] = new GCMSDecBin();
			gcmsDecBins[i].rawScanNumber = ms1SpectrumList.get(i).scanNum - margin;
			gcmsDecBins[i].retentionTime = ms1SpectrumList.get(i).rtMin;
		}

		for (int i = 0; i < peakAreaList.size(); i++) {
			PeakAreaBean peak = peakAreaList.get(i);
			PeakSpot model = new PeakSpot();
			Integer scanBin = rawDataScanDict.get(peak.ms1LevelDatapointNumber);
			model.peakSpotID = i;

			if (peak.idealSlopeValue > 0.999) model.quality = ModelQuality.High;
			else if (peak.idealSlopeValue > 0.9) model.quality = ModelQuality.Medium;
			else model.quality = ModelQuality.Low;

			if (model.quality == ModelQuality.High || model.quality == ModelQuality.Medium)
				gcmsDecBins[scanBin].totalSharpnessValue += peak.sharpenessValue;

			gcmsDecBins[scanBin].peakSpots.add(model);
		}

		return gcmsDecBins;
	}

	/**
	 * Filters the raw data looking for ms1 level scans that match the desired polarity
	 *
	 * @param spectra list of raw scans
	 * @param ionMode desired scan polarity
	 * @return a list of filtered raw ms1 scans
	 */
	static List<RawSpectrum> getMs1SpectrumList(List<RawSpectrum> spectra, char ionMode) {
		return spectra.stream()
				.filter(s -> s.msLevel == 1)
				.filter(s -> s.polarity == ionMode)
				.collect(Collectors.toList());
	}

	/**
	 * get an array of 'normal distribution' coeficients
	 *
	 * @param gcmsDecBinArray
	 * @param sigma           peak width?
	 * @return
	 */
	private static double[] getMatchedFileterArray(GCMSDecBin[] gcmsDecBinArray, double sigma) {
		double halfPoint = 10.0; // currently this value should be enough for GC
		double[] matchedFilterArray = new double[gcmsDecBinArray.length];
		double[] matchedFilterCoefficient = new double[2 * (int) halfPoint + 1];

		for (int i = 0; i < matchedFilterCoefficient.length; i++) {
			matchedFilterCoefficient[i] = (1 - Math.pow((-halfPoint + i) / sigma, 2)) * Math.exp(-0.5 * Math.pow((-halfPoint + i) / sigma, 2));
		}

		for (int i = 0; i < gcmsDecBinArray.length; i++) {
			double sum = 0.0;
			for (int j = -1 * (int) halfPoint; j <= (int) halfPoint; j++) {
				if (i + j < 0) sum += 0;
				else if (i + j > gcmsDecBinArray.length - 1) sum += 0;
				else
					sum += gcmsDecBinArray[i + j].totalSharpnessValue * matchedFilterCoefficient[(int) (j + halfPoint)];
			}
			matchedFilterArray[i] = sum;
		}

		return matchedFilterArray;
	}

	/**
	 * get model chromatograms by applying coeficients
	 *
	 * @param spectra         list of spectra from unprocess data
	 * @param detectedPeaks   list of detected peaks
	 * @param gcmsDecBins
	 * @param rdamToChromDict
	 * @return
	 */
	private static List<ModelChromatogram> getModelChromatograms(List<RawSpectrum> spectra, List<PeakAreaBean> detectedPeaks, GCMSDecBin[] gcmsDecBins, double[] matchedFilterArray, Map<Integer, Integer> rdamToChromDict,
	                                                             MSDialPreProcessingProperties properties) {
		List<RegionMarker> regionMarkers = getRegionMarkers(matchedFilterArray);
		List<ModelChromatogram> modelChromatograms = new ArrayList<>();

		for (RegionMarker region : regionMarkers) {
			List<PeakAreaBean> peakAreas = new ArrayList<>();
			for (int i = region.scanBegin; i <= region.scanEnd; i++) {
				for (PeakSpot peakSpot : gcmsDecBins[i].peakSpots.stream().filter(n -> n.quality == ModelQuality.High).collect(Collectors.toList())) {
					peakAreas.add(detectedPeaks.get(peakSpot.peakSpotID));
				}
			}

			if (peakAreas.size() == 0) {
				for (int i = region.scanBegin; i <= region.scanEnd; i++) {
					for (PeakSpot peakSpot : gcmsDecBins[i].peakSpots.stream().filter(n -> n.quality == ModelQuality.Medium).collect(Collectors.toList())) {
						peakAreas.add(detectedPeaks.get(peakSpot.peakSpotID));
					}
				}
			}

			ModelChromatogram modelChrom = getModelChromatogram(spectra, peakAreas, gcmsDecBins, rdamToChromDict, properties);
			if (modelChrom != null) {
				modelChromatograms.add(modelChrom);
			}
		}

		return modelChromatograms;
	}

	/**
	 * @param matchedFilterArray
	 * @return
	 */
	private static List<RegionMarker> getRegionMarkers(double[] matchedFilterArray) {
		List<RegionMarker> regionMarkers = new ArrayList<>();
		int scanBegin = 0;
		boolean scanBeginFlg = false;
		int margin = 5;

		for (int i = margin; i < matchedFilterArray.length - margin; i++) {
			if (matchedFilterArray[i] > 0 && matchedFilterArray[i - 1] < matchedFilterArray[i] && !scanBeginFlg) {
				scanBegin = i;
				scanBeginFlg = true;
			} else if (scanBeginFlg) {
				if (matchedFilterArray[i] <= 0) {
					regionMarkers.add(new RegionMarker(regionMarkers.size(), scanBegin, i - 1));
					scanBeginFlg = false;
				} else if (matchedFilterArray[i - 1] > matchedFilterArray[i] && matchedFilterArray[i] < matchedFilterArray[i + 1] && matchedFilterArray[i] >= 0) {
					regionMarkers.add(new RegionMarker(regionMarkers.size(), scanBegin, i));
					scanBegin = i + 1;
					scanBeginFlg = true;
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
	 * @param gcmsDecBins
	 * @param rdamToChromDict
	 * @return
	 */
	private static ModelChromatogram getModelChromatogram(List<RawSpectrum> spectra, List<PeakAreaBean> detectedPeaks, GCMSDecBin[] gcmsDecBins, Map<Integer, Integer> rdamToChromDict,
	                                                      MSDialPreProcessingProperties properties) {
		if (detectedPeaks == null || detectedPeaks.size() == 0) return null;

		double maxSharpnessValue = detectedPeaks.stream().max(Comparator.comparing(PeakAreaBean::sharpenessValue)).get().sharpenessValue;
		double maxIdealSlopeValue = detectedPeaks.stream().max(Comparator.comparing(PeakAreaBean::idealSlopeValue)).get().idealSlopeValue;
		ModelChromatogram modelChrom = new ModelChromatogram();
		modelChrom.sharpnessValue = maxSharpnessValue;
		modelChrom.idealSlopeValue = maxIdealSlopeValue;

		boolean firstFlg = false;

		List<MsDialPeak> peaklist = new ArrayList<>();
		List<List<MsDialPeak>> peaklists = new ArrayList<>();
		List<MsDialPeak> baselineCorrectedPeaklist;

		for (PeakAreaBean peak : detectedPeaks.stream().filter(n -> n.sharpenessValue >= maxSharpnessValue * 0.9).sorted(Comparator.comparing(PeakAreaBean::sharpenessValue).reversed()).collect(Collectors.toList())) {
			if (!firstFlg) {
				modelChrom.rawScanOfPeakTop = peak.ms1LevelDatapointNumber;
				modelChrom.chromScanOfPeakTop = rdamToChromDict.get(modelChrom.rawScanOfPeakTop);
				modelChrom.chromScanOfPeakLeft = modelChrom.chromScanOfPeakTop - (peak.scanNumberAtPeakTop - peak.scanNumberAtLeftPeakEdge);
				modelChrom.chromScanOfPeakRight = modelChrom.chromScanOfPeakTop + (peak.scanNumberAtRightPeakEdge - peak.scanNumberAtPeakTop);
				modelChrom.modelMasses.add(peak.accurateMass);
				modelChrom.sharpnessValue = peak.sharpenessValue;

				peaklist = getTrimedAndSmoothedPeaklist(spectra, modelChrom.chromScanOfPeakLeft, modelChrom.chromScanOfPeakRight, gcmsDecBins, peak.accurateMass(), properties);
				baselineCorrectedPeaklist = getBaselineCorrectedPeaklist(peaklist, modelChrom.chromScanOfPeakTop - modelChrom.chromScanOfPeakLeft);
				peaklists.add(baselineCorrectedPeaklist);
				firstFlg = true;
			} else {
				modelChrom.modelMasses.add(peak.accurateMass);
				peaklist = getTrimedAndSmoothedPeaklist(spectra, modelChrom.chromScanOfPeakLeft, modelChrom.chromScanOfPeakRight, gcmsDecBins, peak.accurateMass(), properties);
				baselineCorrectedPeaklist = getBaselineCorrectedPeaklist(peaklist, modelChrom.chromScanOfPeakTop - modelChrom.chromScanOfPeakLeft);
				peaklists.add(baselineCorrectedPeaklist);
			}
		}

		double mzCount = modelChrom.modelMasses.size();
		for (MsDialPeak peak : peaklists.get(0)) {
			modelChrom.peaks.add(new MsDialPeak(peak.scanNum, peak.rtMin, peak.mass, peak.intensity / mzCount));
		}

		if (peaklist.size() > 1) {
			for (int i = 1; i < peaklists.size(); i++) {
				for (int j = 0; j < peaklists.get(i).size(); j++) {
					modelChrom.peaks.get(j).intensity += peaklists.get(i).get(j).intensity /= mzCount;
				}
			}
		}

		modelChrom = getRefinedModelChromatogram(modelChrom, gcmsDecBins, properties.averagePeakWidth);

		return modelChrom;
	}

	/**
	 * @param spectra
	 * @param chromScanOfPeakLeft
	 * @param chromScanOfPeakRight
	 * @param gcmsDecBins
	 * @param focusedMass
	 * @return
	 */
	private static List<MsDialPeak> getTrimedAndSmoothedPeaklist(List<RawSpectrum> spectra, int chromScanOfPeakLeft, int chromScanOfPeakRight, GCMSDecBin[] gcmsDecBins, double focusedMass,
	                                                       MSDialPreProcessingProperties properties) {
		List<MsDialPeak> peaklist = new ArrayList<>();

		int startIndex = 0, leftRemainder = 0, rightRemainder = 0;
		double sum = 0, maxIntensityMz, maxMass;
		double massTol = properties.massAccuracy;

		int chromLeft = chromScanOfPeakLeft - properties.smoothingLevel;
		int chromRight = chromScanOfPeakRight + properties.smoothingLevel;

		if (chromLeft < 0) {
			leftRemainder = properties.smoothingLevel - chromScanOfPeakLeft;
			chromLeft = 0;
		}
		if (chromRight > gcmsDecBins.length - 1) {
			rightRemainder = chromScanOfPeakRight + properties.smoothingLevel - (gcmsDecBins.length - 1);
			chromRight = gcmsDecBins.length - 1;
		}

		for (int i = chromLeft; i <= chromRight; i++) {
			int rawScan = gcmsDecBins[i].rawScanNumber;
			RawSpectrum spectrum = spectra.get(rawScan);
			List<Ion> massSpectra = spectrum.spect;

			sum = 0;
			maxIntensityMz = Double.MIN_VALUE;
			maxMass = focusedMass;

			startIndex = GCMSDataAccessUtility.getMs1StartIndex(focusedMass, massTol, massSpectra);
			for (int j = startIndex; j < massSpectra.size(); j++) {
				if (massSpectra.get(j).mass < focusedMass - massTol) {
					continue;
				} else if (focusedMass - massTol <= massSpectra.get(j).mass && massSpectra.get(j).mass < focusedMass + massTol) {
					sum += massSpectra.get(j).intensity();
					if (maxIntensityMz < massSpectra.get(j).intensity) {
						maxIntensityMz = massSpectra.get(j).intensity;
						maxMass = massSpectra.get(j).mass;
					}
				} else if (massSpectra.get(j).mass >= focusedMass + massTol) {
					break;
				}
			}

			if (spectra.get(0).scanNum == 0)
				peaklist.add(new MsDialPeak(spectrum.scanNum, spectrum.rtMin, maxMass, sum));
			else
				peaklist.add(new MsDialPeak(spectrum.scanNum - 1, spectrum.rtMin, maxMass, sum));
		}

		List<MsDialPeak> smoothedPeaklist = new ArrayList<>(GCMSDataAccessUtility.getSmoothedPeaklist(peaklist, properties));
		for (int i = 0; i < properties.smoothingLevel - leftRemainder; i++)
			smoothedPeaklist.remove(0);
		for (int i = 0; i < properties.smoothingLevel - rightRemainder; i++)
			smoothedPeaklist.remove(smoothedPeaklist.size() - 1);

		return smoothedPeaklist;
	}

	/**
	 * performs baseline correction on the input peakList
	 *
	 * @param peaklist
	 * @param peakTop
	 * @return
	 */
	private static List<MsDialPeak> getBaselineCorrectedPeaklist(List<MsDialPeak> peaklist, int peakTop) {
		List<MsDialPeak> baselineCorrectedPeaklist = new ArrayList<>();

		//find local minimum of left and right edge
		int minimumLeftID = 0;
		double minimumValue = Double.MAX_VALUE;

		for (int i = peakTop; i >= 0; i--)
			if (peaklist.get(i).intensity < minimumValue) {
				minimumValue = peaklist.get(i).intensity;
				minimumLeftID = i;
			}

		int minimumRightID = peaklist.size() - 1;
		minimumValue = Double.MAX_VALUE;
		for (int i = peakTop; i < peaklist.size(); i++)
			if (peaklist.get(i).intensity < minimumValue) {
				minimumValue = peaklist.get(i).intensity;
				minimumRightID = i;
			}

		double coeff = (peaklist.get(minimumRightID).intensity - peaklist.get(minimumLeftID).intensity) / (peaklist.get(minimumRightID).rtMin - peaklist.get(minimumLeftID).rtMin);
		double intercept = (peaklist.get(minimumRightID).rtMin * peaklist.get(minimumLeftID).intensity - peaklist.get(minimumLeftID).rtMin * peaklist.get(minimumRightID).intensity) / (peaklist.get(minimumRightID).rtMin - peaklist.get(minimumLeftID).rtMin);
		double correctedIntensity = 0;
		for (MsDialPeak peak : peaklist) {
			correctedIntensity = peak.intensity - (int) (coeff * peak.rtMin + intercept);
			if (correctedIntensity >= 0)
				baselineCorrectedPeaklist.add(new MsDialPeak(peak.scanNum, peak.rtMin, peak.mass, correctedIntensity));
			else
				baselineCorrectedPeaklist.add(new MsDialPeak(peak.scanNum, peak.rtMin, peak.mass, 0));
		}

		return baselineCorrectedPeaklist;
	}

	/**
	 * @param modelChrom
	 * @param gcmsDecBins
	 * @return
	 */
	private static ModelChromatogram getRefinedModelChromatogram(ModelChromatogram modelChrom, GCMSDecBin[] gcmsDecBins, int avgPeakWidth) {
		double maxIntensity = Double.MIN_VALUE;
		int peakTopID = -1, peakLeftID = -1, peakRightID = -1;

		for (int i = 0; i < modelChrom.peaks.size(); i++) {
			if (modelChrom.peaks.get(i).intensity > maxIntensity) {
				maxIntensity = modelChrom.peaks.get(i).intensity;
				peakTopID = i;
			}
		}

		modelChrom.maximumPeakTopValue = maxIntensity;
		//left spike check
		for (int i = peakTopID; i > 0; i--) {
			if (peakTopID - i < avgPeakWidth * 0.5)
				continue;

			if (modelChrom.peaks.get(i - 1).intensity >= modelChrom.peaks.get(i).intensity) {
				peakLeftID = i;
				break;
			}
		}

		if (peakLeftID < 0)
			peakLeftID = 0;

		//right spike check
		for (int i = peakTopID; i < modelChrom.peaks.size() - 1; i++) {
			if (i - peakTopID < avgPeakWidth * 0.5)
				continue;
			if (modelChrom.peaks.get(i).intensity <= modelChrom.peaks.get(i + 1).intensity) {
				peakRightID = i;
				break;
			}
		}

		if (peakRightID < 0)
			peakRightID = modelChrom.peaks.size() - 1;

		modelChrom.chromScanOfPeakTop = peakTopID + modelChrom.chromScanOfPeakLeft;
		modelChrom.chromScanOfPeakRight = peakRightID + modelChrom.chromScanOfPeakLeft;
		modelChrom.chromScanOfPeakLeft = peakLeftID + modelChrom.chromScanOfPeakLeft;
		modelChrom.rawScanOfPeakTop = gcmsDecBins[modelChrom.chromScanOfPeakTop].rawScanNumber;

		List<MsDialPeak> peaks = new ArrayList<>();
		for (int i = peakLeftID; i <= peakRightID; i++)
			peaks.add(modelChrom.peaks.get(i));

		//final curation
		if (peakTopID - peakLeftID < 3) return null;
		if (peakRightID - peakTopID < 3) return null;

		modelChrom.peaks = peaks;

		return modelChrom;
	}


	/**
	 * @param modelChromatograms
	 * @return
	 */
	private static List<ModelChromatogram> getRefinedModelChromatograms(List<ModelChromatogram> modelChromatograms) {
		modelChromatograms.stream().sorted(Comparator.comparing(ModelChromatogram::chromScanOfPeakTop));

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
	 * @param modelID
	 * @param modelChromatograms
	 * @param gcmsDecBins
	 * @return
	 */
	private static ModelChromVector getModelChromatogramVector(int modelID, List<ModelChromatogram> modelChromatograms, GCMSDecBin[] gcmsDecBins) {
		int modelChromLeft = modelChromatograms.get(modelID).chromScanOfPeakLeft;
		int modelChromRight = modelChromatograms.get(modelID).chromScanOfPeakRight;
		boolean isTwoLeftModel = false;
		boolean isOneLeftModel = false;
		boolean isTwoRightModel = false;
		boolean isOneRightModel = false;

		if (modelID > 1 && modelChromatograms.get(modelID - 2).chromScanOfPeakRight > modelChromLeft) {
			isTwoLeftModel = true;
		}
		if (modelID > 0 && modelChromatograms.get(modelID - 1).chromScanOfPeakRight > modelChromLeft) {
			isOneLeftModel = true;
		}
		if (modelID < modelChromatograms.size() - 2 && modelChromatograms.get(modelID + 2).chromScanOfPeakLeft < modelChromRight) {
			isTwoRightModel = true;
		}
		if (modelID < modelChromatograms.size() - 1 && modelChromatograms.get(modelID + 1).chromScanOfPeakLeft < modelChromRight) {
			isOneRightModel = true;
		}

		if (isTwoLeftModel)
			modelChromLeft = Math.min(modelChromatograms.get(modelID).chromScanOfPeakLeft, Math.min(modelChromatograms.get(modelID - 2).chromScanOfPeakLeft, modelChromatograms.get(modelID - 1).chromScanOfPeakLeft));
		else if (isOneLeftModel)
			modelChromLeft = Math.min(modelChromatograms.get(modelID).chromScanOfPeakLeft, modelChromatograms.get(modelID - 1).chromScanOfPeakLeft);

		if (isTwoRightModel)
			modelChromRight = Math.max(modelChromatograms.get(modelID).chromScanOfPeakRight, Math.max(modelChromatograms.get(modelID + 2).chromScanOfPeakRight, modelChromatograms.get(modelID + 1).chromScanOfPeakRight));
		else if (isOneRightModel)
			modelChromRight = Math.max(modelChromatograms.get(modelID).chromScanOfPeakRight, modelChromatograms.get(modelID + 1).chromScanOfPeakRight);

		ModelChromVector modelVector = new ModelChromVector();
		modelVector.modelMasses = modelChromatograms.get(modelID).modelMasses;

		for (int i = modelChromLeft; i <= modelChromRight; i++) {
			modelVector.chromScanList.add(i);
			modelVector.rawScanList.add(gcmsDecBins[i].rawScanNumber);
			modelVector.rtArray.add(gcmsDecBins[i].retentionTime);

			if (modelChromatograms.get(modelID).chromScanOfPeakLeft > i || modelChromatograms.get(modelID).chromScanOfPeakRight < i)
				modelVector.targetIntensityArray.add(0.0);
			else
				modelVector.targetIntensityArray.add(modelChromatograms.get(modelID).peaks.get(i - modelChromatograms.get(modelID).chromScanOfPeakLeft).intensity);

			if (isTwoLeftModel) {
				if (modelChromatograms.get(modelID - 2).chromScanOfPeakLeft > i || modelChromatograms.get(modelID - 2).chromScanOfPeakRight < i)
					modelVector.twoLeftIntensityArray.add(0.0);
				else
					modelVector.twoLeftIntensityArray.add(modelChromatograms.get(modelID - 2).peaks.get(i - modelChromatograms.get(modelID - 2).chromScanOfPeakLeft).intensity);
			}

			if (isOneLeftModel || isTwoLeftModel) {
				if (modelChromatograms.get(modelID - 1).chromScanOfPeakLeft > i || modelChromatograms.get(modelID - 1).chromScanOfPeakRight < i)
					modelVector.oneLeftIntensityArray.add(0.0);
				else
					modelVector.oneLeftIntensityArray.add(modelChromatograms.get(modelID - 1).peaks.get(i - modelChromatograms.get(modelID - 1).chromScanOfPeakLeft).intensity);
			}

			if (isTwoRightModel) {
				if (modelChromatograms.get(modelID + 2).chromScanOfPeakLeft > i || modelChromatograms.get(modelID + 2).chromScanOfPeakRight < i)
					modelVector.twoRightInetnsityArray.add(0.0);
				else
					modelVector.twoRightInetnsityArray.add(modelChromatograms.get(modelID + 2).peaks.get(i - modelChromatograms.get(modelID + 2).chromScanOfPeakLeft).intensity);
			}

			if (isOneRightModel || isTwoRightModel) {
				if (modelChromatograms.get(modelID + 1).chromScanOfPeakLeft > i || modelChromatograms.get(modelID + 1).chromScanOfPeakRight < i)
					modelVector.oneRightIntensityArray.add(0.0);
				else
					modelVector.oneRightIntensityArray.add(modelChromatograms.get(modelID + 1).peaks.get(i - modelChromatograms.get(modelID + 1).chromScanOfPeakLeft).intensity);
			}
		}
		modelVector.ms1DecPattern = getMs1DecPattern(isOneLeftModel, isTwoLeftModel, isOneRightModel, isTwoRightModel);
		modelVector.targetScanLeftInModelChromVector = modelChromatograms.get(modelID).chromScanOfPeakLeft - modelChromLeft;
		modelVector.targetScanTopInModelChromVector = modelVector.targetScanLeftInModelChromVector + modelChromatograms.get(modelID).chromScanOfPeakTop - modelChromatograms.get(modelID).chromScanOfPeakLeft;
		modelVector.targetScanRightInModelChromVector = modelVector.targetScanLeftInModelChromVector + modelChromatograms.get(modelID).chromScanOfPeakRight - modelChromatograms.get(modelID).chromScanOfPeakLeft;

		return modelVector;
	}

	/**
	 * @param isOneLeftModel
	 * @param isTwoLeftModel
	 * @param isOneRightModel
	 * @param isTwoRightModel
	 * @return
	 */
	private static Ms1DecPattern getMs1DecPattern(boolean isOneLeftModel, boolean isTwoLeftModel, boolean isOneRightModel, boolean isTwoRightModel) {
		if (!isOneLeftModel && !isTwoLeftModel && !isOneRightModel && !isTwoRightModel) return Ms1DecPattern.C;
		if (isOneLeftModel && !isTwoLeftModel && !isOneRightModel && !isTwoRightModel) return Ms1DecPattern.BC;
		if (!isOneLeftModel && !isTwoLeftModel && isOneRightModel && !isTwoRightModel) return Ms1DecPattern.CD;
		if (isOneLeftModel && !isTwoLeftModel && isOneRightModel && !isTwoRightModel) return Ms1DecPattern.BCD;
		if (isOneLeftModel && isTwoLeftModel && !isOneRightModel && !isTwoRightModel) return Ms1DecPattern.ABC;
		if (!isOneLeftModel && !isTwoLeftModel && isOneRightModel && isTwoRightModel) return Ms1DecPattern.CDE;
		if (isOneLeftModel && isTwoLeftModel && isOneRightModel && !isTwoRightModel) return Ms1DecPattern.ABCD;
		if (isOneLeftModel && !isTwoLeftModel && isOneRightModel && isTwoRightModel) return Ms1DecPattern.BCDE;
		if (isOneLeftModel && isTwoLeftModel && isOneRightModel && isTwoRightModel) return Ms1DecPattern.ABCDE;
		return Ms1DecPattern.C;
	}

	/**
	 * @param spectra
	 * @param modelChromVector
	 * @param gcmsDecBins
	 * @param chromScanOfPeakTop
	 * @return
	 */
	private static List<List<MsDialPeak>> getMs1Chromatograms(List<RawSpectrum> spectra, ModelChromVector modelChromVector, GCMSDecBin[] gcmsDecBins, int chromScanOfPeakTop,
	                                                    MSDialPreProcessingProperties properties) {
		int rawScan = gcmsDecBins[chromScanOfPeakTop].rawScanNumber;
		double massBin = properties.massAccuracy;
		if (MassResolution.valueOf(properties.massResolution.toUpperCase()) == MassResolution.NOMINAL) massBin = 0.5;

		List<Ion> focusedMs1Spectrum = GCMSDataAccessUtility.getCentroidMassSpectra(spectra, rawScan, massBin, properties);
		List<List<MsDialPeak>> peaksList = new ArrayList<>();

		for (Ion spec : focusedMs1Spectrum.stream()
				.filter(n -> n.intensity() >= properties.amplitudeCutoff)
				.sorted(Comparator.comparing(Ion::intensity).reversed())
				.collect(Collectors.toList())) {
			List<MsDialPeak> peaks = getTrimedAndSmoothedPeaklist(spectra, modelChromVector.chromScanList.get(0), modelChromVector.chromScanList.get(modelChromVector.chromScanList.size() - 1), gcmsDecBins, spec.mass, properties);
			List<MsDialPeak> baselineCorrectedPeaks = getBaselineCorrectedPeaklist(peaks, modelChromVector.targetScanTopInModelChromVector);

			peaksList.add(baselineCorrectedPeaks);
		}

		return peaksList;
	}

	/**
	 * @param ms1DecResult
	 * @return
	 */
	private static MS1DecResult getRefinedMs1DecResult(MS1DecResult ms1DecResult, MassResolution massResolution) {
		if (massResolution == MassResolution.NOMINAL) return ms1DecResult;

		List<MsDialPeak> spectrum = new ArrayList<>();
		spectrum.add(ms1DecResult.spectrum.get(0));
		for (int i = 1; i < ms1DecResult.spectrum.size(); i++) {
			if (i > ms1DecResult.spectrum.size() - 1) break;
			DecimalFormat df = new DecimalFormat("#.0000");
			if (!Objects.equals(df.format(spectrum.get(spectrum.size() - 1).mass), df.format(ms1DecResult.spectrum.get(i).mass))) {

				spectrum.add(ms1DecResult.spectrum.get(i));
			} else {
				if (spectrum.get(spectrum.size() - 1).intensity < ms1DecResult.spectrum.get(i).intensity) {
					spectrum.remove(spectrum.size() - 1);
					spectrum.add(ms1DecResult.spectrum.get(i));
				}
			}
		}

		if (spectrum.size() > 0) {
			double maxIntensity = spectrum.stream().max(Comparator.comparing(MsDialPeak::intensity)).get().intensity;
			ms1DecResult.spectrum = spectrum.stream().filter(n -> n.intensity > maxIntensity * 0.001).collect(Collectors.toList());
		}

		return ms1DecResult;
	}

	/**
	 * @param peak
	 * @return
	 */
	private static String calculateSplash(MS1DecResult peak) {
		List<edu.ucdavis.fiehnlab.spectra.hash.core.types.Ion> ions = new ArrayList<>();
		peak.spectrum.forEach(it -> ions.add(new edu.ucdavis.fiehnlab.spectra.hash.core.types.Ion(it.mass, it.intensity)));
		return new SplashVersion1().splashIt(new SpectrumImpl(ions, SpectraType.MS));
	}
}
