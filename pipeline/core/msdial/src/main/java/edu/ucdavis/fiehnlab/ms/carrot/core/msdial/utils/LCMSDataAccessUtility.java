package edu.ucdavis.fiehnlab.ms.carrot.core.msdial.utils;

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Ion;
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.IonMode;
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.Feature;
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.MSMSSpectra;
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.MSSpectra;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.utils.TypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class LCMSDataAccessUtility {

    private static Logger logger = LoggerFactory.getLogger(LCMSDataAccessUtility.class);


    /**
     * @param targetRt
     * @param spectrumList
     * @return
     */
    private static int getRtStartIndex(double targetRt, List<? extends Feature> spectrumList) {

        int startIndex = 0, endIndex = spectrumList.size() - 1;
        int counter = 0;

        while (counter < 10) {
            if (spectrumList.get(startIndex).retentionTimeInMinutes() <= targetRt && targetRt < spectrumList.get((startIndex + endIndex) / 2).retentionTimeInMinutes()) {
                endIndex = (startIndex + endIndex) / 2;
            } else if (spectrumList.get((startIndex + endIndex) / 2).retentionTimeInMinutes() <= targetRt && targetRt < spectrumList.get(endIndex).retentionTimeInMinutes()) {
                startIndex = (startIndex + endIndex) / 2;
            }

            counter++;
        }

        return startIndex;
    }


    /**
     * @param spectrumList
     * @param precursorMz
     * @param productMz
     * @param startRt
     * @param endRt
     * @param ionMode
     * @param centroidedMS1Tolerance
     * @param centroidMS2Tolerance
     * @return
     */
    public static List<double[]> getMS2Peaklist(List<? extends Feature> spectrumList, double precursorMz, double productMz, double startRt,
                                                double endRt, IonMode ionMode, double centroidedMS1Tolerance, double centroidMS2Tolerance) {

        List<double[]> peakList = new ArrayList<>();

        int startRtIndex = getRtStartIndex(startRt, spectrumList);

        for (int i = startRtIndex; i < spectrumList.size(); i++) {
            if (spectrumList.get(i).retentionTimeInMinutes() < startRt)
                continue;
            if (spectrumList.get(i).retentionTimeInMinutes() > endRt)
                break;

            if (spectrumList.get(i) instanceof MSMSSpectra && spectrumList.get(i).ionMode().get().mode().equals(ionMode.mode())) {
                MSMSSpectra spectrum = (MSMSSpectra) spectrumList.get(i);
                List<Ion> massSpectrum = TypeConverter.getJavaIonList(spectrum);

                double sum = 0;

                if (Math.abs(precursorMz - spectrum.precursorIon().get().mass()) < centroidedMS1Tolerance && !massSpectrum.isEmpty()) {
                    int startMsIndex = DataAccessUtility.getMs2StartIndex(productMz - centroidMS2Tolerance, massSpectrum);

                    for (int j = startMsIndex; j < massSpectrum.size(); j++) {
                        if (massSpectrum.get(j).mass() < productMz - centroidMS2Tolerance) {
                            continue;
                        } else if (Math.abs(productMz - massSpectrum.get(j).mass()) <= centroidMS2Tolerance) {
                            sum += massSpectrum.get(j).intensity();
                        } else {
                            break;
                        }
                    }
                }

                peakList.add(new double[]{i, spectrum.retentionTimeInMinutes(), 0, sum});
            }
        }

        return peakList;
    }

    /**
     * @param startPoint
     * @param endPoint
     * @param accurateMass
     * @param tolerance
     * @param spectrumList
     * @param ionMode
     * @return
     */
    public static int getMS2DatapointNumber(int startPoint, int endPoint, float accurateMass, double tolerance, List<? extends Feature> spectrumList, IonMode ionMode) {
        double maxIntensity = Double.MIN_VALUE;
        int maxID = -1;

        if (startPoint < 0)
            startPoint = 0;

        for (int i = startPoint; i < endPoint; i++) {
            if (spectrumList.get(i) instanceof MSMSSpectra && spectrumList.get(i).ionMode().get().mode().equals(ionMode.mode())) {
                MSMSSpectra spectrum = (MSMSSpectra) spectrumList.get(i);

                if (Math.abs(accurateMass - spectrum.precursorIon().get().mass()) <= tolerance) {
                    if (maxIntensity < spectrum.associatedScan().get().basePeak().intensity()) {
                        maxIntensity = spectrum.associatedScan().get().basePeak().intensity();
                        maxID = i;
                    }
                }
            }
        }

        return maxID;
    }
}
