package edu.ucdavis.fiehnlab.ms.carrot.core.msdial.peakpicking;

import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.PeakAreaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public abstract class PeakSpotting {

    private static Logger logger = LoggerFactory.getLogger(PeakSpotting.class);



    public void printDoublePeakList(List<List<PeakAreaBean>> detectedPeaksList) {
        int idx = 0;

        for (List<PeakAreaBean> p : detectedPeaksList) {
            printPeakList(p, idx++);
        }
    }

    private void printPeakList(List<PeakAreaBean> p, int idx) {
        for (PeakAreaBean pp : p) {
            logger.debug(String.format("%d, %d, %.5f, %.5f, %.5f, %.5f, %.5f", idx++, pp.scanNumberAtPeakTop, pp.rtAtPeakTop, pp.accurateMass, pp.intensityAtPeakTop, pp.shapenessValue, pp.symmetryValue));
        }
    }


    /**
     * @param peakList
     * @param detectedPeakAreas
     * @return
     */
    public List<PeakAreaBean> filterPeaksByRawChromatogram(List<double[]> peakList, List<PeakAreaBean> detectedPeakAreas) {
        List<PeakAreaBean> newPeakAreas = new ArrayList<>();

        logger.trace("Peak list size: " + peakList.size() + ", Detected peaks size: " + detectedPeakAreas.size());

        for (PeakAreaBean peak : detectedPeakAreas) {
            int scanNum = peak.scanNumberAtPeakTop;

            if (scanNum - 1 < 0 || scanNum + 1 > peakList.size() - 1)
                continue;

            if (peakList.get(scanNum - 1)[3] <= 0 || peakList.get(scanNum + 1)[3] <= 0)
                continue;

            logger.trace("Retaining scan #" + peak.scanNumberAtPeakTop);
            newPeakAreas.add(peak);
        }

        return newPeakAreas;
    }

    /**
     * @param detectedPeakAreas
     * @param peakList
     * @param backgroundSubtraction
     * @return
     */
    public List<PeakAreaBean> getBackgroundSubtractPeaks(List<PeakAreaBean> detectedPeakAreas, List<double[]> peakList, boolean backgroundSubtraction) {
        if (!backgroundSubtraction)
            return detectedPeakAreas;

        int counterThreshold = 4;
        List<PeakAreaBean> sPeakAreaList = new ArrayList<>();

        for (PeakAreaBean peakArea : detectedPeakAreas) {
            int peakTop = peakArea.scanNumberAtPeakTop;
            int peakLeft = peakArea.scanNumberAtLeftPeakEdge;
            int peakRight = peakArea.scanNumberAtRightPeakEdge;

            int trackingNumber = 10 * (peakRight - peakLeft);
            if (trackingNumber > 50)
                trackingNumber = 50;

            double ampDiff = Math.max(peakArea.intensityAtPeakTop - peakArea.intensityAtLeftPeakEdge, peakArea.intensityAtPeakTop - peakArea.intensityAtRightPeakEdge);
            int counter = 0;

            double spikeMax = -1, spikeMin = -1;

            for (int i = peakLeft - trackingNumber; i <= peakLeft; i++) {
                if (i - 1 < 0)
                    continue;

                if (peakList.get(i - 1)[3] < peakList.get(i)[3] && peakList.get(i)[3] > peakList.get(i + 1)[3]) {
                    spikeMax = peakList.get(i)[3];
                } else if (peakList.get(i - 1)[3] > peakList.get(i)[3] && peakList.get(i)[3] < peakList.get(i + 1)[3]) {
                    spikeMin = peakList.get(i)[3];
                }

                if (spikeMax != -1 && spikeMin != -1) {
                    double noise = 0.5 * Math.abs(spikeMax - spikeMin);
                    if (noise * 3 > ampDiff)
                        counter++;

                    spikeMax = -1;
                    spikeMin = -1;
                }
            }

            for (int i = peakRight; i <= peakRight + trackingNumber; i++) {
                if (i + 1 > peakList.size() - 1)
                    break;

                if (peakList.get(i - 1)[3] < peakList.get(i)[3] && peakList.get(i)[3] > peakList.get(i + 1)[3]) {
                    spikeMax = peakList.get(i)[3];
                } else if (peakList.get(i - 1)[3] > peakList.get(i)[3] && peakList.get(i)[3] < peakList.get(i + 1)[3]) {
                    spikeMin = peakList.get(i)[3];
                }

                if (spikeMax != -1 && spikeMin != -1) {
                    double noise = 0.5 * Math.abs(spikeMax - spikeMin);
                    if (noise * 3 > ampDiff) counter++;
                    spikeMax = -1;
                    spikeMin = -1;
                }
            }

            if (counter < counterThreshold)
                sPeakAreaList.add(peakArea);
        }
        return sPeakAreaList;
    }

    /**
     * @param detectedPeakAreasList
     * @param detectedPeakAreas
     * @param massStep
     * @return
     */
    public List<PeakAreaBean> removePeakAreaBeanRedundancy(List<List<PeakAreaBean>> detectedPeakAreasList, List<PeakAreaBean> detectedPeakAreas, double massStep, double minTolerance) {
        if (detectedPeakAreasList.isEmpty())
            return detectedPeakAreas;

        List<PeakAreaBean> parentPeakAreaBeanList = detectedPeakAreasList.get(detectedPeakAreasList.size() - 1);

        for (int i = 0; i < detectedPeakAreas.size(); i++) {
            for (int j = 0; j < parentPeakAreaBeanList.size(); j++) {
                if (Math.abs(parentPeakAreaBeanList.get(j).accurateMass - detectedPeakAreas.get(i).accurateMass) <= massStep * 0.5) {
                    boolean isOverlapped = isOverlapedChecker(parentPeakAreaBeanList.get(j), detectedPeakAreas.get(i));

                    if (!isOverlapped)
                        continue;

                    double hwhm = ((parentPeakAreaBeanList.get(j).rtAtRightPeakEdge - parentPeakAreaBeanList.get(j).rtAtLeftPeakEdge) +
                            (detectedPeakAreas.get(i).rtAtRightPeakEdge - detectedPeakAreas.get(i).rtAtLeftPeakEdge)) * 0.25;

                    double tolerance = Math.min(hwhm, minTolerance);

                    if (Math.abs(parentPeakAreaBeanList.get(j).rtAtPeakTop - detectedPeakAreas.get(i).rtAtPeakTop) <= tolerance) {
                        if (detectedPeakAreas.get(i).intensityAtPeakTop > parentPeakAreaBeanList.get(j).intensityAtPeakTop) {
                            parentPeakAreaBeanList.remove(j);
                            j--;
                            continue;
                        } else {
                            detectedPeakAreas.remove(i);
                            i--;
                            break;
                        }
                    }
                }
            }

            if (parentPeakAreaBeanList.isEmpty())
                return detectedPeakAreas;

            if (detectedPeakAreas.isEmpty())
                return new ArrayList<>();
        }

        return detectedPeakAreas;
    }

    /**
     * @param peakA
     * @param peakB
     * @return
     */
    public boolean isOverlapedChecker(PeakAreaBean peakA, PeakAreaBean peakB) {
        if (peakA.rtAtPeakTop > peakB.rtAtPeakTop) {
            if (peakA.rtAtLeftPeakEdge < peakB.rtAtPeakTop)
                return true;
        } else {
            if (peakA.rtAtLeftPeakEdge < peakB.rtAtPeakTop)
                return true;
        }

        return false;
    }

    /**
     * @param detectedPeaksList
     * @return
     */
    public List<PeakAreaBean> getCombinedPeakAreaBeanList(List<List<PeakAreaBean>> detectedPeaksList) {
        List<PeakAreaBean> combinedPeakAreaBeanList = new ArrayList<>();

        for(int i = 0; i < detectedPeaksList.size(); i++) {
            if(detectedPeaksList.isEmpty())
                continue;

            combinedPeakAreaBeanList.addAll(detectedPeaksList.get(i));
        }

        return combinedPeakAreaBeanList;
    }
}
