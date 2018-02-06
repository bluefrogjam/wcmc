package edu.ucdavis.fiehnlab.ms.carrot.core.msdial;

import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Ion;
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.Sample;
import edu.ucdavis.fiehnlab.ms.carrot.core.api.types.sample.ms.Feature;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.deconvolution.SpectralDeconvolution;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.isotope.IsotopeEstimator;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.peakpicking.DataDependentPeakSpotting;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.MS2DecResult;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.Peak;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.types.PeakAreaBean;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.utils.LCMSDataAccessUtility;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.utils.SpectralCentroiding;
import edu.ucdavis.fiehnlab.ms.carrot.core.msdial.utils.TypeConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MSDialProcessing {

    private static Logger logger = LoggerFactory.getLogger(MSDialProcessing.class);

    public Sample process(Sample sample, MSDialProcessingProperties properties) {
        List<Feature> spectra = TypeConverter.getJavaSpectrumList(sample);

        // Peak picking
        List<PeakAreaBean> detectedPeaks = new DataDependentPeakSpotting().getPeaks(spectra, properties);
        logger.debug("Peaks after peak detection: " + detectedPeaks.size());

        System.out.println("peakID\t  rt  \t  mz  \t  int  \tms1scan\tms2Scan");
        detectedPeaks.stream().forEach(p -> {
            System.out.println(new StringBuilder()
                .append(p.peakID).append("\t")
                .append(p.rtAtPeakTop).append("\t")
                .append(p.accurateMass).append("\t")
                .append(p.intensityAtPeakTop).append("\t")
                .append(p.ms1LevelDataPointNumber).append("\t")
                .append(p.ms2LevelDataPointNumber).toString()
            );
        });

        // Isotope detection
        new IsotopeEstimator().setIsotopeInformation(detectedPeaks, properties);
        logger.debug("Peaks after isotope estimation: " + detectedPeaks.size());

        // Calculate peak properties for deconvolution
        // DataSummary bean does not appear to be used, so skipping that translation
        List<MS2DecResult> deconvolutionResults = new SpectralDeconvolution().getMS2Deconvolution(spectra, detectedPeaks, properties);
        logger.debug("Peaks after deconvolution: " + deconvolutionResults.size());
        logger.debug("scan\trt\tms1 mz\tms1 int\tMS1 spec\tMS2 spec");
        for(MS2DecResult peak: deconvolutionResults){
            System.out.println(String.format("%d\t%.5f\t%.5f\t%.5f\t%s\t%s", peak.peakTopScan, peak.peakTopRetentionTime, peak.ms1AccurateMass, peak.ms1PeakHeight,
                peak.ms1Spectrum.stream().map(p -> String.format("%.5f:%d", p.mass(), (int)p.intensity())).collect(Collectors.joining(" ")),
                peak.ms2Spectrum.stream().map(p -> String.format("%.5f:%d", p.mz, (int)p.intensity)).collect(Collectors.joining(" "))));
        }

        MS2DecResult expScan = new MS2DecResult(new PeakAreaBean());
        expScan.peakTopScan = 32;
        expScan.peakTopRetentionTime = 0.9344167;
        expScan.ms1AccurateMass = 254.0594;
        expScan.ms1PeakHeight = 59863.6;
        expScan.ms1Spectrum = Arrays.asList(new Ion(108.04406,1539),new Ion(110.081,234), new Ion(111.09217,551), new Ion(118.08751,252), new Ion(120.0528,313),new Ion(121.05081,23501),new Ion(121.08357,508),new Ion(121.10647,231),new Ion(122.05454,1508),new Ion(125.10783,438),new Ion(127.0732,637),new Ion(128.10693,339),new Ion(128.95535,237),new Ion(136.06039,217),new Ion(139.12361,697),new Ion(145.04067,300),new Ion(149.0228,490),new Ion(151.0984,229),new Ion(156.01147,2571),new Ion(158.99718,237),new Ion(160.08651,218),new Ion(170.15096,311),new Ion(173.07701,436),new Ion(188.08157,478),new Ion(190.0947,450),new Ion(195.12333,611),new Ion(217.10639,414),new Ion(239.14807,493),new Ion(254.05941,63461),new Ion(254.10104,1374),new Ion(254.13773,573),new Ion(254.17392,202),new Ion(254.21023,277),new Ion(255.06219,6981),new Ion(256.05784,2721),new Ion(257.05918,394),new Ion(276.04243,43841),new Ion(276.09607,763),new Ion(276.12987,382),new Ion(277.04426,4787),new Ion(278.04049,1850),new Ion(292.01456,376),new Ion(298.01987,586),new Ion(391.2858,521),new Ion(392.28702,277),new Ion(529.09393,12004),new Ion(530.09855,3104),new Ion(531.09216,1509),new Ion(532.10268,279),new Ion(622.02994,416), new Ion(922.00524,1664));
        expScan.ms2Spectrum = Arrays.asList(new Peak(107.05798,91), new Peak(107.30766,13), new Peak(108.04446,990), new Peak(108.06402,84), new Peak(108.08245,52), new Peak(108.14417,11), new Peak(108.46367,61), new Peak(109.04321,129), new Peak(109.06885,11), new Peak(121.07557,188), new Peak(121.10909,31), new Peak(132.06081,20), new Peak(133.08627,58), new Peak(143.06653,50), new Peak(146.07051,51), new Peak(147.0788,408), new Peak(147.11317,12), new Peak(148.08379,289), new Peak(148.1149,14), new Peak(156.01038,3032), new Peak(156.04074,114), new Peak(156.08185,10), new Peak(156.17953,66), new Peak(156.28195,15), new Peak(156.93811,17), new Peak(156.96259,10), new Peak(157.01286,268), new Peak(157.06024,24), new Peak(157.43315,18), new Peak(157.7319,10), new Peak(158.00951,101), new Peak(158.24365,22), new Peak(158.95541,14), new Peak(159.52612,29), new Peak(160.08536,548), new Peak(160.12158,35), new Peak(161.0031,24), new Peak(161.08295,33), new Peak(161.22786,31), new Peak(163.28961,16), new Peak(164.48666,11), new Peak(174.07776,21), new Peak(176.03766,72), new Peak(188.08228,648), new Peak(188.10777,27), new Peak(189.07906,33), new Peak(189.09819,56), new Peak(190.09927,209), new Peak(190.13531,11), new Peak(191.09398,57), new Peak(192.10025,18), new Peak(192.24519,18), new Peak(194.03761,236), new Peak(194.09094,10), new Peak(195.02669,25), new Peak(212.57661,19), new Peak(236.04282,98), new Peak(239.15256,16), new Peak(252.13663,20), new Peak(254.06082,2896), new Peak(254.09279,115), new Peak(254.11615,89), new Peak(254.1559,11));

        MS2DecResult res = deconvolutionResults.stream().filter(r -> r.peakTopScan == 32 && (r.ms1AccurateMass >= 254 && r.ms1AccurateMass < 255) && (r.peakTopRetentionTime > 0.9 && r.peakTopRetentionTime < 1.0)).collect(Collectors.toList()).get(0);

        assert (res.ms1Spectrum).equals(expScan.ms1Spectrum);
        assert (res.ms2Spectrum).equals(expScan.ms2Spectrum);

        logger.warn("Returning the input sample");
        // TODO add deconvolution and return a ProcessedSample
        return sample;
    }

}
